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

/**
 *
 * @author desrever <desrever at nubits.com>
 */
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
import com.nubits.nubot.models.Trade;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.Utils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PoloniexWrapper implements TradeInterface {

    private static final Logger LOG = Logger.getLogger(PoloniexWrapper.class.getName());
    private ApiKeys keys;
    private Exchange exchange;
    private String checkConnectionUrl = "http://poloniex.com/";
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    //Entry points
    private final String API_BASE_URL = "https://poloniex.com/tradingApi";
    private final String API_GET_BALANCES = "returnCompleteBalances";
    private final String API_GET_ORDERS = "returnOpenOrders";
    private final String API_GET_TRADES = "returnTradeHistory";
    private final String API_SELL = "sell";
    private final String API_BUY = "buy";
    private final String API_CANCEL_ORDER = "cancelOrder";
    //Errors
    private ArrayList<ApiError> errors;
    private final String TOKEN_ERR = "error";
    private final int ERROR_NO_CONNECTION = 15561;
    private final int ERROR_GENERIC = 15562;
    private final int ERROR_PARSING = 15563;
    private final int ERROR_UNKNOWN = 15560;

    public PoloniexWrapper() {
        setupErrors();

    }

    public PoloniexWrapper(ApiKeys keys, Exchange exchange) {
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
        return getBalanceImpl(pair, null);
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return getBalanceImpl(null, currency);
    }

    private ApiResponse getBalanceImpl(CurrencyPair pair, Currency currency) {
        //Swap the pair for the request
        ApiResponse apiResponse = new ApiResponse();
        Balance balance = new Balance();

        String base = API_BASE_URL;
        String command = API_GET_BALANCES;
        HashMap<String, String> query_args = new HashMap<>();
        /*Params
         */


        /*Sample result
         *{"LTC":
         *  {"available":"5.015","onOrders":"1.0025","btcValue":"0.078"},
         * "NXT:{...} ... }
         */

        String queryResult = query(base, command, query_args, false);
        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = true;
            if (httpAnswerJson.containsKey("error")) {
                valid = false;
            }

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("error");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);
                //LOG.severe("Poloniex API returned an error: " + errorMessage);
                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct
                if (currency != null) {
                    //looking for a specific currency
                    String lookingFor = currency.getCode().toUpperCase();
                    if (httpAnswerJson.containsKey(lookingFor)) {
                        JSONObject balanceJSON = (JSONObject) httpAnswerJson.get(lookingFor);
                        double balanceD = Utils.getDouble(balanceJSON.get("available"));
                        apiResponse.setResponseObject(new Amount(balanceD, currency));
                    } else {
                        String errorMessage = "Cannot find a balance for currency " + lookingFor;
                        ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);
                        apiResponse.setError(apiErr);
                        return apiResponse;
                    }

                } else {
                    //get all balances for the pair
                    boolean foundNBTavail = false;
                    boolean foundPEGavail = false;

                    Amount NBTAvail = new Amount(0, pair.getOrderCurrency()),
                            PEGAvail = new Amount(0, pair.getPaymentCurrency());

                    Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency());
                    Amount NBTonOrder = new Amount(0, pair.getOrderCurrency());

                    String NBTcode = pair.getOrderCurrency().getCode().toUpperCase();
                    String PEGcode = pair.getPaymentCurrency().getCode().toUpperCase();


                    if (httpAnswerJson.containsKey(NBTcode)) {
                        JSONObject balanceJSON = (JSONObject) httpAnswerJson.get(NBTcode);
                        double tempAvailablebalance = Utils.getDouble(balanceJSON.get("available"));
                        double tempLockedebalance = Utils.getDouble(balanceJSON.get("onOrders"));

                        NBTAvail = new Amount(tempAvailablebalance, pair.getOrderCurrency());
                        NBTonOrder = new Amount(tempLockedebalance, pair.getOrderCurrency());

                        foundNBTavail = true;
                    }

                    if (httpAnswerJson.containsKey(PEGcode)) {
                        JSONObject balanceJSON = (JSONObject) httpAnswerJson.get(PEGcode);
                        double tempAvailablebalance = Utils.getDouble(balanceJSON.get("available"));
                        double tempLockedebalance = Utils.getDouble(balanceJSON.get("onOrders"));

                        PEGAvail = new Amount(tempAvailablebalance, pair.getPaymentCurrency());
                        PEGonOrder = new Amount(tempLockedebalance, pair.getPaymentCurrency());

                        foundPEGavail = true;
                    }


                    balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
                    apiResponse.setResponseObject(balance);
                    if (!foundNBTavail || !foundPEGavail) {
                        LOG.warning("Cannot find a balance for currency with code "
                                + "" + NBTcode + " or " + PEGcode + " in your balance. "
                                + "NuBot assumes that balance is 0");
                    }
                }

            }
        } catch (ParseException ex) {
            LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }

        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
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

        String base = API_BASE_URL;
        String command;
        if (type.equals(Constant.SELL)) {
            command = API_SELL;
        } else {
            command = API_BUY;
        }

        HashMap<String, String> query_args = new HashMap<>();

        //Swap the pair for the request
        pair = CurrencyPair.swap(pair);
        /*Params
         */
        query_args.put("currencyPair", pair.toString("_").toUpperCase());
        query_args.put("amount", Double.toString(amount));
        query_args.put("rate", Double.toString(rate));


        String queryResult = query(base, command, query_args, false);

        /*Sample result
         *{"orderNumber":31226040,"resultingTrades":[{"amount":"338.8732","date":"2014-10-18 23:03:21","rate":"0.00000173","total":"0.00058625","tradeID":"16164","type":"buy"}]}
         */

        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = true;
            if (httpAnswerJson.containsKey("error")) {
                valid = false;
            }

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("error");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);
                //LOG.severe("Poloniex API returned an error: " + errorMessage);
                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct
                String order_id = (String) httpAnswerJson.get("orderNumber");
                apiResponse.setResponseObject(order_id);
            }
        } catch (ParseException ex) {
            LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders() {
        return getOrdersImpl(null);
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        return getOrdersImpl(pair);
    }

    private ApiResponse getOrdersImpl(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        ArrayList<Order> orderList = new ArrayList<Order>();

        String base = API_BASE_URL;
        String command = API_GET_ORDERS;
        HashMap<String, String> query_args = new HashMap<>();

        String pairString = "all";
        if (pair != null) {
            pair = CurrencyPair.swap(pair);
            pairString = pair.toString("_").toUpperCase();
        }

        query_args.put("currencyPair", pairString);


        String queryResult = query(base, command, query_args, false);
        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        boolean valid = true;
        boolean isArray = false;
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
        } catch (ClassCastException ex) {
            isArray = true;
        } catch (ParseException ex) {
            LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }


        JSONObject httpAnswerJSONobject = null;
        JSONArray httpAnwerJSONarray = null;
        if (isArray) {
            try {
                httpAnwerJSONarray = (JSONArray) (parser.parse(queryResult));
            } catch (ParseException ex) {
                LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
                apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
                return apiResponse;
            }
        } else {
            try {
                httpAnswerJSONobject = (JSONObject) (parser.parse(queryResult));
            } catch (ParseException ex) {
                LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
                apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
                return apiResponse;
            }
        }

        if (!isArray && httpAnswerJSONobject.containsKey("error")) {
            valid = false;
        }
        if (!valid) {
            //error
            String errorMessage = (String) httpAnswerJSONobject.get("error");
            ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);
            //LOG.severe("Poloniex API returned an error: " + errorMessage);
            apiResponse.setError(apiErr);
            return apiResponse;
        } else {
            //correct
            JSONArray orders = null;
            if (isArray) {
                /*Sample result
                 * if currencyPair is specified
                 *[
                 * {"orderNumber":"120466","type":"sell","rate":"0.025","amount":"100","total":"2.5"},
                 * {"orderNumber":"120467","type":"sell","rate":"0.04","amount":"100","total":"4"},
                 * ... ]
                 */
                orders = httpAnwerJSONarray;
                for (int i = 0; i < orders.size(); i++) {
                    JSONObject orderObject = (JSONObject) orders.get(i);
                    Order tempOrder = parseOrder(orderObject, pair);
                    orderList.add(tempOrder);
                }

            } else { //if currencyPair is set to 'all'
                /*
                 * {"BTC_1CR":[],
                 * "BTC_AC":
                 *      [{"orderNumber":"120466","type":"sell","rate":"0.025","amount":"100","total":"2.5"},
                 *          {"orderNumber":"120467","type":"sell","rate":"0.04","amount":"100","total":"4"}],
                 *          ... }
                 */

                Set<String> set = httpAnswerJSONobject.keySet();
                for (String key : set) {
                    JSONArray tempArray = (JSONArray) httpAnswerJSONobject.get(key);
                    for (int i = 0; i < tempArray.size(); i++) {
                        CurrencyPair cp = CurrencyPair.getCurrencyPairFromString(key, "_");
                        JSONObject orderObject = (JSONObject) tempArray.get(i);
                        Order tempOrder = parseOrder(orderObject, cp);
                        orderList.add(tempOrder);
                    }
                }

            }

        }
        apiResponse.setResponseObject(orderList);
        return apiResponse;
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        ApiResponse apiResp = new ApiResponse();
        Order order = null;

        ApiResponse listApiResp = getActiveOrders();
        if (listApiResp.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) listApiResp.getResponseObject();
            boolean found = false;
            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                if (orderID.equals(tempOrder.getId())) {
                    found = true;
                    apiResp.setResponseObject(tempOrder);
                    return apiResp;
                }
            }
            if (!found) {
                apiResp.setError(new ApiError(ERROR_GENERIC, "Cannot find the order with id " + orderID));
                return apiResp;

            }
        } else {
            return listApiResp;
        }

        return apiResp;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String base = API_BASE_URL;

        String command = API_CANCEL_ORDER;

        HashMap<String, String> query_args = new HashMap<>();

        /*Params
         */
        pair = CurrencyPair.swap(pair);
        query_args.put("currencyPair", pair.toString("_").toUpperCase());
        query_args.put("orderNumber", orderID);


        String queryResult = query(base, command, query_args, false);

        /*Sample result
         {"success":1}
         */

        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean valid = true;
            if (httpAnswerJson.containsKey("error")) {
                valid = false;
            }

            if (!valid) {
                //error
                String errorMessage = (String) httpAnswerJson.get("error");
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);
                LOG.warning("Cannot delete order " + orderID + " :" + errorMessage);
                apiResponse.setResponseObject(false);

                return apiResponse;
            } else {
                //correct
                apiResponse.setResponseObject(true);
            }
        } catch (ParseException ex) {
            LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee() {
        double defaultFee = 0.2;

        if (Global.options != null) {
            return new ApiResponse(true, Global.options.getTxFee(), null);
        } else {
            return new ApiResponse(true, defaultFee, null);
        }
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        LOG.fine("Poloniex uses global TX fee, currency pair not supprted. \n" + "now calling getTxFee()");
        return getTxFee();
    }

    @Override
    public ApiResponse isOrderActive(String id) {
        ApiResponse existResponse = new ApiResponse();

        ApiResponse orderDetailResponse = getOrderDetail(id);
        if (orderDetailResponse.isPositive()) {
            Order order = (Order) orderDetailResponse.getResponseObject();
            existResponse.setResponseObject(true);
        } else {
            ApiError err = orderDetailResponse.getError();
            if (err.getDescription().contains("Cannot find the order")) {
                existResponse.setResponseObject(false);

            } else {
                existResponse.setError(err);
            }
        }
        return existResponse;
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
        //Since there is no API entry point for that, this call will iterate over actie
        ApiResponse toReturn = new ApiResponse();
        boolean ok = true;

        ApiResponse activeOrdersResponse = getActiveOrders();
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();
            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);

                ApiResponse deleteOrderResponse = cancelOrder(tempOrder.getId(), pair);
                if (deleteOrderResponse.isPositive()) {
                    boolean deleted = (boolean) deleteOrderResponse.getResponseObject();

                    if (deleted) {
                        LOG.warning("Order " + tempOrder.getId() + " deleted succesfully");
                    } else {
                        LOG.warning("Could not delete order " + tempOrder.getId() + "");
                        ok = false;
                    }

                } else {
                    LOG.severe(deleteOrderResponse.getError().toString());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    LOG.severe(ex.toString());
                }

            }
            toReturn.setResponseObject(ok);
        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
            toReturn.setError(activeOrdersResponse.getError());
            return toReturn;
        }

        return toReturn;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        PoloniexService query = new PoloniexService(base, method, keys, args);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, isGet);
        } else {
            LOG.severe("The bot will not execute the query, there is no connection to Poloniex");
            queryResult = "error : no connection with Poloniex";
        }
        return queryResult;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair) {
        return getTradesImpl(pair, 0);
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        return getTradesImpl(pair, startTime);
    }

    private ApiResponse getTradesImpl(CurrencyPair pair, long startTime) {
        ApiResponse apiResponse = new ApiResponse();
        ArrayList<Trade> tradeList = new ArrayList<Trade>();

        String base = API_BASE_URL;
        String command = API_GET_TRADES;
        HashMap<String, String> query_args = new HashMap<>();

        String startDateArg;
        if (startTime == 0) {
            long now = System.currentTimeMillis();
            long yesterday = Math.round((now - Utils.getOneDayInMillis()) / 1000);
            startDateArg = Long.toString(yesterday); //24hours
        } else {
            startDateArg = Long.toString(startTime);
        }


        pair = CurrencyPair.swap(pair);
        query_args.put("currencyPair", pair.toString("_").toUpperCase());
        query_args.put("start", startDateArg);

        String queryResult = query(base, command, query_args, false);
        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        boolean valid = true;
        boolean isArray = false;
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
        } catch (ClassCastException ex) {
            isArray = true;
        } catch (ParseException ex) {
            LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }

        JSONObject httpAnswerJSONobject = null;
        JSONArray httpAnwerJSONarray = null;
        if (isArray) {
            try {
                httpAnwerJSONarray = (JSONArray) (parser.parse(queryResult));
            } catch (ParseException ex) {
                LOG.severe("httpresponse: " + queryResult + " \n" + ex.toString());
                apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
                return apiResponse;
            }
        }
        if (!isArray && httpAnswerJSONobject.containsKey("error")) {
            valid = false;
        }
        if (!valid) {
            //error
            String errorMessage = (String) httpAnswerJSONobject.get("error");
            ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage);
            //LOG.severe("Poloniex API returned an error: " + errorMessage);
            apiResponse.setError(apiErr);
            return apiResponse;
        } else {
            //correct
            JSONArray trades = null;
            if (isArray) {
                /*Sample result
                 * if currencyPair is specified
                 *[
                 * {"date":"2014-02-19 03:44:59","rate":"0.0011","amount":"99.9070909","total":"0.10989779","orderNumber":"3048809","type":"sell"},
                 *{"date":"2014-02-19 04:55:44","rate":"0.0015","amount":"100","total":"0.15","orderNumber":"3048903","type":"sell"},
                 * ... ] */
                trades = httpAnwerJSONarray;
                for (int i = 0; i < trades.size(); i++) {
                    JSONObject tradesObject = (JSONObject) trades.get(i);
                    Trade tempTrade = parseTrade(tradesObject, pair);
                    tradeList.add(tempTrade);
                }

            }

        }
        apiResponse.setResponseObject(tradeList);
        return apiResponse;
    }

    private Order parseOrder(JSONObject orderObject, CurrencyPair pair) {
        /* {"orderNumber":"120466","type":"sell","rate":"0.025","amount":"100","total":"2.5" */
        Order order = new Order();

        order.setType(((String) orderObject.get("type")).toUpperCase());
        order.setId((String) orderObject.get("orderNumber"));
        order.setAmount(new Amount(Utils.getDouble(orderObject.get("amount")), pair.getPaymentCurrency()));
        order.setPrice(new Amount(Utils.getDouble(orderObject.get("rate")), pair.getOrderCurrency()));
        order.setCompleted(false);
        order.setPair(pair);
        order.setInsertedDate(new Date()); //Not provided

        return order;

    }

    private Trade parseTrade(JSONObject tradeObj, CurrencyPair pair) {
        /* {"date":"2014-02-19 04:55:44","rate":"0.0015","amount":"100","fee":"0.02","total":"0.15","orderNumber":"3048903","type":"sell"}*/
        Trade trade = new Trade();
        trade.setOrder_id((String) tradeObj.get("orderNumber"));

        trade.setExchangeName(Constant.POLONIEX);
        trade.setPair(pair);

        trade.setType(((String) tradeObj.get("type")).toUpperCase());
        trade.setAmount(new Amount(Utils.getDouble(tradeObj.get("amount")), pair.getPaymentCurrency()));
        trade.setPrice(new Amount(Utils.getDouble(tradeObj.get("rate")), pair.getOrderCurrency()));
        trade.setFee(new Amount(0, pair.getPaymentCurrency()));

        String date = (String) tradeObj.get("date");
        trade.setDate(parseDate(date));

        return trade;
    }

    private Date parseDate(String dateStr) {
        Date toRet = null;
        //Parse the date
        //Sample 2014-02-19 04:55:44

        String datePattern = "yyyy-MM-dd HH:mm:ss";
        DateFormat df = new SimpleDateFormat(datePattern, Locale.ENGLISH);
        try {
            toRet = df.parse(dateStr);
        } catch (java.text.ParseException ex) {
            LOG.severe(ex.toString());
            toRet = new Date();
        }
        return toRet;
    }

    private class PoloniexService implements ServiceInterface {

        protected String base;
        protected String method;
        protected HashMap args;
        protected ApiKeys keys;
        protected String url;

        private PoloniexService(String base, String method, ApiKeys keys, HashMap<String, String> args) {
            this.base = base;
            this.method = method;
            this.args = args;
            this.keys = keys;
        }

        private PoloniexService(String url, HashMap<String, String> args) {
            //Used for ticker, does not require auth
            this.url = url;
            this.args = args;
            this.method = "";
        }

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
            String answer = "";
            String signature = "";
            String post_data = "";
            boolean httpError = false;
            HttpsURLConnection connection = null;

            try {
                // add nonce and build arg list
                if (needAuth) {
                    args.put("nonce", createNonce());
                    args.put("command", method);

                    post_data = TradeUtils.buildQueryString(args, ENCODING);

                    // args signature with apache cryptographic tools
                    String toHash = post_data;

                    signature = signRequest(keys.getPrivateKey(), toHash);
                }
                // build URL

                URL queryUrl;
                if (needAuth) {
                    queryUrl = new URL(base);
                } else {
                    queryUrl = new URL(url);
                }


                connection = (HttpsURLConnection) queryUrl.openConnection();
                connection.setRequestMethod("POST");

                // create and setup a HTTP connection

                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", Global.settings.getProperty("app_name"));

                if (needAuth) {
                    connection.setRequestProperty("Key", keys.getApiKey());
                    connection.setRequestProperty("Sign", signature);
                }

                connection.setDoOutput(true);
                connection.setDoInput(true);

                //Read the response

                DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(post_data);
                os.close();

                BufferedReader br = null;
                boolean toLog = false;
                if (connection.getResponseCode() >= 400) {
                    httpError = true;
                    br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
                    toLog = true;
                } else {
                    br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                }

                String output;

                if (httpError) {
                    LOG.severe("Post Data: " + post_data);
                }
                LOG.fine("Query to :" + base + "(method=" + method + ")" + " , HTTP response : \n"); //do not log unless is error > 400
                while ((output = br.readLine()) != null) {
                    LOG.fine(output);
                    answer += output;
                }

                if (httpError) {
                    JSONParser parser = new JSONParser();
                    try {
                        JSONObject obj2 = (JSONObject) (parser.parse(answer));
                        answer = (String) obj2.get(TOKEN_ERR);

                    } catch (ParseException ex) {
                        LOG.severe(ex.toString());

                    }
                }
            } //Capture Exceptions
            catch (IllegalStateException ex) {
                LOG.severe(ex.toString());

            } catch (NoRouteToHostException | UnknownHostException ex) {
                //Global.BtceExchange.setConnected(false);
                LOG.severe(ex.toString());

                answer = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
            } catch (IOException ex) {
                LOG.severe(ex.toString());
            } finally {
                //close the connection, set all objects to null
                connection.disconnect();
                connection = null;
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
                LOG.severe(ex.toString());
            }
            return sign;
        }

        private String createNonce() {
            long toRet = System.currentTimeMillis();
            return Long.toString(toRet);
        }
    }
}
