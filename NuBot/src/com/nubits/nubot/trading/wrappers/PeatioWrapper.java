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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiError;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.HttpUtils;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class PeatioWrapper implements TradeInterface {

    private static final Logger LOG = Logger.getLogger(PeatioWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final int SPACING_BETWEEN_CALLS = 1100;
    private final int TIME_OUT = 15000;
    private long lastSentTonce = 0L;
    private boolean apiBusy = false;
    private final String SIGN_HASH_FUNCTION = "HmacSHA256";
    private final String ENCODING = "UTF-8";
    private String apiBaseUrl;
    public String checkConnectionUrl;
    private final String API_GET_INFO = "/api/v2/members/me"; //GET
    private final String API_TRADE = "/api/v2/orders"; //POST
    private final String API_ACTIVE_ORDERS = "/api/v2/orders"; //GET
    private final String API_ORDER = "/api/v2/order"; //GET
    private final String API_CANCEL_ORDER = "/api/v2/order/delete"; //POST
    private final String API_CLEAR_ORDERS = "/api/v2/orders/clear"; //POST
    //For the ticker entry point, use getTicketPath(CurrencyPair pair)
    // Errors
    private ArrayList<ApiError> errors;
    private final String TOKEN_ERR = "error";
    private final int ERROR_UNKNOWN = 5560;
    private final int ERROR_NO_CONNECTION = 5561;
    private final int ERROR_GENERIC = 5562;
    private final int ERROR_PARSING = 5563;
    private final int ERROR_GET_INFO = 5564;
    private final int ERROR_TRADE = 5566;
    private final int ERROR_GET_ORDERS = 5567;
    private final int ERROR_GET_ORDERDETAIL = 5568;
    private final int ERROR_CANCEL_ORDER = 5569;

    public PeatioWrapper() {
        setupErrors();

    }

    public PeatioWrapper(ApiKeys keys, Exchange exchange, String api_base) {
        this.keys = keys;
        this.exchange = exchange;
        this.apiBaseUrl = api_base;
        this.checkConnectionUrl = api_base;
        setupErrors();

    }

    protected Long createNonce(String requester) {
        Long toReturn = 0L;
        if (!apiBusy) {
            toReturn = getNonceInternal(requester);
        } else {
            try {
                if (Global.options != null) {
                    if (Global.options.isVerbose()) {
                        LOG.info(System.currentTimeMillis() + " - Api is busy, I'll sleep and retry in a few ms (" + requester + ")");
                    }
                }
                Thread.sleep(Math.round(2.2 * SPACING_BETWEEN_CALLS));
                createNonce(requester);
            } catch (InterruptedException e) {
                LOG.severe(e.toString());
            }
        }
        return toReturn;
    }

    private void setupErrors() {
        errors = new ArrayList<ApiError>();
        errors.add(new ApiError(ERROR_NO_CONNECTION, "Failed to connect to the exchange entrypoint. Verify your connection"));
        errors.add(new ApiError(ERROR_GET_INFO, "Can't get_info"));
        errors.add(new ApiError(ERROR_GET_INFO, "Can't get ticker"));
        errors.add(new ApiError(ERROR_PARSING, "Parsing error"));
        errors.add(new ApiError(ERROR_TRADE, "Can't trade"));
        errors.add(new ApiError(ERROR_GET_ORDERS, "Can't get orders"));
        errors.add(new ApiError(ERROR_GET_ORDERDETAIL, "Can't get order detail"));
        errors.add(new ApiError(ERROR_CANCEL_ORDER, "Can't cancel order"));

    }

    private String getTickerPath(CurrencyPair pair) {
        return "api/v2/tickers/" + pair.toString();
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
        Balance balance = null;

        String path = API_GET_INFO;
        boolean isGet = true;
        TreeMap<String, String> query_args = new TreeMap<>();
        /*Params
         *
         */
        query_args.put("canonical_verb", "GET");
        query_args.put("canonical_uri", path);

        String queryResult = query(apiBaseUrl, path, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_GET_INFO));
            return apiResponse;
        }

        JSONObject response = JSON.parseObject(queryResult);

        if (response == null) {
            apiResponse.setError(new ApiError(3923, "response is null"));
            return apiResponse;
        }

        if (response.containsKey("error")) {
            JSONObject error = response.getJSONObject("error");
            int code = error.getInteger("code");
            String msg = error.getString("message");
            apiResponse.setError(new ApiError(code, msg));
            return apiResponse;
        }


        /*Sample result
         *{"sn":"PEA5TFFOGQHTIO","name":"foo","email":"foo@peatio.dev","activated":true,
         * "accounts":[
         *  {"currency":"cny","balance":"100243840.0","locked":"0.0"},
         *  {"currency":"btc","balance":"99999708.26","locked":"210.8"}
         *  ]
         * }
         */


        Amount NBTonOrder = null,
                NBTAvail = null,
                PEGonOrder = null,
                PEGAvail = null;

        JSONArray accounts = response.getJSONArray("accounts");
        if (currency == null) { //Get all balances
            for (int i = 0; i < accounts.size(); i++) {
                JSONObject balanceObj = accounts.getJSONObject(i);
                String tempCurrency = balanceObj.getString("currency");

                String nbtCurrencyCode = pair.getOrderCurrency().getCode();
                String pegCurrencyCode = pair.getPaymentCurrency().getCode();

                if (tempCurrency.equalsIgnoreCase(nbtCurrencyCode)) {
                    NBTAvail = new Amount(balanceObj.getDouble("balance"), pair.getOrderCurrency());
                    NBTonOrder = new Amount(balanceObj.getDouble("locked"), pair.getOrderCurrency());
                }
                if (tempCurrency.equalsIgnoreCase(pegCurrencyCode)) {
                    PEGAvail = new Amount(balanceObj.getDouble("balance"), pair.getPaymentCurrency());
                    PEGonOrder = new Amount(balanceObj.getDouble("locked"), pair.getPaymentCurrency());
                }
            }

            if (NBTAvail != null && NBTonOrder != null
                    && PEGAvail != null && PEGonOrder != null) {
                balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
                //Pack it into the ApiResponse
                apiResponse.setResponseObject(balance);
            } else {
                apiResponse.setError(getErrorByCode(ERROR_PARSING));
            }
        } else {//return available balance for the specific currency
            boolean found = false;
            Amount amount = null;
            for (int i = 0; i < accounts.size(); i++) {
                JSONObject balanceObj = accounts.getJSONObject(i);
                String tempCurrency = balanceObj.getString("currency");

                if (tempCurrency.equalsIgnoreCase(currency.getCode())) {
                    amount = new Amount(balanceObj.getDouble("balance") - balanceObj.getDouble("locked"), currency);
                    found = true;
                }
            }

            if (found) {
                apiResponse.setResponseObject(amount);
            } else {
                apiResponse.setError(new ApiError(21341, "Can't find balance for"
                        + " specified currency: " + currency.getCode()));
            }
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        Ticker ticker = new Ticker();
        ApiResponse apiResponse = new ApiResponse();

        double last = -1;
        double ask = -1;
        double bid = -1;

        String ticker_url = apiBaseUrl + getTickerPath(pair);
        String text = HttpUtils.getContentForGet(ticker_url, 5000);

        /*Sample result
         * {"at":1398410899,
         * "ticker":
         *  {
         *      "buy":"3000.0",
         *      "sell":"3100.0",
         *      "low":"3000.0",
         *      "high":"3000.0",
         *      "last":"3000.0",
         *      "vol":"0.11"}}
         */


        JSONObject jsonObject = JSONArray.parseObject(text);
        JSONObject tickerOBJ = jsonObject.getJSONObject("ticker");

        last = tickerOBJ.getDouble("last");
        ask = tickerOBJ.getDouble("buy");
        bid = tickerOBJ.getDouble("sell");

        ticker.setAsk(ask);
        ticker.setBid(bid);
        ticker.setLast(last);

        apiResponse.setResponseObject(ticker);
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

    public ApiResponse enterOrder(String type, CurrencyPair pair, double amount, double rate) {
        ApiResponse apiResponse = new ApiResponse();
        String order_id = "";
        String path = API_TRADE;
        boolean isGet = false;

        TreeMap<String, String> query_args = new TreeMap<>();

        query_args.put("side", type.toLowerCase());
        query_args.put("volume", Double.toString(amount));
        query_args.put("price", Double.toString(rate));
        query_args.put("market", pair.toString());
        query_args.put("canonical_verb", "POST");
        query_args.put("canonical_uri", path);

        String queryResult = query(apiBaseUrl, path, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_TRADE));
            return apiResponse;
        }

        JSONObject response = JSON.parseObject(queryResult);
        if (response == null) {
            apiResponse.setError(getErrorByCode(ERROR_TRADE));
            return apiResponse;
        } else if (response.containsKey("error")) {
            JSONObject error = response.getJSONObject("error");
            int code = error.getInteger("code");
            String msg = error.getString("message");
            apiResponse.setError(new ApiError(code, msg));
            return apiResponse;
        } else {
            //Correct
            if (response.containsKey("id")) {
                order_id = response.getLong("id").toString();
                apiResponse.setResponseObject(order_id);
            }
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders() {
        ApiError err = new ApiError(ERROR_GENERIC, "In Peatio API you should specify the CurrencyPair"
                + "\n use getActiveOrders(CurrencyPair pair)");
        return new ApiResponse(false, null, err);
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        ArrayList<Order> orderList = new ArrayList<Order>();

        String path = API_ACTIVE_ORDERS;
        boolean isGet = true;
        TreeMap<String, String> query_args = new TreeMap<>();

        /*Params
         * pair, default all pairs
         */

        query_args.put("canonical_verb", "GET");
        query_args.put("canonical_uri", path);
        query_args.put("market", pair.toString());
        query_args.put("limit", "999"); //default is 10 , max is 1000

        String queryResult = query(apiBaseUrl, path, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_GET_ORDERS));
            return apiResponse;
        }



        if (queryResult == null) {
            apiResponse.setError(new ApiError(3923, "response is null"));
            return apiResponse;
        } else if (queryResult.contains("error")) {
            apiResponse.setError(new ApiError(ERROR_GENERIC, queryResult));
            return apiResponse;
        }


        /*Sample result
         */
        JSONArray response = JSONObject.parseArray(queryResult);
        for (Object anOrdersResponse : response) {
            JSONObject orderResponse = (JSONObject) anOrdersResponse;
            Order tempOrder = parseOrder(orderResponse);
            if (!tempOrder.isCompleted()) //Do not add executed orders
            {
                orderList.add(tempOrder);
            }

        }

        apiResponse.setResponseObject(orderList);


        return apiResponse;
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        ApiResponse apiResponse = new ApiResponse();
        Order order = null;
        String path = API_ORDER;
        boolean isGet = true;


        TreeMap<String, String> query_args = new TreeMap<>();
        query_args.put("canonical_verb", "GET");
        query_args.put("canonical_uri", "/api/v2/order");
        query_args.put("id", orderID);

        String queryResult = query(apiBaseUrl, path, query_args, isGet);

        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_GET_ORDERDETAIL));
            return apiResponse;
        }

        JSONObject response = JSON.parseObject(queryResult);

        if (response == null) {
            apiResponse.setError(new ApiError(3923, "response is null"));
            return apiResponse;
        } else if (response.containsKey("error")) {
            JSONObject error = response.getJSONObject("error");
            int code = error.getInteger("code");
            String msg = error.getString("message");
            apiResponse.setError(new ApiError(code, msg));
            return apiResponse;
        }
        /*Sample result
         * {"id":7,"side":"sell","price":"3100.0","avg_price":"3101.2","state":"wait","market":"btccny","created_at":"2014-04-18T02:02:33Z","volume":"100.0","remaining_volume":"89.8","executed_volume":"10.2","trades":[{"id":2,"price":"3100.0","volume":"10.2","market":"btccny","created_at":"2014-04-18T02:04:49Z","side":"sell"}]}
         */
        apiResponse.setResponseObject(parseOrder(response));

        return apiResponse;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String path = API_CANCEL_ORDER;
        boolean isGet = false;

        TreeMap<String, String> query_args = new TreeMap<>();

        query_args.put("id", orderID);
        query_args.put("canonical_verb", "POST");
        query_args.put("canonical_uri", path);

        String queryResult = query(apiBaseUrl, path, query_args, isGet);


        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_CANCEL_ORDER));
            return apiResponse;
        }

        JSONObject response = JSON.parseObject(queryResult);

        if (response == null) {
            apiResponse.setError(new ApiError(3923, "response is null"));
            return apiResponse;
        } else if (response.containsKey("error")) {
            JSONObject error = response.getJSONObject("error");
            int code = error.getInteger("code");
            String msg = error.getString("message");
            apiResponse.setError(new ApiError(code, msg));
            return apiResponse;
        }
        /*Sample result
         * Cancel order is an asynchronous operation. A success response only means your cancel
         * request has been accepted, it doesn't mean the order has been cancelled.
         * You should always use /api/v2/order or websocket api to get order's latest state.
         */

        apiResponse.setResponseObject(true);
        return apiResponse;

    }

    @Override
    public ApiResponse isOrderActive(String id) {
        boolean exists = false;
        ApiResponse existResponse = new ApiResponse();

        ApiResponse orderDetailResponse = getOrderDetail(id);
        if (orderDetailResponse.isPositive()) {
            Order order = (Order) orderDetailResponse.getResponseObject();
            if (order.isCompleted()) {
                exists = false;
            } else {
                exists = true;
            }
            existResponse.setResponseObject(exists);
        } else {
            ApiError err = orderDetailResponse.getError();
            if (err.getCode() == 2004) {
                exists = false; //Order has been canceled or is already completed
                existResponse.setResponseObject(exists);
            } else {
                existResponse.setError(err);
                LOG.severe(existResponse.getError().toString());
            }
        }

        return existResponse;
    }

    @Override
    public ApiResponse getTxFee() {
        return getTxFeeImpl();
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        return getTxFeeImpl();
    }

    private ApiResponse getTxFeeImpl() {
        double defaultFee = 0.2;

        if (Global.options != null) {
            return new ApiResponse(true, Global.options.getTxFee(), null);
        } else {
            return new ApiResponse(true, defaultFee, null);
        }
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
    public String query(String url, TreeMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String query(String base, String method, TreeMap<String, String> args, boolean isGet) {
        PeatioService query = new PeatioService(base, method, args, keys);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, isGet);

        } else {
            LOG.severe("The bot will not execute the query, there is no connection to Peatio");
            queryResult = "error : no connection with peatio";
        }
        return queryResult;
    }

    private Order parseOrder(JSONObject jsonObject) {
        Order order = new Order();
        String status = jsonObject.getString("state");

        boolean executed = false;
        switch (status) {
            case "wait": {
                executed = false;
                break;
            }
            case "done": {
                executed = true;
                break;
            }
            case "cancel": {
                executed = true;
                break;
            }
        }


        //Create a CurrencyPair object
        CurrencyPair cp = CurrencyPair.getCurrencyPairFromString(jsonObject.getString("market"), "");

        order.setPair(cp);
        order.setCompleted(executed);
        order.setId("" + jsonObject.getLong("id"));
        order.setAmount(new Amount(jsonObject.getDouble("remaining_volume"), cp.getOrderCurrency()));
        order.setPrice(new Amount(jsonObject.getDouble("price"), cp.getPaymentCurrency()));

        order.setInsertedDate(parseDate(jsonObject.getString("created_at")));

        order.setType(jsonObject.getString("side"));
        //Created at?

        return order;

    }

    @Override
    public String query(String url, HashMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    private Date parseDate(String dateStr) {
        Date toRet = null;
        //Parse the date
        //Sample 2014-08-19T10:23:49+01:00

        //Remove the Timezone
        dateStr = dateStr.substring(0, dateStr.length() - 6);
        String datePattern = "yyyy-MM-dd'T'HH:mm:ss";
        DateFormat df = new SimpleDateFormat(datePattern, Locale.ENGLISH);
        try {
            toRet = df.parse(dateStr);
        } catch (ParseException ex) {
            LOG.severe(ex.toString());
            toRet = new Date();
        }
        return toRet;
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String path = API_CLEAR_ORDERS;
        boolean isGet = false;

        TreeMap<String, String> query_args = new TreeMap<>();

        query_args.put("canonical_verb", "POST");
        query_args.put("canonical_uri", path);

        String queryResult = query(apiBaseUrl, path, query_args, isGet);


        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_CANCEL_ORDER));
            return apiResponse;
        }



        /*Sample result
         * Cancel order is an asynchronous operation. A success response only means your cancel
         * request has been accepted, it doesn't mean the order has been cancelled.
         * You should always use /api/v2/order or websocket api to get order's latest state.
         */

        apiResponse.setResponseObject(true);
        return apiResponse;


    }

    //DO NOT USE THIS METHOD DIRECTLY, use CREATENONCE
    private long getNonceInternal(String requester) {
        apiBusy = true;
        long currentTime = System.currentTimeMillis();
        if (Global.options != null) {
            if (Global.options.isVerbose()) {
                LOG.info(currentTime + " Now apiBusy! req : " + requester);
            }
        }
        long timeElapsedSinceLastCall = currentTime - lastSentTonce;
        if (timeElapsedSinceLastCall < SPACING_BETWEEN_CALLS) {
            try {
                long sleepTime = SPACING_BETWEEN_CALLS;
                Thread.sleep(sleepTime);
                currentTime = System.currentTimeMillis();
                if (Global.options != null) {
                    if (Global.options.isVerbose()) {
                        LOG.info("Just slept " + sleepTime + "; req : " + requester);
                    }
                }
            } catch (InterruptedException e) {
                LOG.severe(e.toString());
            }
        }

        lastSentTonce = currentTime;
        if (Global.options != null) {
            if (Global.options.isVerbose()) {
                LOG.info("Final tonce to be sent: req : " + requester + " ; Tonce=" + lastSentTonce);
            }
        }
        apiBusy = false;
        return lastSentTonce;
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
        this.checkConnectionUrl = apiBaseUrl;

    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    private class PeatioService implements ServiceInterface {

        protected String base;
        protected String method;
        protected TreeMap args;
        protected ApiKeys keys;
        protected String url;

        public PeatioService(String base, String method, TreeMap<String, String> args, ApiKeys keys) {
            this.base = base;
            this.method = method;
            this.args = args;
            this.keys = keys;

        }

        private PeatioService(String url, TreeMap<String, String> args) {
            //Used for ticker, does not require auth
            this.url = url;
            this.args = args;
            this.method = "";

        }

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {


            args.put("access_key", keys.getApiKey());

            String messageDbg = (String) args.get("canonical_verb") + " " + (String) args.get("canonical_uri");
            args.put("tonce", createNonce(messageDbg).toString());

            args.put("signature", getSign(args));

            String canonical_verb = (String) args.get("canonical_verb");
            args.remove("canonical_verb");
            String canonical_uri = (String) args.get("canonical_uri");
            args.remove("canonical_uri");
            LOG.fine("Calling " + canonical_uri + " with params:" + args);
            Document doc;
            String response = null;
            try {
                String url = apiBaseUrl + canonical_uri;
                Connection connection = HttpUtils.getConnectionForPost(url, args).timeout(TIME_OUT);


                connection.ignoreHttpErrors(true);
                if ("post".equalsIgnoreCase(canonical_verb)) {
                    doc = connection.ignoreContentType(true).post();
                } else {
                    doc = connection.ignoreContentType(true).get();
                }
                response = doc.body().text();

                return response;
            } catch (Exception e) {
                LOG.severe(e.toString());
                return null;
            } finally {
                LOG.fine("result:{}" + response);
            }
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            throw new UnsupportedOperationException("Use getSign(TreeMap<String, String> parameters"); //TODO change body of generated methods, choose Tools | Templates.
        }

        private String getSign(TreeMap<String, String> parameters) {
            if (parameters.containsKey("signature")) {
                parameters.remove("signature");
            }

            StringBuilder parameter = new StringBuilder();
            for (Map.Entry entry : parameters.entrySet()) {
                if (entry.getKey().equals("canonical_verb") || entry.getKey().equals("canonical_uri")) {
                    continue;
                }

                parameter.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            if (parameter.length() > 0) {
                parameter.deleteCharAt(0);
            }
            String canonical_verb = parameters.get("canonical_verb");
            String canonical_uri = parameters.get("canonical_uri");

            String signStr = String.format("%s|%s|%s", canonical_verb, canonical_uri, parameter.toString());
            try {
                Mac mac = Mac.getInstance(SIGN_HASH_FUNCTION);
                SecretKeySpec keyspec = new SecretKeySpec(keys.getPrivateKey().getBytes(ENCODING), SIGN_HASH_FUNCTION);
                mac.init(keyspec);
                mac.update(signStr.getBytes(ENCODING));
                return String.format("%064x", new BigInteger(1, mac.doFinal()));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
