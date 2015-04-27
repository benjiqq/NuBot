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
import com.nubits.nubot.exchanges.ExchangeFacade;
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
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class CcedkWrapper implements TradeInterface {

    //Class fields
    private ApiKeys keys;
    protected CcedkService service;
    private Exchange exchange;
    private final int SPACING_BETWEEN_CALLS = 1100;
    private final int TIME_OUT = 15000;
    private long lastSentTonce = 0L;
    private static int offset = -1000000000;
    private String checkConnectionUrl = "http://www.ccedk.com";
    private boolean apiBusy = false;
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private final String API_BASE_URL = "https://www.ccedk.com/api/v1/";
    private final String API_GET_INFO = "balance/list"; //post
    private final String API_TRADE = "order/new"; //post
    private final String API_GET_TRADES = "trade/list"; //post
    private final String API_ACTIVE_ORDERS = "order/list";
    private final String API_ORDER = "order/info";
    private final String API_CANCEL_ORDER = "order/cancel";
    private final String API_LAST_PRICE = "stats/marketdepthbtcav";
    //For the ticker entry point, use getTicketPath(CurrencyPair pair)
    // Errors
    private ErrorManager errors = new ErrorManager();
    private final String TOKEN_ERR = "errors";
    private final String TOKEN_BAD_RETURN = "No Connection With Exchange";
    private static final Logger LOG = LoggerFactory.getLogger(CcedkWrapper.class.getName());
    private static final String INVALID_NONCE_ERROR = "Invalid Nonce value detected";
    private static final int ROUND_CUTOFF = 99;
    private static int INVALID_NONCE_COUNT = 1;

    public CcedkWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        service = new CcedkService(keys);

        setupErrors();

    }

    public String createNonce(String requester) {
        //This is a  workaround waiting for clarifications from CCEDK team
        int lastdigit;
        String validNonce;
        int numericalNonce;
        String startvalid = " till";
        int indexStart;
        int upperEdge;
        String nonceError;
        if (offset == -1000000000) {
            JSONParser parser = new JSONParser();
            try {
                String htmlString = Utils.getHTML("https://www.ccedk.com/api/v1/currency/list?nonce=1234567891", false);
                //{"errors":{"nonce":"incorrect range `nonce`=`1234567891`, must be from `1411036100` till `1411036141`"}
                JSONObject httpAnswerJson = (JSONObject) (parser.parse(htmlString));
                JSONObject errors = (JSONObject) httpAnswerJson.get("errors");
                nonceError = (String) errors.get("nonce");
                indexStart = nonceError.lastIndexOf(startvalid) + startvalid.length() + 2;
                upperEdge = Integer.parseInt(nonceError.substring(indexStart, indexStart + 10));
                offset = upperEdge - (int) (System.currentTimeMillis() / 1000L);
            } catch (ParseException ex) {
                LOG.error(ex.toString());
            } catch (IOException io) {
                LOG.error(io.toString());
            }
        }
        if (offset != -1000000000) {
            numericalNonce = (int) (System.currentTimeMillis() / 1000L) + offset;

            validNonce = Objects.toString(numericalNonce);
            //LOG.warn("validNonce = " + validNonce);
        } else {
            LOG.error("Error calculating nonce");
            validNonce = "1234567891";
        }
        return validNonce;
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
        if (queryResult.contains(INVALID_NONCE_ERROR)) {
            ApiError error = errors.genericError;
            error.setDescription(queryResult);
            apiResponse.setError(error);
            if (INVALID_NONCE_COUNT < 5) {
                getQuery(url, method, query_args, needAuth, isGet);
                INVALID_NONCE_COUNT++;
            }
            return apiResponse;
        }

        INVALID_NONCE_COUNT = 1;

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean hasErrors;
            try {
                hasErrors = (boolean) httpAnswerJson.get(TOKEN_ERR);
            } catch (ClassCastException e) {
                hasErrors = true;
            }

            if (hasErrors) {
                JSONObject errorMessage = (JSONObject) httpAnswerJson.get(TOKEN_ERR);
                ApiError apiErr = errors.apiReturnError;
                apiErr.setDescription(errorMessage.toJSONString());
                LOG.error("Ccedk API returned an error: " + errorMessage);
                apiResponse.setError(apiErr);
            } else {
                apiResponse.setResponseObject(httpAnswerJson);
            }
        } catch (ClassCastException cce) {
            //if casting to a JSON object failed, try a JSON Array
            try {
                JSONArray httpAnswerJson = (JSONArray) (parser.parse(queryResult));
                apiResponse.setResponseObject(httpAnswerJson);
            } catch (ParseException pe) {
                LOG.error("httpResponse: " + queryResult + " \n" + pe.toString());
                apiResponse.setError(errors.parseError);
            }
        } catch (ParseException ex) {
            LOG.error("httpresponse: " + queryResult + " \n" + ex.toString());
            apiResponse.setError(errors.parseError);
            return apiResponse;
        }
        return apiResponse;
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

        String url = API_BASE_URL;
        String method = API_GET_INFO;
        boolean isGet = false;
        HashMap<String, String> query_args = new HashMap<>();


        ApiResponse response = getQuery(url, method, query_args, true, isGet);

        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
            JSONArray entities = (JSONArray) dataJson.get("entities");

            if (currency == null) { //Get all balances
                long NBTid = TradeUtilsCCEDK.getCCDKECurrencyId(pair.getOrderCurrency().getCode().toUpperCase());
                long PEGid = TradeUtilsCCEDK.getCCDKECurrencyId(pair.getPaymentCurrency().getCode().toUpperCase());
                Amount NBTTotal = new Amount(0, pair.getOrderCurrency());
                Amount PEGTotal = new Amount(0, pair.getPaymentCurrency());

                for (Iterator<JSONObject> entity = entities.iterator(); entity.hasNext(); ) {
                    JSONObject thisEntity = entity.next();
                    long entityId = (Long) thisEntity.get("currency_id");
                    if (entityId == NBTid) {
                        NBTTotal.setQuantity(Utils.getDouble(thisEntity.get("balance")));
                    }
                    if (entityId == PEGid) {
                        PEGTotal.setQuantity(Utils.getDouble(thisEntity.get("balance")));
                    }
                }
                PairBalance balance = new PairBalance(NBTTotal, PEGTotal);
                apiResponse.setResponseObject(balance);

            } else { //Specific currency requested
                long id = TradeUtilsCCEDK.getCCDKECurrencyId(currency.getCode().toUpperCase());
                Amount total = new Amount(0, currency);

                for (Iterator<JSONObject> entity = entities.iterator(); entity.hasNext(); ) {
                    JSONObject thisEntity = entity.next();
                    long entityId = (Long) thisEntity.get("currency_id");
                    if (entityId == id) {
                        total.setQuantity(Utils.getDouble(thisEntity.get("balance")));
                    }
                }
                apiResponse.setResponseObject(total);
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
        String method = API_LAST_PRICE;
        HashMap<String, String> args = new HashMap<>();
        boolean needAuth = false;
        boolean isGet = true;

        int pairId = TradeUtilsCCEDK.getCCDKECurrencyPairId(pair);
        args.put("pair_id", Integer.toString(pairId));

        ApiResponse response = getQuery(url, method, args, needAuth, isGet);

        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
            JSONObject entity = (JSONObject) dataJson.get("entity");

            Ticker ticker = new Ticker(0, 0, 0);

            ticker.setAsk(Utils.getDouble(entity.get("min_ask")));
            ticker.setBid(Utils.getDouble(entity.get("max_bid")));
            ticker.setLast(Utils.getDouble(entity.get("last_price")));

            apiResponse.setResponseObject(ticker);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse sell(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.SELL.toLowerCase(), pair, amount, rate);
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.BUY.toLowerCase(), pair, amount, rate);
    }

    private ApiResponse enterOrder(String type, CurrencyPair pair, double amount, double price) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = API_TRADE;
        boolean isGet = false;
        boolean needAuth = true;
        HashMap<String, String> query_args = new HashMap<>();
        query_args.put("pair_id", Integer.toString(TradeUtilsCCEDK.getCCDKECurrencyPairId(pair)));
        query_args.put("type", type);
        query_args.put("price", Double.toString(price));
        query_args.put("amount", Double.toString(amount));

        ApiResponse response = getQuery(url, method, query_args, needAuth, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
            JSONObject entity = (JSONObject) dataJson.get("entity");
            Long order_id = (Long) entity.get("order_id");
            apiResponse.setResponseObject(Long.toString(order_id));
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
        ArrayList<Order> orderList = new ArrayList<Order>();
        String url = API_BASE_URL;
        String method = API_ACTIVE_ORDERS;
        boolean isGet = false;
        ;
        HashMap<String, String> query_args = new HashMap<>();

        if (pair != null) {
            String pair_id = Integer.toString(TradeUtilsCCEDK.getCCDKECurrencyPairId(pair));
            query_args.put("pair_id", pair_id);
        }

        ApiResponse response = getQuery(url, method, query_args, true, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
            JSONArray entities = (JSONArray) dataJson.get("entities");

            for (Iterator<JSONObject> entity = entities.iterator(); entity.hasNext(); ) {
                JSONObject orderObject = entity.next();
                Order tempOrder = parseOrder(orderObject);

                if (!tempOrder.isCompleted()) //Do not add executed orders
                {
                    orderList.add(tempOrder);
                }
            }
            apiResponse.setResponseObject(orderList);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse getActiveOrders = getActiveOrders();
        boolean found = false;
        if (getActiveOrders.isPositive()) {
            apiResponse.setResponseObject(false);
            ArrayList<Order> orderList = (ArrayList) getActiveOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(orderID)) {
                    apiResponse.setResponseObject(thisOrder);
                    found = true;
                    break;
                }
            }
            if (!found)
                apiResponse.setError(errors.orderNotFound);
        } else {
            apiResponse = getActiveOrders;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = API_CANCEL_ORDER;
        boolean isGet = false;
        boolean needAuth = true;
        HashMap<String, String> query_args = new HashMap<>();
        query_args.put("order_id", orderID);

        ApiResponse response = getQuery(url, method, query_args, needAuth, isGet);
        if (response.isPositive()) {
            apiResponse.setResponseObject(true);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee() {

        return new ApiResponse(true, Global.options.getTxFee(), null);

    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {

        LOG.warn("CCEDK uses global TX fee, currency pair not supported."
                + "now calling getTxFee()");
        return getTxFee();
    }

    @Override
    public ApiResponse isOrderActive(String orderID) {
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse getActiveOrders = getActiveOrders();
        if (getActiveOrders.isPositive()) {
            ArrayList<Order> orderList = (ArrayList) getActiveOrders.getResponseObject();
            apiResponse.setResponseObject(false);
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(orderID)) {
                    apiResponse.setResponseObject(true);
                }
            }
        } else {
            apiResponse = getActiveOrders;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
        //Since there is no API entry point for that, this call will iterate over active
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse activeOrdersResponse = getActiveOrders();
        if (activeOrdersResponse.isPositive()) {
            apiResponse.setResponseObject(true);
            ArrayList<Order> orderList = (ArrayList) activeOrdersResponse.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                if (!pair.equals(thisOrder.getPair())) {
                    continue;
                }
                ApiResponse deleteOrderResponse = cancelOrder(thisOrder.getId(), null);
                if (deleteOrderResponse.isPositive()) {
                    apiResponse.setResponseObject(false);
                }
            }
        } else {
            apiResponse = activeOrdersResponse;
        }

        return apiResponse;
    }

    @Override
    public ApiError getErrorByCode(int code) {
        return null;
    }

    @Override
    public String getUrlConnectionCheck() {
        return checkConnectionUrl;
    }

    @Override
    public String query(String base, String method, AbstractMap<String, String> args, boolean needAuth, boolean isGet) {
        String queryResult;
        if (exchange.getLiveData().isConnected()) {
            queryResult = service.executeQuery(base, method, args, needAuth, isGet);
        } else {
            LOG.error("The bot will not execute the query, there is no connection to ccdek");
            queryResult = TOKEN_BAD_RETURN;
        }
        return queryResult;
    }

    private Order parseOrder(JSONObject orderObject) {
        Order order = new Order();

        /*
         * "order_id":"1617","pair_id":"3",
         * "type":"sell","volume":"48.00000000",
         * "price":"9.50000000","fee":"0. 91200000",
         * "active":"1","created":"1406098537"}
         */

        order.setId(Long.toString((Long) orderObject.get("order_id")));

        long currencyPairID = (Long) orderObject.get("pair_id");
        CurrencyPair cp = TradeUtilsCCEDK.getCCEDKPairFromID(currencyPairID);
        order.setPair(cp);


        order.setType(orderObject.get("type").toString());

        order.setAmount(new Amount(Utils.getDouble(orderObject.get("volume")), cp.getOrderCurrency()));
        order.setPrice(new Amount(Utils.getDouble(orderObject.get("price")), cp.getPaymentCurrency()));

        long active = (Long) orderObject.get("active");
        if (active == 0) {
            order.setCompleted(true);
        } else {
            order.setCompleted(false);
        }

        long created = (Long) orderObject.get("created") * 1000;
        order.setInsertedDate(new Date(created));

        return order;
    }

    private Trade parseTrade(JSONObject orderObject) {
        //hotfix for Ben request
        //FileSystem.writeToFile(orderObject.toJSONString(), "raw-trades.json", true);

        Trade trade = new Trade();

        /*
         "id":<\d+> AS "trade_id",
         "pair_id":<\d+>,
         "type":<buy|sell>,
         "is_buyer":<0|1>,
         "is_seller":<0|1>,
         "price":<\d{1,8}\.d{0,8}>,
         "volume":<\d{1,8}\.d{0,8}>,
         "fee":<\d{1,8}\.d{0,8}>,
         "created":<\d{10}>
         */
        trade.setId((String) orderObject.get("trade_id"));

        trade.setExchangeName(ExchangeFacade.CCEDK);
        int currencyPairID = Integer.parseInt((String) orderObject.get("pair_id"));
        CurrencyPair cp = TradeUtilsCCEDK.getCCEDKPairFromID(currencyPairID);
        trade.setPair(cp);

        trade.setType(orderObject.get("is_seller").equals("1") ? Constant.SELL : Constant.BUY);
        trade.setAmount(new Amount(Double.parseDouble((String) orderObject.get("volume")), cp.getOrderCurrency()));
        trade.setPrice(new Amount(Double.parseDouble((String) orderObject.get("price")), cp.getPaymentCurrency()));
        trade.setFee(new Amount(Double.parseDouble((String) orderObject.get("fee")), cp.getPaymentCurrency()));

        long date = Long.parseLong(((String) orderObject.get("created")) + "000");
        trade.setDate(new Date(date));

        return trade;
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
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair) {
        return getLastTradesImpl(pair, 0);
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        return getLastTradesImpl(pair, startTime);
    }

    public ApiResponse getLastTradesImpl(CurrencyPair pair, long startTime) {
        ApiResponse apiResponse = new ApiResponse();
        ArrayList<Trade> tradesList = new ArrayList<Trade>();
        String url = API_BASE_URL;
        String method = API_GET_TRADES;
        boolean isGet = false;
        HashMap<String, String> query_args = new HashMap<>();
        String pair_id = Integer.toString(TradeUtilsCCEDK.getCCDKECurrencyPairId(pair));

        String startDateArg;
        if (startTime == 0) {
            long now = System.currentTimeMillis();
            long yesterday = now - Utils.getOneDayInMillis();
            startDateArg = Long.toString(yesterday); //24hours
        } else {
            startDateArg = Long.toString(startTime);
        }

        startDateArg = startDateArg.substring(0, 10);

        query_args.put("date_from", startDateArg);
        query_args.put("items_per_page", "100");

        ApiResponse response = getQuery(url, method, query_args, true, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
            JSONArray entities;
            try {
                entities = (JSONArray) dataJson.get("entities");
            } catch (ClassCastException e) { //Empty order list returns {"errors":false,"response":{"entities":false},
                apiResponse.setResponseObject(tradesList);
                return apiResponse;
            }
            for (int i = 0; i < entities.size(); i++) {
                JSONObject tradeObject = (JSONObject) entities.get(i);
                Trade tempTrade = parseTrade(tradeObject);
                tradesList.add(tempTrade);

            }
            apiResponse.setResponseObject(tradesList);
        } else {
            apiResponse = response;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getOrderBook(CurrencyPair pair) {
        throw new UnsupportedOperationException("CcedkWrapper.getOrderBook() not implemented yet.");
    }

    private class CcedkService implements ServiceInterface {

        protected ApiKeys keys;

        //Parameters used to repeat an API call in case of wrong nonce error
        protected final int MAX_NUMBER_ATTEMPTS = 3;
        protected int wrongNonceCounter;
        protected String adjustedNonce;

        public CcedkService(ApiKeys keys) {

            this.keys = keys;
            this.wrongNonceCounter = 0;

        }

        private CcedkService() {
            //Used for ticker, does not require auth
            this.wrongNonceCounter = 0;
        }

        @Override
        public String executeQuery(String base, String method, AbstractMap<String, String> args, boolean needAuth, boolean isGet) {

            String answer = "";
            String signature = "";
            String post_data;
            boolean httpError = false;
            HttpsURLConnection connection = null;

            try {
                // add nonce and build arg list
                if (needAuth) {
                    args.put("nonce", Long.toString(System.currentTimeMillis()));
                    post_data = TradeUtils.buildQueryString(args, ENCODING);
                    String toHash = post_data;
                    signature = signRequest(keys.getPrivateKey(), toHash);
                } else {
                    post_data = TradeUtils.buildQueryString(args, ENCODING);
                }
                // build URL

                URL queryUrl;
                if (isGet) {
                    queryUrl = new URL(base + method + "?" + post_data);
                } else {
                    queryUrl = new URL(base + method);
                }
                LOG.trace(queryUrl.toString());


                connection = (HttpsURLConnection) queryUrl.openConnection();
                if (isGet) {
                    connection.setRequestMethod("GET");
                } else {
                    connection.setRequestMethod("POST");
                }

                // create and setup a HTTP connection

                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; CCEDK PHP client; " + Settings.APP_NAME);

                if (needAuth) {
                    connection.setRequestProperty("Key", keys.getApiKey());
                    connection.setRequestProperty("Sign", signature);
                }

                if (!isGet) {
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(post_data);
                    os.close();
                }

                //Read the response


                BufferedReader br;
                if (connection.getResponseCode() >= 400) {
                    LOG.info(connection.getRequestMethod() + " query to :" + base + " (method=" + method + ")" + " , HTTP response : " + connection.getResponseCode());
                    httpError = true;
                    br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
                } else {
                    br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                }

                String output;

                while ((output = br.readLine()) != null) {
                    LOG.debug(output);
                    answer += output;
                }

                if (httpError) {
                    LOG.error("Http error - Post Data: " + post_data);
                    JSONParser parser = new JSONParser();
                    try {
                        JSONObject obj2 = (JSONObject) (parser.parse(answer));
                        answer = (String) obj2.get(TOKEN_ERR);

                    } catch (ParseException ex) {
                        LOG.error(ex.toString());

                    }
                }
            } //Capture Exceptions
            catch (IllegalStateException ex) {
                LOG.error(ex.toString());
                return null;
            } catch (NoRouteToHostException | UnknownHostException ex) {
                //Global.BtceExchange.setConnected(false);
                LOG.error(ex.toString());

                answer = TOKEN_BAD_RETURN;
            } catch (IOException ex) {
                LOG.error(ex.toString());
                return null;
            } finally {
                //close the connection, set all objects to null
                connection.disconnect();
                connection = null;
            }

            return answer;
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            String signature = "";

            Mac mac;
            SecretKeySpec key = null;

            // Create a new secret key
            try {
                key = new SecretKeySpec(secret.getBytes(ENCODING), SIGN_HASH_FUNCTION);
            } catch (UnsupportedEncodingException uee) {
                LOG.error("Unsupported encoding exception: " + uee.toString());
                return null;
            }

            // Create a new mac
            try {
                mac = Mac.getInstance(SIGN_HASH_FUNCTION);
            } catch (NoSuchAlgorithmException nsae) {
                LOG.error("No such algorithm exception: " + nsae.toString());
                return null;
            }

            // Init mac with key.
            try {
                mac.init(key);
            } catch (InvalidKeyException ike) {
                LOG.error("Invalid key exception: " + ike.toString());
                return null;
            }
            try {
                signature = Hex.encodeHexString(mac.doFinal(hash_data.getBytes(ENCODING)));

            } catch (UnsupportedEncodingException ex) {
                LOG.error(ex.toString());
            }
            return signature;
        }
    }
}
