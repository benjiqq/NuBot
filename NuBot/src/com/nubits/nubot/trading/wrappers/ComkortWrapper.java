/*
 * Copyright (C) 2014 Nu Development Team
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

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.*;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.ErrorManager;
import com.nubits.nubot.utils.Utils;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by woolly_sammoth on 03/02/15.
 */
public class ComkortWrapper implements TradeInterface {
    private static final Logger LOG = Logger.getLogger(ExcoinWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private String apiBaseUrl;
    private String checkConnectionUrl;
    //API Paths
    private final String API_BASE_URL = "https://api.comkort.com/v1/private";
    private final String API_BASE_URL_PUBLIC = "https://api.comkort.com/v1/public";
    private final String API_USER = "user";
    private final String API_BALANCE = "balance";
    private final String API_MARKET = "market";
    private final String API_SUMMARY = "summary";
    private final String API_ORDER = "order";
    private final String API_SELL = "sell";
    private final String API_BUY = "buy";
    private final String API_LIST = "list";
    private final String API_LIST_ALL = "list_all";
    private final String API_CANCEL = "cancel";
    //Errors
    private ErrorManager errors = new ErrorManager();
    private final String TOKEN_ERR = "error";
    private final String TOKEN_BAD_RETURN = "No Connection With Exchange";
    private final String TOKEN_CODE = "Code";


    public ComkortWrapper() {
        setupErrors();
    }

    public ComkortWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        setupErrors();
    }

    private void setupErrors() {
        errors.setExchangeName(exchange);
    }

    private ApiResponse getQuery(String url, HashMap<String, String> query_args, boolean isGet) {
        ApiResponse apiResponse = new ApiResponse();
        String queryResult = query(url, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(errors.nullReturnError);
            return apiResponse;
        }
        if (queryResult.equals(TOKEN_BAD_RETURN)) {
            apiResponse.setError(errors.noConnectionError);
            return apiResponse;
        }

        JSONParser parser = new JSONParser();

        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            apiResponse.setResponseObject(httpAnswerJson);
        } catch (ClassCastException cce) {
            //if casting to a JSON object failed, try a JSON Array
            try {
                JSONArray httpAnswerJson = (JSONArray) (parser.parse(queryResult));
                apiResponse.setResponseObject(httpAnswerJson);
            } catch (ParseException pe) {
                LOG.severe("httpResponse: " + queryResult + " \n" + pe.toString());
                apiResponse.setError(errors.parseError);
            }
        } catch (ParseException pe) {
            LOG.severe("httpResponse: " + queryResult + " \n" + pe.toString());
            apiResponse.setError(errors.parseError);
            return apiResponse;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        return getAvailableBalancesImpl(null, pair);
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return getAvailableBalancesImpl(currency, null);
    }

    private ApiResponse getAvailableBalancesImpl(Currency currency, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        //https://api.comkort.com/v1/private/user/balance
        String url = API_BASE_URL + "/" + API_USER + "/" + API_BALANCE;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;

        if (currency != null) { //get a specific currency
            args.put("key", currency.getCode().toUpperCase());
        }

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            if (currency != null) { //only one currency
                JSONObject balances = (JSONObject) httpAnswerJson.get(currency.getCode().toUpperCase());
                Amount total = new Amount(Utils.getDouble(balances.get("balance")), currency);
                apiResponse.setResponseObject(total);
            } else {
                JSONObject pegBalances = (JSONObject) httpAnswerJson.get(pair.getPaymentCurrency().getCode().toUpperCase());
                Amount PEGAvail = new Amount(Utils.getDouble(pegBalances.get("balance")), pair.getPaymentCurrency());
                Amount PEGonOrder = new Amount(Utils.getDouble(pegBalances.get("reserve")), pair.getPaymentCurrency());
                JSONObject nbtBalances = (JSONObject) httpAnswerJson.get(pair.getOrderCurrency().getCode().toUpperCase());
                Amount NBTAvail = new Amount(Utils.getDouble(nbtBalances.get("balance")), pair.getOrderCurrency());
                Amount NBTonOrder = new Amount(Utils.getDouble(nbtBalances.get("reserve")), pair.getOrderCurrency());
                Balance balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
                apiResponse.setResponseObject(balance);
            }
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        //market/summary?market_alias={param}
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL_PUBLIC + "/" + API_MARKET + "/" + API_SUMMARY;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = true;
        
        args.put("market_alias", pair.toString("_"));
        
        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            
            Ticker ticker = new Ticker();
            double last = -1;
            double ask = -1;
            double bid = -1;
            
            JSONObject markets = (JSONObject) httpAnswerJson.get("markets");
            JSONObject market = (JSONObject) markets.get(pair.getOrderCurrency().getCode().toUpperCase() + "/" + pair.getPaymentCurrency().getCode().toUpperCase());
            last = Utils.getDouble(market.get("last_price"));
            ask = Utils.getDouble(market.get("low"));
            bid = Utils.getDouble(market.get("high"));
            
            ticker.setAsk(ask);
            ticker.setBid(bid);
            ticker.setLast(last);
            
            apiResponse.setResponseObject(ticker);
        } else {
            apiResponse = response;
        }
        
        return apiResponse;
    }

    @Override
    public ApiResponse sell(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.SELL, pair, amount, rate);
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.BUY, pair, amount, rate);
    }
    
    private ApiResponse enterOrder(String type, CurrencyPair pair, double amount, double rate) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_ORDER + "/";
        if (type.equals(Constant.BUY)) {
            url += API_BUY;
        } else {
            url += API_SELL;
        }
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;

        args.put("market_alias", pair.toString("_"));
        args.put("amount", Objects.toString(amount));
        args.put("price", Objects.toString(rate));

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()){
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            String order_id = httpAnswerJson.get("order_id").toString();
            apiResponse.setResponseObject(order_id);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders() {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_ORDER + "/" + API_LIST_ALL;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        ArrayList<Order> orderList = new ArrayList<>();

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject orders = (JSONObject) httpAnswerJson.get("orders");
            Set<String> keys = orders.keySet();
            for (Iterator<String> key = keys.iterator(); key.hasNext();) {
                String thisKey = key.next();
                CurrencyPair thisPair = CurrencyPair.getCurrencyPairFromString(thisKey, "_");
                JSONArray pairOrders = (JSONArray) orders.get(thisKey);
                for (Iterator<JSONObject> order = pairOrders.iterator(); order.hasNext();) {
                    orderList.add(parseOrder(order.next(), thisPair));
                }
            }
            apiResponse.setResponseObject(orderList);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_ORDER + "/" + API_LIST;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        ArrayList<Order> orderList = new ArrayList<>();

        args.put("market_alias", pair.toString("_"));
        
        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray orders = (JSONArray) httpAnswerJson.get("orders");
            for (Iterator<JSONObject> order = orders.iterator(); order.hasNext();) {
                orderList.add(parseOrder(order.next(), pair));
            }
            apiResponse.setResponseObject(orderList);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }
    
    private Order parseOrder(JSONObject in, CurrencyPair pair) {
        Order out = new Order();

        out.setId(in.get("id").toString());
        out.setType(in.get("type") == "sell" ? Constant.SELL : Constant.BUY);
        Amount amount = new Amount(Utils.getDouble(in.get("amount").toString()), pair.getOrderCurrency());
        out.setAmount(amount);
        Amount price = new Amount(Utils.getDouble(in.get("price").toString()), pair.getPaymentCurrency());
        out.setPrice(price);
        out.setPair(pair);
        if (in.containsKey("added")) {
            LOG.severe(in.get("added").toString());
            long timeStamp = (long) Utils.getDouble(in.get("added").toString());
            LOG.severe(Objects.toString(timeStamp));
            Date insertDate = new Date(timeStamp * 1000);
            out.setInsertedDate(insertDate);
        }

        return out;        
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        ApiResponse apiResponse = new ApiResponse();
        
        ApiResponse activeOrders = getActiveOrders();
        if (activeOrders.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext();) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(orderID)) {
                    apiResponse.setResponseObject(thisOrder);
                }
            }
        } else {
            apiResponse = activeOrders;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_ORDER + "/" + API_CANCEL;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        
        args.put("order_id", orderID);
        
        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            boolean success = (boolean) httpAnswerJson.get("success");
            apiResponse.setResponseObject(success);
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee() {
        double defaultFee = Global.options == null ? 0.2 : Global.options.getTxFee();
        return new ApiResponse(true, defaultFee, null);
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        return getTxFee();
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        return null;
    }

    @Override
    public ApiResponse isOrderActive(String id) {
        return null;
    }

    @Override
    public ApiResponse getOrderBook(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiError getErrorByCode(int code) {
        return null;
    }

    @Override
    public String getUrlConnectionCheck() {
        return API_BASE_URL;
    }

    @Override
    public String query(String url, HashMap<String, String> args, boolean isGet) {
        if (!exchange.getLiveData().isConnected()) {
            LOG.severe("The bot will not execute the query, there is no connection to Comkort");
            return TOKEN_BAD_RETURN;
        }
        String queryResult;
        ComkortService query = new ComkortService(url, args, keys);
        if (isGet) {
            queryResult = query.executeQuery(false, isGet);
        } else {
            queryResult = query.executeQuery(true, isGet);
        }
        return queryResult;
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        return null;
    }

    @Override
    public String query(String url, TreeMap<String, String> args, boolean isGet) {
        return null;
    }

    @Override
    public String query(String base, String method, TreeMap<String, String> args, boolean isGet) {
        return null;
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
        
        
    }

    private class ComkortService implements ServiceInterface {

        protected String url;
        protected HashMap args;
        protected ApiKeys keys;

        public ComkortService(String url, HashMap<String, String> args, ApiKeys keys) {
            this.url = url;
            this.args = args;
            this.keys = keys;
        }

        private ComkortService(String url, HashMap<String, String> args) {
            //Used for ticker, does not require auth
            this.url = url;
            this.args = args;
        }

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
            HttpsURLConnection connection = null;
            URL queryUrl = null;
            String post_data = "";
            boolean httpError = false;
            String output;
            int response = 200;
            String answer = null;
            post_data = TradeUtils.buildQueryString(args, ENCODING);
            
            try {
                if (isGet) {
                    queryUrl = new URL(url + "?" + post_data);
                } else {
                    queryUrl = new URL(url);
                }
            } catch (MalformedURLException mal) {
                LOG.severe(mal.toString());
                return null;
            }

            try {
                connection = (HttpsURLConnection) queryUrl.openConnection();
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", Global.settings.getProperty("app_name"));

                if (needAuth) {
                    connection.setRequestProperty("apikey", keys.getApiKey());
                    connection.setRequestProperty("sign", signRequest(keys.getPrivateKey(), post_data));
                    connection.setRequestProperty("nonce", Objects.toString(System.currentTimeMillis() / 1000L));
                }

                connection.setDoOutput(true);
                connection.setDoInput(true);

                if (isGet) {
                    connection.setRequestMethod("GET");
                } else {
                    connection.setRequestMethod("POST");
                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(post_data);
                    os.flush();
                    os.close();
                }
            } catch (ProtocolException pe) {
                LOG.severe(pe.toString());
                return null;
            } catch (IOException io) {
                LOG.severe((io.toString()));
                return null;
            }


            BufferedReader br = null;
            try {
                if (connection.getResponseCode() >= 400) {
                    httpError = true;
                    response = connection.getResponseCode();
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                } else {
                    answer = "";
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
            } catch (IOException io) {
                LOG.severe(io.toString());
                return null;
            }

            if (httpError) {
                LOG.severe("Query to : " + url
                        + "\nData : " + post_data
                        + "\nHTTP Response : " + Objects.toString(response));
                String error = "";
                try {
                    while ((output = br.readLine()) != null) {
                        error += output;
                    }
                } catch (IOException io) {
                    LOG.severe(io.toString());
                }
                return "{'error': 'httpError', 'response': " + error + "}";
            }

            try {
                while ((output = br.readLine()) != null) {
                    answer += output;
                }
            } catch (IOException io) {
                LOG.severe(io.toString());
                return null;
            }

            connection.disconnect();
            connection = null;

            return answer;
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            String sign = "";
            try {
                Mac mac;
                SecretKeySpec key;
                // Create a new secret key
                key = new SecretKeySpec(secret.getBytes(ENCODING), SIGN_HASH_FUNCTION);
                // Create a new mac
                mac = Mac.getInstance(SIGN_HASH_FUNCTION);
                // Init mac with key.
                mac.init(key);
                sign = Hex.encodeHexString(mac.doFinal(hash_data.getBytes(ENCODING)));
            } catch (UnsupportedEncodingException uee) {
                LOG.severe("Unsupported encoding exception: " + uee.toString());
            } catch (NoSuchAlgorithmException nsae) {
                LOG.severe("No such algorithm exception: " + nsae.toString());
            } catch (InvalidKeyException ike) {
                LOG.severe("Invalid key exception: " + ike.toString());
            }
            return sign;
        }
    }

}
