/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.nubits.nubot.trading.wrappers;

import com.alibaba.fastjson.JSON;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiError;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BterWrapper implements TradeInterface {

    private static final Logger LOG = Logger.getLogger(BterWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private String checkConnectionUrl = "https://bter.com/";
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private final String API_BASE_URL = "https://bter.com/api/1/";
    private final String API_GET_INFO = "private/getfunds";
    private final String API_TRADE = "private/placeorder";
    private final String API_ACTIVE_ORDERS = "";
    private final String API_CANCEL_ORDER = "";
    private final String API_GET_FEE = "http://data.bter.com/api/1/marketinfo";
    // Errors
    private ArrayList<ApiError> errors;
    private final String TOKEN_ERR = "error";
    private final int ERROR_UNKNOWN = 14560;
    private final int ERROR_NO_CONNECTION = 14561;
    private final int ERROR_GENERIC = 14562;
    private final int ERROR_PARSING = 14563;
    private final int ERROR_CURRENCY_NOT_FOUND = 14567;

    public BterWrapper() {
    }

    public BterWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        setupErrors();
    }

    private void setupErrors() {
        errors = new ArrayList<ApiError>();
        errors.add(new ApiError(ERROR_NO_CONNECTION, "Failed to connect to the exchange entrypoint. Verify your connection"));
        errors.add(new ApiError(ERROR_PARSING, "Parsing error"));

    }

    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        return getBalanceImpl(null, pair);
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return getBalanceImpl(currency, null);
    }

    private ApiResponse getBalanceImpl(Currency currency, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        Balance balance = new Balance();

        String path = API_BASE_URL + API_GET_INFO;
        HashMap<String, String> query_args = new HashMap<>();
        /*Params
         */


        /*Sample result
         *{
         "result":"true",
         "available_funds":{
         "CNY":"1122.16",
         "BTC":"0.83337671",
         "LTC":"94.364",
         "YAC":"0.07161",
         "WDC":"82.35029899"
         },
         "locked_funds":{
         "BTC":"0.0002",
         "YAC":"10.01"
         }
         }

         */



        String queryResult = query(path, query_args, false);
        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = true;
            try {
                valid = Boolean.parseBoolean((String) httpAnswerJson.get("result"));
            } catch (ClassCastException e) {
                valid = true;
            }

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("msg");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);

                LOG.severe("Bter API returned an error: " + errorMessage);

                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct
                JSONObject availableFundsJSON = (JSONObject) httpAnswerJson.get("available_funds");
                JSONObject lockedFundsJSON = (JSONObject) httpAnswerJson.get("locked_funds");

                if (currency == null) { //Get all balances

                    boolean foundNBTavail = false;
                    boolean foundPEGavail = false;

                    Amount NBTAvail = null,
                            PEGAvail = null;

                    Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency());
                    Amount NBTonOrder = new Amount(0, pair.getOrderCurrency());

                    String NBTcode = pair.getOrderCurrency().getCode().toUpperCase();
                    String PEGcode = pair.getPaymentCurrency().getCode().toUpperCase();
                    if (availableFundsJSON.containsKey(NBTcode)) {
                        double tempbalance = Double.parseDouble((String) availableFundsJSON.get(NBTcode));
                        NBTAvail = new Amount(tempbalance, pair.getOrderCurrency());
                        foundNBTavail = true;
                    }

                    if (availableFundsJSON.containsKey(PEGcode)) {
                        double tempbalance = Double.parseDouble((String) availableFundsJSON.get(PEGcode));
                        PEGAvail = new Amount(tempbalance, pair.getOrderCurrency());
                        foundPEGavail = true;
                    }

                    if (lockedFundsJSON.containsKey(NBTcode)) {
                        double tempbalance = Double.parseDouble((String) lockedFundsJSON.get(NBTcode));
                        NBTonOrder = new Amount(tempbalance, pair.getOrderCurrency());
                    }

                    if (lockedFundsJSON.containsKey(PEGcode)) {
                        double tempbalance = Double.parseDouble((String) lockedFundsJSON.get(PEGcode));
                        PEGonOrder = new Amount(tempbalance, pair.getOrderCurrency());
                    }


                    if (foundNBTavail && foundPEGavail) {
                        //Pack it into the ApiResponse
                        balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);

                        apiResponse.setResponseObject(balance);
                    } else {
                        apiResponse.setError(new ApiError(ERROR_CURRENCY_NOT_FOUND, ""
                                + "Cannot find a currency with code " + NBTcode + " or " + PEGcode + " in your balance"));
                    }

                } else { //Get specific balance

                    boolean found = false;
                    Amount avail = null;
                    String code = currency.getCode().toUpperCase();
                    if (availableFundsJSON.containsKey(code)) {
                        double tempbalance = Double.parseDouble((String) availableFundsJSON.get(code));
                        avail = new Amount(tempbalance, currency);
                        found = true;
                    }

                    if (found) {
                        apiResponse.setResponseObject(avail);
                    } else {
                        apiResponse.setError(new ApiError(ERROR_CURRENCY_NOT_FOUND, ""
                                + "Cannot find a currency with code " + code + " in your balance"));
                    }
                }
            }
        } catch (ParseException ex) {
            LOG.severe(ex.getMessage());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the balance response"));
            return apiResponse;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        return getLastPriceImpl(pair, false);
    }

    public ApiResponse getLastPriceFeed(CurrencyPair pair) { //used for BterPriceFeed only
        return getLastPriceImpl(pair, true);
    }

    private ApiResponse getLastPriceImpl(CurrencyPair pair, boolean bypass) {
        Ticker ticker = new Ticker();
        ApiResponse apiResponse = new ApiResponse();

        double last = -1;
        double ask = -1;
        double bid = -1;

        String ticker_url = API_BASE_URL + getTickerPath(pair);

        HashMap<String, String> query_args = new HashMap<>();


        /* Sample response
         * {"result":"true","last":2599,"high":2620,"low":2406,"avg":2526.11,"sell":2598,"buy":2578,"vol_btc":544.5027,"vol_cny":1375475.92}
         */

        String queryResult = "";
        if (bypass) { //sed by BterPriceFeed only
            BterService query = new BterService(ticker_url, keys, query_args);
            queryResult = query.executeQuery(true, true);
        } else {
            queryResult = query(ticker_url, query_args, true);
        }


        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = true;
            try {
                valid = Boolean.parseBoolean((String) httpAnswerJson.get("result"));
            } catch (ClassCastException e) {
                valid = true;
            }

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("message");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);

                LOG.severe("Bter API returned an error: " + errorMessage);

                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct

                last = Utils.getDouble(httpAnswerJson.get("last"));
                bid = Utils.getDouble(httpAnswerJson.get("sell"));
                ask = Utils.getDouble(httpAnswerJson.get("buy"));


            }
        } catch (ParseException ex) {
            LOG.severe(ex.getMessage());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the balance response"));
            return apiResponse;
        }

        ticker.setAsk(ask);
        ticker.setBid(bid);
        ticker.setLast(last);
        apiResponse.setResponseObject(ticker);
        return apiResponse;

    }

    @Override
    public ApiResponse sell(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.SELL.toUpperCase(), pair, amount, rate);
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.BUY.toUpperCase(), pair, amount, rate);
    }

    private ApiResponse enterOrder(String type, CurrencyPair pair, double amount, double rate) {
        ApiResponse apiResponse = new ApiResponse();
        String path = API_BASE_URL + API_TRADE;

        String order_id = "";
        HashMap<String, String> query_args = new HashMap<>();
        query_args.put("pair", pair.toString("_").toLowerCase());
        query_args.put("type", type);
        query_args.put("rate", Double.toString(rate));
        query_args.put("amount", Double.toString(amount));

        /* Sample response
         * {
         "result":"true",
         "order_id":"123456",
         "msg":"Success"
         }
         */

        String queryResult = query(path, query_args, false);
        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = (boolean) httpAnswerJson.get("result");

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("msg");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);

                LOG.severe("Bter API returned an error: " + errorMessage);

                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct
                order_id = "" + (long) httpAnswerJson.get("order_id");
                String msg = (String) httpAnswerJson.get("msg");
                if (!msg.equals("Success")) {
                    LOG.severe("BTER : Something went wrong while placing the order :" + msg);
                    ApiError apiErr = new ApiError(ERROR_GENERIC, msg);
                    apiResponse.setError(apiErr);
                    return apiResponse;
                } else {
                    apiResponse.setResponseObject(order_id);
                    return apiResponse;
                }
            }
        } catch (ParseException ex) {
            LOG.severe(ex.getMessage());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the balance response"));
            return apiResponse;
        }
    }

    @Override
    public ApiResponse getActiveOrders() {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse cancelOrder(String orderID) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getTxFee() {
        return new ApiResponse(false, null,
                new ApiError(ERROR_UNKNOWN, "For Bter the fee changes with the currency. Please be more specific"));
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        double fee = 0;


        String path = API_GET_FEE;

        HashMap<String, String> query_args = new HashMap<>();


        /* Sample response
         * {"result":"true",
         * "pairs":[{"btc_cny":{"decimal_places":2,"min_amount":0.5,"fee":0}},
         *  {"ltc_cny":{"decimal_places":2,"min_amount":0.5,"fee":0}},
         *  {"bc_cny":{"decimal_places":3,"min_amount":0.5,"fee":0.2}}....
         }
         */

        String queryResult = query(path, query_args, true);

        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = true;
            try {
                valid = Boolean.parseBoolean((String) httpAnswerJson.get("result"));
            } catch (ClassCastException e) {
                valid = true;
            }

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("msg");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);

                LOG.severe("Bter API returned an error: " + errorMessage);

                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct

                com.alibaba.fastjson.JSONObject response = JSON.parseObject(queryResult);
                com.alibaba.fastjson.JSONArray array = response.getJSONArray("pairs");

                String searchingFor = pair.toString("_").toLowerCase();

                for (int i = 0; i < array.size(); i++) {
                    com.alibaba.fastjson.JSONObject tempObj = array.getJSONObject(i);
                    if (tempObj.containsKey(searchingFor)) {
                        {
                            com.alibaba.fastjson.JSONObject tempObjCorrect = (com.alibaba.fastjson.JSONObject) tempObj.get(searchingFor);
                            fee = tempObjCorrect.getDoubleValue("fee");
                            apiResponse.setResponseObject(fee);
                            return apiResponse;
                        }
                    }
                }
                //Not found
                ApiError err = new ApiError(ERROR_UNKNOWN, "Did not found fee for pair " + searchingFor);
                apiResponse.setError(err);
                return apiResponse;
            }
        } catch (ParseException ex) {
            LOG.severe(ex.getMessage());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the balance response"));
            return apiResponse;
        }
    }

    public static String getTickerPath(CurrencyPair pair) {
        return "ticker/" + pair.toString("_").toLowerCase();
    }

    @Override
    public ApiResponse getPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse orderExists(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse clearOrders() {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiError getErrorByCode(int code) {
        boolean found = false;
        ApiError toReturn = null;;
        for (int i = 0; i < errors.size(); i++) {
            ApiError temp = errors.get(i);
            if (code == temp.getCode()) {
                found = true;
                toReturn = temp;
                break;
            }
        }

        if (found) {
            return toReturn;
        } else {
            return new ApiError(ERROR_UNKNOWN, "Unknown API error");
        }
    }

    @Override
    public String getUrlConnectionCheck() {
        return checkConnectionUrl;
    }

    @Override
    public String query(String url, HashMap<String, String> args, boolean isGet) {
        BterService query = new BterService(url, keys, args);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, isGet);
        } else {
            LOG.severe("The bot will not execute the query, there is no connection to bter");
            queryResult = "error : no connection with Bter";
        }


        return queryResult;
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.

    }

    @Override
    public String query(String url, TreeMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String query(String base, String method, TreeMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setKeys(ApiKeys keys) {
        this.keys = keys;
    }

    @Override
    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void setApiBaseUrl(String apiBaseUrl) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.

    }

    private class BterService implements ServiceInterface {

        protected String base;
        protected String method;
        protected HashMap args;
        protected ApiKeys keys;
        protected String url;

        private BterService(String url, ApiKeys keys, HashMap<String, String> args) {
            //Used for ticker, does not require auth
            this.url = url;
            this.args = args;
            this.method = "";
            this.keys = keys;
        }

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
            String answer = "";
            String signature = "";
            String post_data = "";

            List< NameValuePair> urlParameters = new ArrayList< NameValuePair>();

            for (Iterator< Map.Entry< String, String>> argumentIterator = args.entrySet().iterator(); argumentIterator.hasNext();) {

                Map.Entry< String, String> argument = argumentIterator.next();

                urlParameters.add(new BasicNameValuePair(argument.getKey().toString(), argument.getValue().toString()));

                if (post_data.length() > 0) {
                    post_data += "&";
                }

                post_data += argument.getKey() + "=" + argument.getValue();

            }

            signature = signRequest(keys.getPrivateKey(), post_data);

            // add header
            Header[] headers = new Header[3];
            headers[ 0] = new BasicHeader("Key", keys.getApiKey());
            headers[ 1] = new BasicHeader("Sign", signature);
            headers[ 2] = new BasicHeader("Content-type", "application/x-www-form-urlencoded");

            URL queryUrl;
            try {
                queryUrl = new URL(url);
            } catch (MalformedURLException ex) {
                LOG.severe(ex.getMessage());
            }
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = null;
            HttpGet get = null;
            HttpResponse response = null;


            try {
                if (!isGet) {
                    post = new HttpPost(url);
                    post.setEntity(new UrlEncodedFormEntity(urlParameters));
                    post.setHeaders(headers);
                    response = client.execute(post);
                } else {
                    get = new HttpGet(url);
                    get.setHeaders(headers);
                    response = client.execute(get);
                }
            } catch (Exception e) {
                LOG.severe(e.getMessage());
            }
            BufferedReader rd;


            try {
                rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));


                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    buffer.append(line);
                }

                answer = buffer.toString();
            } catch (IOException ex) {
                Logger.getLogger(BterWrapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalStateException ex) {
                Logger.getLogger(BterWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (Global.options
                    != null && Global.options.isVerbose()) {

                LOG.fine("\nSending request to URL : " + url + " ; get = " + isGet);
                if (post != null) {
                    System.out.println("Post parameters : " + post.getEntity());
                }
                LOG.fine("Response Code : " + response.getStatusLine().getStatusCode());
                LOG.fine("Response :" + response);

            }
            return answer;
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            String sign = "";
            try {
                Mac mac = null;
                SecretKeySpec key = null;
                // Create a new secret key
                try {
                    key = new SecretKeySpec(secret.getBytes(ENCODING), SIGN_HASH_FUNCTION);
                } catch (UnsupportedEncodingException uee) {
                    LOG.severe("Unsupported encoding exception: " + uee.toString());
                }

                // Create a new mac
                try {
                    mac = Mac.getInstance(SIGN_HASH_FUNCTION);
                } catch (NoSuchAlgorithmException nsae) {
                    LOG.severe("No such algorithm exception: " + nsae.toString());
                }

                // Init mac with key.
                try {
                    mac.init(key);
                } catch (InvalidKeyException ike) {
                    LOG.severe("Invalid key exception: " + ike.toString());
                }

                sign = Hex.encodeHexString(mac.doFinal(hash_data.getBytes(ENCODING)));

            } catch (UnsupportedEncodingException ex) {
                LOG.severe(ex.getMessage());
            }
            return sign;

        }
    }
}
