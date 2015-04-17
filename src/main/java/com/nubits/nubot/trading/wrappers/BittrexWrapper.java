/*
 * Copyright (C) 2015 Nu Development Team
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

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.*;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.trading.*;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.Utils;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by woolly_sammoth on 05/03/15.
 */
public class BittrexWrapper implements TradeInterface {

    private static final Logger LOG = LoggerFactory.getLogger(BittrexWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    protected BittrexService service;

    private Exchange exchange;
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private String apiBaseUrl;
    private String checkConnectionUrl;
    //API Paths
    private final String API_BASE_URL = "https://bittrex.com/api/v1.1";
    private final String API_BALANCE = "account/getbalance";
    private final String API_BALANCES = "account/getbalances";
    private final String API_TICKER = "public/getticker";
    private final String API_SELL = "market/selllimit";
    private final String API_BUY = "market/buylimit";
    private final String API_ACTIVE_ORDERS = "market/getopenorders";
    private final String API_CANCEL = "market/cancel";
    private final String API_LAST_ORDERS = "account/getorderhistory";
    //Errors
    private ErrorManager errors = new ErrorManager();
    private final String TOKEN_ERR = "error";
    private final String TOKEN_BAD_RETURN = "No Connection With Exchange";

    public BittrexWrapper() {
        setupErrors();
    }

    public BittrexWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        service = new BittrexService(keys);
        setupErrors();
    }

    private void setupErrors() {
        errors.setExchangeName(exchange);
    }

    private ApiResponse getQuery(String url, String method, HashMap<String, String> query_args, boolean needAuth, boolean isGet) {
        ApiResponse apiResponse = new ApiResponse();
        String queryResult = query(url, method, query_args, needAuth, isGet);
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
            if (!(boolean) httpAnswerJson.get("success")) {
                ApiError error = errors.apiReturnError;
                error.setDescription(httpAnswerJson.get("message").toString());
                apiResponse.setError(error);
                return apiResponse;
            }
            apiResponse.setResponseObject(httpAnswerJson);
        } catch (ClassCastException cce) {
            //if casting to a JSON object failed, try a JSON Array
            try {
                JSONArray httpAnswerJson = (JSONArray) (parser.parse(queryResult));
                apiResponse.setResponseObject(httpAnswerJson);
            } catch (ParseException pe) {
                LOG.error("httpResponse: " + queryResult + " \n" + pe.toString());
                apiResponse.setError(errors.parseError);
            }
        } catch (ParseException pe) {
            LOG.error("httpResponse: " + queryResult + " \n" + pe.toString());
            apiResponse.setError(errors.parseError);
            return apiResponse;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        HashMap<String, String> args = new HashMap<>();
        String method = API_BALANCES;
        boolean isGet = true;
        boolean needAuth = true;

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            if (httpAnswerJson.get("result") == null) {
                ApiError error = errors.nullReturnError;
                error.setDescription("No Wallets Enabled");
                apiResponse.setError(error);
            } else {
                Amount NBTOnOrder = new Amount(0, pair.getOrderCurrency());
                Amount NBTAvailable = new Amount(0, pair.getOrderCurrency());
                Amount PegOnOrder = new Amount(0, pair.getPaymentCurrency());
                Amount PegAvailable = new Amount(0, pair.getPaymentCurrency());
                JSONArray result = (JSONArray) httpAnswerJson.get("result");
                for (Iterator<JSONObject> wallet = result.iterator(); wallet.hasNext(); ) {
                    JSONObject thisWallet = wallet.next();
                    if (thisWallet.get("Currency").equals(pair.getPaymentCurrency().getCode().toUpperCase())) {
                        PegAvailable.setQuantity(Utils.getDouble(thisWallet.get("Available")));
                        PegOnOrder.setQuantity(Utils.getDouble(thisWallet.get("Balance")) - PegAvailable.getQuantity());
                    }
                    if (thisWallet.get("Currency").equals(pair.getOrderCurrency().getCode().toUpperCase())) {
                        NBTAvailable.setQuantity(Utils.getDouble(thisWallet.get("Available")));
                        NBTOnOrder.setQuantity(Utils.getDouble(thisWallet.get("Balance")) - NBTAvailable.getQuantity());
                    }
                }
                PairBalance balance = new PairBalance(PegAvailable, NBTAvailable, PegOnOrder, NBTOnOrder);
                apiResponse.setResponseObject(balance);
            }
        } else {
            apiResponse = response;
        }
        return apiResponse;

    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        HashMap<String, String> args = new HashMap<>();
        String method = API_BALANCE;
        args.put("currency", currency.getCode().toUpperCase());
        boolean isGet = true;
        boolean needAuth = true;

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            if (httpAnswerJson.get("result") == null) {
                ApiError error = errors.nullReturnError;
                error.setDescription("No Wallet Enabled for " + currency.getExtendedName());
                apiResponse.setError(error);
            } else {
                JSONObject result = (JSONObject) httpAnswerJson.get("result");
                Amount balance = new Amount(Utils.getDouble(result.get("Available")), currency);
                apiResponse.setResponseObject(balance);
            }
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = API_TICKER;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = true;
        boolean needAuth = true;

        args.put("market", pair.toStringSepInverse("-").toUpperCase());
        ApiResponse response = getQuery(url, method, args, needAuth, isGet);

        if (response.isPositive()) {

            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject result = (JSONObject) httpAnswerJson.get("result");
            double last = Utils.getDouble(result.get("Last"));
            double ask = Utils.getDouble(result.get("Ask"));
            double bid = Utils.getDouble(result.get("Bid"));
            Ticker ticker = new Ticker(last, ask, bid);
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
        String url = API_BASE_URL;
        String method;
        if (type.equals(Constant.SELL)) {
            method = API_SELL;
        } else {
            method = API_BUY;
        }
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = true;
        boolean needAuth = true;

        args.put("market", pair.toStringSepInverse("-").toUpperCase());
        DecimalFormat nf = new DecimalFormat("0");
        nf.setMinimumFractionDigits(8);
        args.put("quantity", nf.format(amount));
        args.put("rate", nf.format(rate));

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject result = (JSONObject) httpAnswerJson.get("result");
            String orderId = result.get("uuid").toString();
            apiResponse.setResponseObject(orderId);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders() {
        return getActiveOrdersImpl(null);
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        return getActiveOrdersImpl(pair);
    }

    private ApiResponse getActiveOrdersImpl(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = API_ACTIVE_ORDERS;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = true;
        boolean needAuth = true;
        ArrayList<Order> orderList = new ArrayList<>();

        if (pair != null) {
            args.put("market", pair.toStringSepInverse("-"));
        }

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray result = (JSONArray) httpAnswerJson.get("result");
            for (Iterator<JSONObject> order = result.iterator(); order.hasNext(); ) {
                JSONObject thisOrder = order.next();
                orderList.add(parseOrder(thisOrder));
            }
            apiResponse.setResponseObject(orderList);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    private Order parseOrder(JSONObject in) {
        Order out = new Order();

        out.setId(in.get("OrderUuid").toString());
        //Bittrex returns the currencies inverted. turn them round here.
        CurrencyPair orderPair = CurrencyPair.getCurrencyPairFromString(in.get("Exchange").toString(), "-");
        CurrencyPair pair = new CurrencyPair(orderPair.getPaymentCurrency(), orderPair.getOrderCurrency());
        out.setPair(pair);
        out.setType(in.get("OrderType").toString().contains("SELL") ? Constant.SELL : Constant.BUY);
        Amount amount = new Amount(Utils.getDouble(in.get("Quantity")), pair.getOrderCurrency());
        out.setAmount(amount);
        Amount price = new Amount(Utils.getDouble(in.get("Price")), pair.getPaymentCurrency());
        out.setPrice(price);
        //2014-07-09T03:55:48.77
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
        Date date = null;
        try {
            if (in.get("Opened") != null) {
                date = sdf.parse(in.get("Opened").toString());
            }
        } catch (java.text.ParseException pe) {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                if (in.get("Opened") != null) {
                    date = sdf.parse(in.get("Opened").toString());
                }
            } catch (java.text.ParseException pe1) {
                LOG.error(pe1.toString());
            }
        }
        if (date != null) {
            long timeStamp = date.getTime();
            Date insertDate = new Date(timeStamp);
            out.setInsertedDate(insertDate);
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
        date = null;
        try {
            if (in.get("Closed") != null) {
                date = sdf.parse(in.get("Closed").toString());
            }
        } catch (java.text.ParseException pe) {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                if (in.get("Opened") != null) {
                    date = sdf.parse(in.get("Closed").toString());
                }
            } catch (java.text.ParseException pe1) {
                LOG.error(pe1.toString());
            }
        }
        if (date != null) {
            long timeStamp = date.getTime();
            Date insertDate = new Date(timeStamp);
            out.setExecutedDate(insertDate);
        }
        out.setCompleted(false);
        if (Utils.getDouble(in.get("QuantityRemaining")) == 0) {
            out.setCompleted(true);
        }

        return out;
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        ApiResponse apiResponse = new ApiResponse();
        ApiResponse getOrders = getActiveOrders();
        boolean found = false;

        if (getOrders.isPositive()) {
            ArrayList<Order> orderList = (ArrayList) getOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(orderID)) {
                    apiResponse.setResponseObject(thisOrder);
                    found = true;
                    break;
                }
            }
        } else {
            return getOrders;
        }
        if (!found) {
            String message = "The order " + orderID + " does not exist";
            ApiError err = errors.apiReturnError;
            err.setDescription(message);
            apiResponse.setError(err);
        }

        return apiResponse;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = API_CANCEL;
        HashMap<String, String> args = new HashMap<>();
        boolean needAuth = true;
        boolean isGet = true;

        args.put("uuid", orderID);

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);
        if (response.isPositive()) {
            apiResponse.setResponseObject(true);
        } else {
            apiResponse.setResponseObject(false);
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseObject(0.25);
        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseObject(0.25);
        return apiResponse;
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair) {
        return getLastTradesImpl(pair, 0);
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        return getLastTradesImpl(pair, startTime);
    }

    private ApiResponse getLastTradesImpl(CurrencyPair pair, long startTime) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = API_LAST_ORDERS;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = true;
        boolean needAuth = true;
        ArrayList<Trade> tradeList = new ArrayList<>();

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray result = (JSONArray) httpAnswerJson.get("result");
            for (Iterator<JSONObject> trade = result.iterator(); trade.hasNext(); ) {
                Trade thisTrade = parseTrade(trade.next());
                if (thisTrade.getDate() == null) {
                    continue;
                }
                if (startTime > 0 && thisTrade.getDate().getTime() < startTime) {
                    continue;
                }
                tradeList.add(thisTrade);
            }
            apiResponse.setResponseObject(tradeList);
        } else {
            apiResponse = response;
        }


        return apiResponse;
    }

    private Trade parseTrade(JSONObject in) {
        Trade out = new Trade();

        out.setExchangeName(Global.exchange.getName());
        //Bittrex returns the currencies inverted. turn them round here.
        CurrencyPair orderPair = CurrencyPair.getCurrencyPairFromString(in.get("Exchange").toString(), "-");
        CurrencyPair pair = new CurrencyPair(orderPair.getPaymentCurrency(), orderPair.getOrderCurrency());
        out.setPair(pair);
        out.setType(in.get("OrderType").toString().contains("SELL") ? Constant.SELL : Constant.BUY);
        Amount amount = new Amount(Utils.getDouble(in.get("Quantity")), pair.getOrderCurrency());
        out.setAmount(amount);
        Amount price = new Amount(Utils.getDouble(in.get("Price")), pair.getPaymentCurrency());
        out.setPrice(price);
        //2014-07-09T03:55:48.77
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
        Date date = null;
        try {
            if (in.get("Closed") != null) {
                date = sdf.parse(in.get("Closed").toString());
            }
        } catch (java.text.ParseException pe) {
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            try {
                if (in.get("Opened") != null) {
                    date = sdf.parse(in.get("Closed").toString());
                }
            } catch (java.text.ParseException pe1) {
                LOG.error(pe1.toString());
            }
        }
        if (date != null) {
            long timeStamp = date.getTime();
            Date insertDate = new Date(timeStamp);
            out.setDate(insertDate);
        }

        return out;
    }

    @Override
    public ApiResponse isOrderActive(String id) {
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse getOrders = getActiveOrders();
        if (getOrders.isPositive()) {
            apiResponse.setResponseObject(false);
            ArrayList<Order> orderList = (ArrayList) getOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(id)) {
                    apiResponse.setResponseObject(!thisOrder.isCompleted());
                }
            }
        } else {
            apiResponse = getOrders;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getOrderBook(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse getOrders = getActiveOrders(pair);
        if (getOrders.isPositive()) {
            apiResponse.setResponseObject(true);
            ArrayList<Order> orderList = (ArrayList) getOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                ApiResponse cancel = cancelOrder(thisOrder.getId(), thisOrder.getPair());
                if (!cancel.isPositive()) {
                    apiResponse.setResponseObject(false);
                }
            }
        } else {
            apiResponse = getOrders;
        }

        return apiResponse;
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
    public String query(String base, String method, AbstractMap<String, String> args, boolean needAuth, boolean isGet) {
        if (!exchange.getLiveData().isConnected()) {
            LOG.error("The bot will not execute the query, there is no connection to " + exchange.getName());
            return TOKEN_BAD_RETURN;
        }
        String query = service.executeQuery(base, method, args, needAuth, isGet);
        return query;
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
        this.apiBaseUrl = apiBaseUrl;
    }

    class BittrexService implements ServiceInterface {

        protected ApiKeys keys;

        public BittrexService(ApiKeys keys) {
            this.keys = keys;
        }

        @Override
        public String executeQuery(String base, String method, AbstractMap<String, String> args, boolean needAuth, boolean isGet) {
            HttpsURLConnection connection = null;
            boolean httpError = false;
            String output;
            int response = 200;
            String answer = null;
            URL queryUrl = null;
            String post_data = "";

            if (needAuth) {
                args.put("apikey", keys.getApiKey());
            }
            args.put("nonce", Long.toString(System.currentTimeMillis()));

            try {
                post_data = TradeUtils.buildQueryString(args, ENCODING);
                queryUrl = new URL(base + "/" + method + "?" + post_data);
            } catch (MalformedURLException mal) {
                LOG.error(mal.toString());
                return null;
            }

            try {
                connection = (HttpsURLConnection) queryUrl.openConnection();
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", Settings.APP_NAME);
                connection.setRequestProperty("Accept", "*/*");
                if (needAuth) {
                    connection.setRequestProperty("apisign", signRequest(keys.getPrivateKey(), queryUrl.toString()));
                }
                connection.setRequestProperty("Connection", "close");
                connection.setRequestProperty("Host", "exco.in");

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
                LOG.error(pe.toString());
                return answer;
            } catch (IOException io) {
                LOG.error((io.toString()));
                return answer;
            }

            BufferedReader br = null;
            try {
                if (connection.getResponseCode() >= 400) {
                    httpError = true;
                    response = connection.getResponseCode();
                    answer = "";
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                } else {
                    answer = "";
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
            } catch (IOException io) {
                LOG.error(io.toString());
                return answer;
            }

            if (httpError) {
                LOG.error("Query to : " + queryUrl
                        + "\nHTTP Response : " + Objects.toString(response));
            }

            try {
                while ((output = br.readLine()) != null) {
                    answer += output;
                }
            } catch (IOException io) {
                LOG.error(io.toString());
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
                LOG.error("Unsupported encoding exception: " + uee.toString());
            } catch (NoSuchAlgorithmException nsae) {
                LOG.error("No such algorithm exception: " + nsae.toString());
            } catch (InvalidKeyException ike) {
                LOG.error("Invalid key exception: " + ike.toString());
            }
            return sign;
        }
    }
}
