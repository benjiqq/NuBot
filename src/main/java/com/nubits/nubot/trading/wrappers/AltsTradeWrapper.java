package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.*;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.ErrorManager;
import com.nubits.nubot.utils.Utils;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by sammoth on 27/02/15.
 */
public class AltsTradeWrapper implements TradeInterface {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AltsTradeWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private String apiBaseUrl;
    private String checkConnectionUrl;
    //API Paths
    private final String API_BASE_URL = "https://alts.trade/rest_api";
    private final String API_BALANCE = "balance";
    private final String API_TICKER = "ticker";
    private final String API_ORDERS = "orders";
    private final String API_MY = "my";
    private final String API_MY_ALL = "my_all";
    private final String API_CANCEL = "cancel";
    private final String API_MY_HISTORY = "my_history";
    //Errors
    private ErrorManager errors = new ErrorManager();
    private final String TOKEN_ERR = "error";
    private final String TOKEN_BAD_RETURN = "No Connection With Exchange";

    public AltsTradeWrapper() {
        setupErrors();
    }

    public AltsTradeWrapper(ApiKeys keys, Exchange exchange) {
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
            boolean status = (boolean) httpAnswerJson.get("status");
            if (!status) {
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
        return getBalancesImpl(pair, null);
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return getBalancesImpl(null, currency);
    }

    private ApiResponse getBalancesImpl(CurrencyPair pair, Currency currency) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_BALANCE;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONArray httpAnswerJson = (JSONArray) response.getResponseObject();
            if (currency != null) { //get just one currency balance
                Amount balance = new Amount(0, currency);
                for (Iterator<JSONObject> wallet = httpAnswerJson.iterator(); wallet.hasNext();) {
                    JSONObject thisWallet = wallet.next();
                    if (thisWallet.get("code").equals(currency.getCode().toUpperCase())) {
                        balance.setQuantity(Utils.getDouble(thisWallet.get("balance")));
                    }
                }
                apiResponse.setResponseObject(balance);
            } else { // get the full pair balances
                Amount PEGAvail = new Amount(0, pair.getPaymentCurrency());
                Amount NBTAvail = new Amount(0, pair.getOrderCurrency());
                Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency());
                Amount NBTonOrder = new Amount(0, pair.getOrderCurrency());
                for (Iterator<JSONObject> wallet = httpAnswerJson.iterator(); wallet.hasNext();) {
                    JSONObject thisWallet = wallet.next();
                    if (thisWallet.get("code").equals(pair.getOrderCurrency().getCode().toUpperCase())) {
                        NBTAvail.setQuantity(Utils.getDouble(thisWallet.get("balance")));
                        NBTonOrder.setQuantity(Utils.getDouble(thisWallet.get("held_for_orders")));
                    }
                    if (thisWallet.get("code").equals(pair.getPaymentCurrency().getCode().toUpperCase())) {
                        PEGAvail.setQuantity(Utils.getDouble(thisWallet.get("balance")));
                        PEGonOrder.setQuantity(Utils.getDouble(thisWallet.get("held_for_orders")));
                    }
                }
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
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_TICKER + "/" + pair.toStringSepSpecial("/");
        //https://alts.trade/rest_api/ticker/{market_name}
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = true;

        double last = -1;
        double ask = -1;
        double bid = -1;
        Ticker ticker = new Ticker();

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONObject result = (JSONObject) httpAnswerJson.get("result");
            last = Utils.getDouble(result.get("last"));
            ask = Utils.getDouble(result.get("low"));
            bid = Utils.getDouble(result.get("high"));
            ticker.setAsk(ask);
            ticker.setLast(last);
            ticker.setBid(bid);
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
        String url = API_BASE_URL + "/" + API_ORDERS;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;

        DecimalFormat nf = new DecimalFormat("0");
        nf.setMinimumFractionDigits(8);

        args.put("amount", nf.format(amount));
        args.put("market", pair.toStringSepSpecial("/").toUpperCase());
        args.put("price", nf.format(rate));
        args.put("action", type);

        LOG.warn(args.toString());

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            Integer order_id = (Integer) response.getResponseObject();
            apiResponse.setResponseObject(order_id);
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders() {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_ORDERS + "/" + API_MY_ALL;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        ArrayList<Order> orderList = new ArrayList<>();

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray orders = (JSONArray) httpAnswerJson.get("orders");
            for (Iterator<JSONObject> order = orders.iterator(); order.hasNext();) {
                orderList.add(parseOrder(order.next(), null));
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
        String url = API_BASE_URL + "/" + API_ORDERS + "/" + API_MY;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        ArrayList<Order> orderList = new ArrayList<>();

        args.put("market", pair.toStringSepSpecial("/").toUpperCase());

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

        out.setId(in.get("order_id").toString());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(in.get("date").toString());
        } catch (java.text.ParseException pe) {
            LOG.warn(pe.toString());
        }
        if (date != null) {
            out.setInsertedDate(date);
        }
        if (pair == null) {
            pair = CurrencyPair.getCurrencyPairFromString(in.get("market").toString(), "/");
        }
        out.setPair(pair);
        Amount price = new Amount(Utils.getDouble(in.get("price")), pair.getPaymentCurrency());
        out.setPrice(price);
        Amount amount = new Amount(Utils.getDouble(in.get("amount")), pair.getOrderCurrency());
        out.setAmount(amount);
        out.setType(in.get("action").toString() == "SELL" ? Constant.SELL : Constant.BUY);
        return out;
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse getAllOrders = getActiveOrders();
        if (getAllOrders.isPositive()) {
            apiResponse.setResponseObject(new Order());
            ArrayList<Order> orders = (ArrayList) getAllOrders.getResponseObject();
            for (Iterator<Order> order = orders.iterator(); order.hasNext();) {
                Order thisOrder = order.next();
                if (thisOrder.getId().equals(orderID)) {
                    apiResponse.setResponseObject(thisOrder);
                }
            }
        } else {
            apiResponse = getAllOrders;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_ORDERS + "/" + API_CANCEL;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;

        args.put("order_id", orderID);

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            apiResponse.setResponseObject(true);
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee() {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseObject((double) 0);
        return apiResponse;
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setResponseObject((double) 0);
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
        String url = API_BASE_URL + "/" + API_ORDERS + "/" + API_MY_HISTORY;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        ArrayList<Trade> tradeList = new ArrayList<>();

        args.put("market", pair.toStringSepSpecial("/"));

        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            JSONArray history = (JSONArray) httpAnswerJson.get("history");
            for (Iterator<JSONObject> trade = history.iterator(); trade.hasNext();) {
                Trade thisTrade = parseTrade(trade.next(), pair);
                if (startTime > 0 && thisTrade.getDate().getTime() < startTime) continue;
                tradeList.add(thisTrade);
            }
            apiResponse.setResponseObject(tradeList);
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    private Trade parseTrade(JSONObject in, CurrencyPair pair) {
        Trade out = new Trade();

        out.setExchangeName(exchange.getName());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(in.get("date").toString());
        } catch (java.text.ParseException pe) {
            LOG.warn(pe.toString());
        }
        if (date != null) {
            out.setDate(date);
        }
        Amount amount = new Amount(Utils.getDouble(in.get("amount")), pair.getOrderCurrency());
        out.setAmount(amount);
        Amount price = new Amount(Utils.getDouble(in.get("price")), pair.getPaymentCurrency());
        out.setPrice(price);
        out.setType(in.get("action").toString().equals("SELL") ? Constant.SELL : Constant.BUY);

        return out;
    }

    @Override
    public ApiResponse isOrderActive(String id) {
        ApiResponse apiResponse = new ApiResponse();

        ApiResponse getActiveOrders = getActiveOrders();
        if (getActiveOrders.isPositive()) {
            ArrayList<Order> orders = (ArrayList) getActiveOrders.getResponseObject();
            boolean isActive = false;
            for (Iterator<Order> order = orders.iterator(); order.hasNext();) {
                if (order.next().getId().equals(id)) {
                    isActive = true;
                }
            }
            apiResponse.setResponseObject(isActive);
        } else {
            apiResponse = getActiveOrders;
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
            ArrayList<Order> orders = (ArrayList) getOrders.getResponseObject();
            boolean allClear = true;
            for (Iterator<Order> order = orders.iterator(); order.hasNext();) {
                ApiResponse cancel = cancelOrder(order.next().getId(), pair);
                if (!cancel.isPositive()) {
                    allClear = false;
                }
            }
            apiResponse.setResponseObject(allClear);
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
    public String query(String url, HashMap<String, String> args, boolean isGet) {
        if (!exchange.getLiveData().isConnected()) {
            LOG.warn("The bot will not execute the query, there is no connection to Alts.Trade");
            return TOKEN_BAD_RETURN;
        }
        String queryResult;
        AltsTradeService query = new AltsTradeService(url, args, keys);
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

    class AltsTradeService implements ServiceInterface {

        protected String url;
        protected HashMap args;
        protected ApiKeys keys;

        public AltsTradeService(String url, HashMap<String, String> args, ApiKeys keys) {
            this.url = url;
            this.args = args;
            this.keys = keys;
        }

        private AltsTradeService(String url, HashMap<String, String> args) {
            //Used for ticker, does not require auth
            this.url = url;
            this.args = args;
        }


        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
            String answer = null;
            String signature = "";
            String post_data = "";

            args.put("nonce", Objects.toString(System.currentTimeMillis()));

            List< NameValuePair> urlParameters = new ArrayList< NameValuePair>();

            for (Iterator< Map.Entry< String, String>> argumentIterator = args.entrySet().iterator(); argumentIterator.hasNext();) {
                Map.Entry< String, String> argument = argumentIterator.next();
                urlParameters.add(new BasicNameValuePair(argument.getKey(), argument.getValue()));
            }

            post_data = TradeUtils.buildQueryString(args, ENCODING);

            signature = signRequest(keys.getPrivateKey(), post_data);

            // add header
            Header[] headers = new Header[3];
            headers[ 0] = new BasicHeader("Rest-Key", keys.getApiKey());
            headers[ 1] = new BasicHeader("Rest-Sign", signature);
            headers[ 2] = new BasicHeader("Content-type", "application/x-www-form-urlencoded");

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = null;
            HttpGet get = null;
            HttpResponse response = null;

            try {
                if (!isGet) {
                    post = new HttpPost(url);
                    post.setEntity(new UrlEncodedFormEntity(urlParameters, ENCODING));
                    post.setHeaders(headers);
                    response = client.execute(post);
                } else {
                    get = new HttpGet(url);
                    get.setHeaders(headers);
                    response = client.execute(get);
                }
            } catch (NoRouteToHostException e) {
                if (!isGet) {
                    post.abort();
                } else {
                    get.abort();
                }
                LOG.warn(e.toString());
                return null;
            } catch (SocketException e) {
                if (!isGet) {
                    post.abort();
                } else {
                    get.abort();
                }
                LOG.warn(e.toString());
                return null;
            } catch (Exception e) {
                if (!isGet) {
                    post.abort();
                } else {
                    get.abort();
                }
                LOG.warn(e.toString());
                return null;
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

                LOG.warn(ex.toString());
                return null;
            } catch (IllegalStateException ex) {

                LOG.warn(ex.toString());
                return null;
            }
            if (Global.options
                    != null && Global.options.isVerbose()) {

                LOG.info("\nSending request to URL : " + url + " ; get = " + isGet);
                if (post != null) {
                    System.out.println("Post parameters : " + post.getEntity());
                }
                LOG.info("Response Code : " + response.getStatusLine().getStatusCode());
                LOG.info("Response :" + response);

            }
            return answer;

        }

        @Override
        public String signRequest(String secret, String hash_data) {

            String sign = "";
            try {
                Mac mac;
                SecretKeySpec key;
                // Create a new secret key
                key = new SecretKeySpec(DatatypeConverter.parseBase64Binary(secret), SIGN_HASH_FUNCTION);
                // Create a new mac
                mac = Mac.getInstance(SIGN_HASH_FUNCTION);
                // Init mac with key.
                mac.init(key);
                byte[] b = mac.doFinal(hash_data.getBytes(ENCODING));

                sign = DatatypeConverter.printBase64Binary(b);

            } catch (UnsupportedEncodingException uee) {
                LOG.warn("Unsupported encoding exception: " + uee.toString());
            } catch (NoSuchAlgorithmException nsae) {
                LOG.warn("No such algorithm exception: " + nsae.toString());
            } catch (InvalidKeyException ike) {
                LOG.warn("Invalid key exception: " + ike.toString());
            }
            return sign;
        }
    }
}
