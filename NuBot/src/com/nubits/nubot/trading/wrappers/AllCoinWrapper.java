package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.*;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.HttpUtils;
import com.nubits.nubot.utils.Utils;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONString;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.omg.CORBA.TIMEOUT;

import javax.crypto.Mac;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.*;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.sql.Time;
import java.util.*;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by woolly_sammoth on 21/10/14.
 */
public class AllCoinWrapper implements TradeInterface {

    private static final Logger LOG = Logger.getLogger(BterWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final int TIME_OUT = 15000;
    private String checkConnectionUrl = "https://www.allcoin.com/";
    private final String SIGN_HASH_FUNCTION = "MD5";
    private final String ENCODING = "UTF-8";
    //Entry Points
    private final String API_BASE_URL = "https://www.allcoin.com/api2/";
    private final String API_AUTH_URL = "https://www.allcoin.com/api2/auth_api/";
    private final String API_GET_INFO = "getinfo";
    private final String API_SELL_COIN = "sell_coin";
    private final String API_BUY_COIN = "buy_coin";
    private final String API_OPEN_ORDERS = "myorders";
    //Tokens
    private final String TOKEN_ERR = "error_info";
    private final String TOKEN_CODE = "code";
    private final String TOKEN_DATA = "data";
    private final String TOKEN_BAL_AVAIL = "balances_available";
    private final String TOKEN_BAL_HOLD = "balance_hold";
    private final String TOKEN_ORDER_ID = "order_id";
    //Errors
    private ArrayList<ApiError> errors;
    private final int NO_ERROR = 12059;
    private final int ERROR_UNKNOWN = 12560;
    private final int ERROR_NO_CONNECTION = 12561;
    private final int ERROR_GENERIC = 12562;
    private final int ERROR_PARSING = 12563;
    private final int ERROR_CURRENCY_NOT_FOUND = 12567;
    private final int ERROR_GET_INFO = 12568;
    private final int ERROR_SELL_COIN = 12569;
    private final int ERROR_ACTIVE_ORDERS = 12570;
    private final int ERROR_NULL_RETURN = 12571;


    public AllCoinWrapper() { setupErrors(); }

    public AllCoinWrapper(ApiKeys keys, Exchange exchange) {
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
    public ApiError getErrorByCode(int code) {
        boolean found = false;
        ApiError toReturn = null;
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
    public ApiResponse sell(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.SELL, pair, amount, rate);
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.BUY, pair, amount, rate);
    }

    private ApiResponse enterOrder(String type, CurrencyPair pair, double amount, double price) {
        ApiResponse apiResponse = new ApiResponse();
        String order_id;
        boolean isGet = false;
        TreeMap<String, String> query_args = new TreeMap<>();

        query_args.put("num", String.valueOf(amount));
        query_args.put("price", String.valueOf(price));
        query_args.put("type", pair.getOrderCurrency().getCode().toUpperCase());

        String url = API_AUTH_URL;
        String method;

        if (type == Constant.BUY) {
            method = API_BUY_COIN;
        } else {
            method = API_SELL_COIN;
        }

        String queryResult = query(url, method, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_SELL_COIN));
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            int code = 0;
            try {
                code = Integer.parseInt(httpAnswerJson.get(TOKEN_CODE).toString());
            } catch (ClassCastException cce) {
                LOG.severe(cce.toString());
            }

            if (code < 0) {
                String errorMessage = (String) httpAnswerJson.get(TOKEN_ERR);
                ApiError apiError = new ApiError(ERROR_GENERIC, errorMessage);
                //LOG.severe("AllCoin API returned an error : " + errorMessage);
                apiResponse.setError(apiError);
            } else {
                //we have returned data
                JSONObject dataJson = (JSONObject) httpAnswerJson.get(TOKEN_DATA);
                if (dataJson.containsKey(TOKEN_ORDER_ID)) {
                    order_id = dataJson.get(TOKEN_ORDER_ID).toString();
                    apiResponse.setResponseObject(order_id);
                }
            }

        } catch (ParseException pe) {
            LOG.severe("httpResponse: " + queryResult + " \n" + pe.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
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
        boolean isGet = false;
        TreeMap<String, String> query_args = new TreeMap<>();

        /*Params

         */

        String url = API_AUTH_URL;

        String queryResult = query(url, API_GET_INFO, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_GET_INFO));
        }

        LOG.info(queryResult);

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            int code = 0;
            try {
                code = Integer.parseInt(httpAnswerJson.get(TOKEN_CODE).toString());
            } catch (ClassCastException cce) {
                LOG.severe(cce.toString());
            }

            if (code < 0) {
                String errorMessage = (String) httpAnswerJson.get(TOKEN_ERR);
                ApiError apiError = new ApiError(ERROR_GENERIC, errorMessage);
                //LOG.severe("AllCoin API returned an error : " + errorMessage);
                apiResponse.setError(apiError);
            } else {
                //we have returned data
                JSONObject dataJson = (JSONObject) httpAnswerJson.get(TOKEN_DATA);
                JSONObject availableBal = (JSONObject) dataJson.get(TOKEN_BAL_AVAIL);

                //check for returned data
                if (availableBal == null) {
                    //we return the balances as 0
                    if (currency == null) { //all balances were requested
                        Amount PEGAvail = new Amount(0, pair.getPaymentCurrency());
                        Amount NBTAvail = new Amount(0, pair.getOrderCurrency());
                        Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency());
                        Amount NBTonOrder = new Amount(0, pair.getOrderCurrency());
                        Balance balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
                        apiResponse.setResponseObject(balance);
                    } else {
                        Amount total = new Amount(0, currency);
                        apiResponse.setResponseObject(total);
                    }
                }
                else { //we have returned data
                    String s;
                    if (currency == null) { //get all balances
                        JSONObject holdBal = (JSONObject) dataJson.get(TOKEN_BAL_HOLD);
                        Amount PEGAvail = new Amount(0, pair.getPaymentCurrency());
                        Amount NBTAvail = new Amount(0, pair.getOrderCurrency());
                        Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency());
                        Amount NBTonOrder = new Amount(0, pair.getOrderCurrency());

                        if (availableBal.containsKey(pair.getPaymentCurrency().getCode().toUpperCase())) {
                            s = availableBal.get(pair.getPaymentCurrency().getCode().toUpperCase()).toString();
                            PEGAvail.setQuantity(Double.parseDouble(s));
                        }

                        if (availableBal.containsKey(pair.getOrderCurrency().getCode().toUpperCase())) {
                            s = availableBal.get(pair.getOrderCurrency().getCode().toUpperCase()).toString();
                            NBTAvail.setQuantity(Double.parseDouble(s));
                        }

                        if (holdBal != null && holdBal.containsKey(pair.getPaymentCurrency().getCode().toUpperCase())) {
                            s = holdBal.get(pair.getPaymentCurrency().getCode().toUpperCase()).toString();
                            PEGonOrder.setQuantity(Double.parseDouble(s));
                        }

                        if (holdBal != null && holdBal.containsKey((pair.getOrderCurrency().getCode().toUpperCase()))) {
                            s = holdBal.get(pair.getOrderCurrency().getCode().toUpperCase()).toString();
                            NBTonOrder.setQuantity(Double.parseDouble(s));
                        }

                        Balance balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
                        apiResponse.setResponseObject(balance);
                    } else { //specific currency requested
                        Amount total = new Amount(0, currency);
                        if (availableBal.containsKey(currency.getCode().toUpperCase())) {
                            s = availableBal.get(currency.getCode().toUpperCase()).toString();
                            total.setQuantity(Double.parseDouble(s));
                        }
                        apiResponse.setResponseObject(total);
                    }
                }
            }
        } catch (ParseException pe) {
            LOG.severe("httpResponse: " + queryResult + " \n" + pe.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }

        return apiResponse;
    }

    @Override
    public String getUrlConnectionCheck() {
        return checkConnectionUrl;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String query(String url, HashMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates
    }

    @Override
    public String query(String url, String method, TreeMap<String, String> args, boolean isGet) {
        AllCoinService query = new AllCoinService(url, method, args, keys);
        String queryResult;
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, isGet);
        } else {
            LOG.severe("The bot will not execute the query, there is no connection to AllCoin");
            queryResult = "error : no connection with AllCoin";
        }
        return queryResult;
    }


    @Override
    public String query(String url, TreeMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates
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
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates
    }

    @Override
    public ApiResponse getActiveOrders() {
        return getActiveOrdersImpl(null);
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        return getActiveOrdersImpl(pair);
    }

    public ApiResponse getActiveOrdersImpl(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        boolean isGet = false;
        TreeMap<String, String> query_args = new TreeMap<>();
        ArrayList<Order> orderList = new ArrayList<Order>();
        String url = API_AUTH_URL;

        String queryResult = query(url, API_OPEN_ORDERS, query_args, isGet);
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_ACTIVE_ORDERS));
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            int code = 0;
            try {
                code = Integer.parseInt(httpAnswerJson.get(TOKEN_CODE).toString());
            } catch (ClassCastException cce) {
                LOG.severe(cce.toString());
            }

            if (code < 0) { //we have an error
                String errorMessage = (String) httpAnswerJson.get(TOKEN_ERR);
                ApiError apiError = new ApiError(ERROR_GENERIC, errorMessage);
                apiResponse.setError(apiError);
            } else {
                //we have returned data
                JSONArray dataJson = (JSONArray) httpAnswerJson.get(TOKEN_DATA);
                if (dataJson == null) {
                    ApiError apiError = new ApiError(ERROR_NULL_RETURN, "Null data return");
                    apiResponse.setError(apiError);
                }
                else {
                    for (Iterator<JSONObject> data = dataJson.iterator(); data.hasNext();) {
                        Order order = parseOrder(data.next());
                        if (pair != null && !order.getPair().equals(pair)) {
                            LOG.info("|" + order.getPair().toString() + "| = |" + pair.toString() + "|");
                            //we are only looking for orders with the specified pair.
                            //the current order doesn't fill that need
                            continue;
                        }
                        orderList.add(order);
                    }
                }
            }
            apiResponse.setResponseObject(orderList);

        } catch (ParseException pe) {
            LOG.severe("httpResponse: " + queryResult + " \n" + pe.toString());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the response"));
            return apiResponse;
        }

        return apiResponse;
    }

    public Order parseOrder(JSONObject data) {
        Order order = new Order();
        /*
        {
        "code": 1,
        "data": [
            {
            "order_id": "1410027",
            "user_id": "100000",
            "type": "DOGE",
            "exchange": "BTC",
            "ctime": "2014-06-15 14:42:36",
            "price": "0.00000060",
            "num": "1000.00000000",
            "total": "0.00060000",
            "rest_num": "1000.00000000", // the remaining DOGE of the order
            "rest_total": "0.00060000", //the remaining BTC of the order
            "fee": "0.00000090", // about fees, please visit here https://www.allcoin.com/pub/fee
            "order_type": "sell"
            },
            ...
        }
        */
        LOG.info(data.toString());
        //set the order id
        // A String containing a unique identifier for this order
        order.setId(data.get(TOKEN_ORDER_ID).toString());
        //set the pair
        //Object containing currency pair
        String pairString = data.get("type").toString() + "_" + data.get("exchange").toString();
        CurrencyPair thisPair = CurrencyPair.getCurrencyPairFromString(pairString, "_");
        order.setPair(thisPair);
        //set the amount
        //Object containing the number of units for this trade (without fees).
        Amount thisAmount = new Amount(Double.parseDouble(data.get("num").toString()), thisPair.getOrderCurrency());
        order.setAmount(thisAmount);
        //set the price
        //Object containing the price for each units traded.
        Amount thisPrice = new Amount(Double.parseDouble(data.get("price").toString()), thisPair.getOrderCurrency());
        order.setPrice(thisPrice);
        //set the insertedDate
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(data.get("ctime").toString());
        } catch (java.text.ParseException pe) {
            LOG.severe(pe.toString());
        }
        if (date != null) {
            long ctime = date.getTime();
            Date insertDate = new Date(ctime);
            order.setInsertedDate(insertDate);
        }
        //set the type
        String type = data.get("order_type").toString();
        if (type.equals("sell")) {
            order.setType(Constant.SELL);
        } else {
            order.setType(Constant.BUY);
        }
        return order;
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        return null;
    }

    @Override
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiResponse getTxFee() {
        return null;
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        return null;
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
    public ApiResponse clearOrders(CurrencyPair pair) {
        return null;
    }

    private class AllCoinService implements ServiceInterface {

        protected String url;
        protected TreeMap args;
        protected ApiKeys keys;
        protected String method;

        public AllCoinService(String url, String method, TreeMap<String, String> args, ApiKeys keys) {
            this.url = url;
            this.args = args;
            this.keys = keys;
            this.method = method;
        }

        private AllCoinService(String url, TreeMap<String, String> args) {
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

            try {
                queryUrl = new URL(url);
            } catch (MalformedURLException mal) {
                LOG.severe(mal.toString());
            }

            if (needAuth) {
                //add the access key, secret key, timestamp, method and sign to the args
                args.put("access_key", keys.getApiKey());
                args.put("secret_key", keys.getPrivateKey());
                args.put("created", Objects.toString(System.currentTimeMillis() / 1000L));
                args.put("method", method);
                //the sign is the MD5 hash of all arguments so far in alphabetical order
                args.put("sign", signRequest(keys.getPrivateKey(), TradeUtils.buildQueryString(args, ENCODING)));

                post_data = TradeUtils.buildQueryString(args, ENCODING);
            } else {
                post_data = TradeUtils.buildQueryString(args, ENCODING);
                try {
                    queryUrl = new URL(queryUrl + "?" + post_data);
                } catch (MalformedURLException mal) {
                    LOG.severe(mal.toString());
                }
            }

            try {
                connection = (HttpsURLConnection) queryUrl.openConnection();
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", Global.settings.getProperty("app_name"));

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
            } catch (IOException io) {
                LOG.severe((io.toString()));
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
            }

            if (httpError) {
                LOG.severe("Query to : " + url + " (method = " + method + " )" +
                        "\nData : \" + post_data" +
                        "\nHTTP Response : " + Objects.toString(response));
            }

            try {
                while ((output = br.readLine()) != null) {
                    answer += output;
                }
            } catch (IOException io) {
                LOG.severe(io.toString());
            }

            if (httpError) {
                JSONParser parser = new JSONParser();
                try {
                    JSONObject obj = (JSONObject) (parser.parse(answer));
                    answer = (String) obj.get(TOKEN_ERR);
                } catch (ParseException pe) {
                    LOG.severe(pe.toString());
                }
            }

            connection.disconnect();
            connection = null;

            return answer;
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            try {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
                byte[] array = md.digest(hash_data.getBytes());
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < array.length; ++i) {
                    sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
                }
                return sb.toString();
            } catch (java.security.NoSuchAlgorithmException e) {
            }
            return null;
        }

    }

}
