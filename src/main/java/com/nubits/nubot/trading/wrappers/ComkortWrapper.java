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

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.*;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.trading.*;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by woolly_sammoth on 03/02/15.
 */
public class ComkortWrapper implements TradeInterface {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ComkortWrapper.class.getName());

    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";

    //Class fields
    private ApiKeys keys;
    protected ComkortService service;
    private Exchange exchange;
    private String apiBaseUrl;
    private String checkConnectionUrl;
    private int lastNonce = 0;
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
    private final String API_TRADES = "trades";
    private final String TOKEN_ERR = "error";
    private final String TOKEN_BAD_RETURN = "No Connection With Exchange";
    private final String TOKEN_CODE = "Code";
    //Errors
    private ErrorManager errors = new ErrorManager();

    public ComkortWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        service = new ComkortService(keys);
        setupErrors();
    }

    private void setupErrors() {
        errors.setExchangeName(exchange);
    }

    private ApiResponse getQuery(String url, HashMap<String, String> query_args, boolean needAuth, boolean isGet) {
        ApiResponse apiResponse = new ApiResponse();
        String queryResult = query(url, "", query_args, needAuth, isGet);
        if (queryResult == null) {
            apiResponse.setError(errors.nullReturnError);
            return apiResponse;
        }
        if (queryResult.equals(TOKEN_BAD_RETURN)) {
            apiResponse.setError(errors.noConnectionError);
            return apiResponse;
        }

        //LOG.error(queryResult);
        JSONParser parser = new JSONParser();
        Integer code = 0;

        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            if (httpAnswerJson.containsKey("code")) {
                ApiError error = errors.apiReturnError;
                error.setDescription(httpAnswerJson.get("error").toString());
                apiResponse.setError(error);
            } else {
                apiResponse.setResponseObject(httpAnswerJson);
            }
        } catch (ClassCastException cce) {
            //if casting to a JSON object failed, try a JSON Array
            try {
                JSONArray httpAnswerJson = (JSONArray) (parser.parse(queryResult));
                apiResponse.setResponseObject(httpAnswerJson);
            } catch (ParseException pe) {
                LOG.warn("httpResponse: " + queryResult + " \n" + pe.toString());
                apiResponse.setError(errors.parseError);
            }
        } catch (ParseException pe) {
            LOG.warn("httpResponse: " + queryResult + " \n" + pe.toString());
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

        ApiResponse response = getQuery(url, args, true, isGet);
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
                PairBalance balance = new PairBalance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
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

        args.put("market_alias", pair.toStringSepSpecial("_"));

        ApiResponse response = getQuery(url, args, true, isGet);
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


        args.put("market_alias", pair.toStringSepSpecial("_"));
        args.put("amount", Utils.formatNumber(amount, 8));
        args.put("price", Utils.formatNumber(rate, 8));

        ApiResponse response = getQuery(url, args, true, isGet);
        if (response.isPositive()) {
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

        ApiResponse response = getQuery(url, args, true, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject orders;
            try {
                orders = (JSONObject) httpAnswerJson.get("orders");
            } catch (ClassCastException cce) {
                apiResponse.setResponseObject(orderList);
                return apiResponse;
            }
            Set<String> keys = orders.keySet();
            for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
                String thisKey = key.next();
                CurrencyPair thisPair = CurrencyPair.getCurrencyPairFromString(thisKey, "_");
                JSONArray pairOrders = (JSONArray) orders.get(thisKey);
                for (Iterator<JSONObject> order = pairOrders.iterator(); order.hasNext(); ) {
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

        args.put("market_alias", pair.toStringSepSpecial("_"));

        ApiResponse response = getQuery(url, args, true, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray orders = (JSONArray) httpAnswerJson.get("orders");
            for (Iterator<JSONObject> order = orders.iterator(); order.hasNext(); ) {
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
        String type = (String) in.get("type");
        out.setType(type.equalsIgnoreCase("sell") ? Constant.SELL : Constant.BUY);
        Amount amount = new Amount(Utils.getDouble(in.get("amount").toString()), pair.getOrderCurrency());
        out.setAmount(amount);
        Amount price = new Amount(Utils.getDouble(in.get("price").toString()), pair.getPaymentCurrency());
        out.setPrice(price);
        out.setPair(pair);
        if (in.containsKey("added")) {
            long timeStamp = (long) Utils.getDouble(in.get("added").toString());
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
            apiResponse.setResponseObject(new Order());
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
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

        ApiResponse response = getQuery(url, args, true, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            try {
                boolean success = (boolean) httpAnswerJson.get("success");
                apiResponse.setResponseObject(success);
            } catch (NullPointerException npe) {
                apiResponse.setResponseObject(false);
            }
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
        return getLastTradesImpl(pair, 0);
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        return getLastTradesImpl(pair, startTime);
    }

    private ApiResponse getLastTradesImpl(CurrencyPair pair, long startTime) {
        ApiResponse apiResponse = new ApiResponse();
        //https://api.comkort.com/v1/private/user/trades
        String url = API_BASE_URL + "/" + API_USER + "/" + API_TRADES;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        ArrayList<Trade> tradeList = new ArrayList<>();

        args.put("market_alias", pair.toStringSepSpecial("_"));

        ApiResponse response = getQuery(url, args, true, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray trades = (JSONArray) httpAnswerJson.get("trades");
            for (Iterator<JSONObject> trade = trades.iterator(); trade.hasNext(); ) {
                Trade thisTrade = parseTrade(trade.next());
                if (thisTrade.getDate().getTime() < (startTime * 1000L)) {
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

        CurrencyPair pair = CurrencyPair.getCurrencyPairFromString(in.get("market_alias").toString(), "_");
        out.setPair(pair);
        Amount amount = new Amount(Utils.getDouble(in.get("amount")), pair.getOrderCurrency());
        out.setAmount(amount);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(in.get("time").toString());
        } catch (java.text.ParseException pe) {
            LOG.warn(pe.toString());
        }
        if (date != null) {
            long timeStamp = date.getTime();
            Date insertDate = new Date(timeStamp);
            out.setDate(insertDate);
        }
        out.setExchangeName(Global.exchange.getName());
        out.setId(in.get("id").toString());
        if (in.get("buy_order_id").equals(false)) {
            out.setOrder_id(in.get("sell_order_id").toString());
            out.setType(Constant.SELL);
        } else {
            out.setOrder_id(in.get("buy_order_id").toString());
            out.setType(Constant.BUY);
        }
        Amount price = new Amount(Utils.getDouble(in.get("price")), pair.getPaymentCurrency());
        out.setPrice(price);

        return out;
    }

    @Override
    public ApiResponse isOrderActive(String id) {
        ApiResponse apiRespone = new ApiResponse();
        ApiResponse activeOrders = getActiveOrders();
        if (activeOrders.isPositive()) {
            ArrayList<Order> orderList = (ArrayList) activeOrders.getResponseObject();
            apiRespone.setResponseObject(false);
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(id)) {
                    apiRespone.setResponseObject(true);
                }
            }
        } else {
            apiRespone = activeOrders;
        }
        return apiRespone;
    }

    @Override
    public ApiResponse getOrderBook(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        ApiResponse openOrders = getActiveOrders();
        if (openOrders.isPositive()) {
            apiResponse.setResponseObject(true);
            ArrayList<Order> orderList = (ArrayList) openOrders.getResponseObject();
            for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {
                Order thisOrder = order.next();
                ApiResponse cancel = cancelOrder(thisOrder.getId(), thisOrder.getPair());
                if (cancel.getResponseObject().equals(false)) {
                    apiResponse.setResponseObject(false);
                }
            }
        } else {
            apiResponse = openOrders;
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
        String queryResult = TOKEN_BAD_RETURN; //Will return this string in case it fails
        if (exchange.getLiveData().isConnected()) {
            if (exchange.isFree()) {
                exchange.setBusy();
                queryResult = service.executeQuery(base, method, args, needAuth, isGet);
                exchange.setFree();
            } else {
                //Another thread is probably executing a query. Init the retry procedure
                long sleeptime = Settings.RETRY_SLEEP_INCREMENT * 1;
                int counter = 0;
                long startTimeStamp = System.currentTimeMillis();
                LOG.debug(method + " blocked, another call is being processed ");
                boolean exit = false;
                do {
                    counter++;
                    sleeptime = counter * Settings.RETRY_SLEEP_INCREMENT; //Increase sleep time
                    sleeptime += (int) (Math.random() * 200) - 100;// Add +- 100 ms random to facilitate competition
                    LOG.debug("Retrying for the " + counter + " time. Sleep for " + sleeptime + "; Method=" + method);
                    try {
                        Thread.sleep(sleeptime);
                    } catch (InterruptedException e) {
                        LOG.error(e.toString());
                    }

                    //Try executing the call
                    if (exchange.isFree()) {
                        LOG.debug("Finally the exchange is free, executing query after " + counter + " attempt. Method=" + method);
                        exchange.setBusy();
                        queryResult = service.executeQuery(base, method, args, needAuth, isGet);
                        exchange.setFree();
                        break; //Exit loop
                    } else {
                        LOG.debug("Exchange still busy : " + counter + " .Will retry soon; Method=" + method);
                        exit = false;
                    }
                    if (System.currentTimeMillis() - startTimeStamp >= Settings.TIMEOUT_QUERY_RETRY) {
                        exit = true;
                        LOG.error("The bot will not execute the query, there is no connection with" + exchange.getName());
                    }
                } while (!exit);
            }
        } else {
            LOG.error("The bot will not execute the query, there is no connection to BitcoinCoId");
            queryResult = TOKEN_BAD_RETURN;
        }
        return queryResult;
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

        protected ApiKeys keys;

        public ComkortService(ApiKeys keys) {
            this.keys = keys;
        }

        private ComkortService() {
            //Used for ticker, does not require auth
        }

        @Override
        public String executeQuery(String base, String method, AbstractMap<String, String> args, boolean needAuth, boolean isGet) {
            HttpsURLConnection connection = null;
            URL queryUrl = null;
            String post_data = "";
            String url = base + method;
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
                LOG.warn(mal.toString());
                return null;
            }

            try {
                connection = (HttpsURLConnection) queryUrl.openConnection();
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", Settings.APP_NAME);

                if (needAuth) {
                    connection.setRequestProperty("apikey", keys.getApiKey());
                    connection.setRequestProperty("sign", TradeUtils.signRequest(keys.getPrivateKey(), post_data, SIGN_HASH_FUNCTION, ENCODING));
                    int nonce = Integer.parseInt(Objects.toString(System.currentTimeMillis() / 1000L));
                    while (nonce <= lastNonce) {
                        nonce += 1;
                    }
                    lastNonce = nonce;
                    connection.setRequestProperty("nonce", Objects.toString(nonce));
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
                LOG.warn(pe.toString());
                return null;
            } catch (IOException io) {
                LOG.warn((io.toString()));
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
                LOG.warn(io.toString());
                return null;
            }

            if (httpError) {
                if (Global.options.isVerbose()) {
                    LOG.warn("Query to : " + url
                            + "\nData : " + post_data
                            + "\nHTTP Response : " + Objects.toString(response));
                }
                String error = "";
                try {
                    while ((output = br.readLine()) != null) {
                        error += output;
                    }
                } catch (IOException io) {
                    LOG.warn(io.toString());
                }
                return error;
            }

            try {
                while ((output = br.readLine()) != null) {
                    answer += output;
                }
            } catch (IOException io) {
                LOG.warn(io.toString());
                return null;
            }

            connection.disconnect();
            connection = null;

            return answer;
        }

    }

}
