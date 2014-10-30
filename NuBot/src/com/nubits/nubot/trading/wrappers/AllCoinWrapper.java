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
    //Tokens
    private final String TOKEN_ERR = "error_info";
    private final String TOKEN_CODE = "code";
    private final String TOKEN_DATA = "data";
    private final String TOKEN_BAL_AVAIL = "balances_avaidlable";
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

        String queryResult = query(url, API_GET_INFO, query_args, isGet);;
        if (queryResult == null) {
            apiResponse.setError(getErrorByCode(ERROR_GET_INFO));
        }

        /* Response
        {
            "code": 1,
            "data": {
                "balances_avaidlable": {
                    "BTC": "100.06187964",
                    "LTC": "30.22620324",
                    ...
                },
                "balance_hold": {
                    "DOGE": 100000,
                    "BTC": 10.00773423,
                    ...
                },
                "servertimestamp": 1402830826
            }
        }
         */
        /* Error Response
            {"code":-13,"error_info":"invalide access_key","data":null}
         */

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
                String s;
                if (currency == null) { //get all balances
                    JSONObject holdBal = (JSONObject) dataJson.get(TOKEN_BAL_HOLD);
                    Amount PEGAvail = new Amount(-1, pair.getPaymentCurrency());
                    Amount NBTAvail = new Amount(-1, pair.getOrderCurrency());
                    Amount PEGonOrder = new Amount (-1, pair.getPaymentCurrency());
                    Amount NBTonOrder = new Amount(-1, pair.getOrderCurrency());

                    if (availableBal.containsKey(pair.getPaymentCurrency().getCode().toUpperCase())) {
                        s = availableBal.get(pair.getPaymentCurrency().getCode().toUpperCase()).toString();
                        PEGAvail.setQuantity(Double.parseDouble(s));
                    }

                    if (availableBal.containsKey(pair.getOrderCurrency().getCode().toUpperCase())) {
                        s = availableBal.get(pair.getOrderCurrency().getCode().toUpperCase()).toString();
                        NBTAvail.setQuantity(Double.parseDouble(s));
                    }

                    if (holdBal.containsKey(pair.getPaymentCurrency().getCode().toUpperCase())) {
                        s = holdBal.get(pair.getPaymentCurrency().getCode().toUpperCase()).toString();
                        PEGonOrder.setQuantity(Double.parseDouble(s));
                    }

                    if (holdBal.containsKey((pair.getOrderCurrency().getCode().toUpperCase()))) {
                        s = holdBal.get(pair.getOrderCurrency().getCode().toUpperCase()).toString();
                        NBTonOrder.setQuantity(Double.parseDouble(s));
                    }

                    Balance balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
                    apiResponse.setResponseObject(balance);
                } else { //specific currency requested
                    Amount total = new Amount(-1, currency);
                    if (availableBal.containsKey(currency.getCode().toUpperCase())) {
                        s = availableBal.get(currency.getCode().toUpperCase()).toString();
                        total.setQuantity(Double.parseDouble(s));
                    }
                    apiResponse.setResponseObject(total);
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
        return null;
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates
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
                Date currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
                args.put("created", currentTimestamp.toString());
                args.put("method", method);
                //the sign is the MD% hash of all arguments so far in alphabetical order
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
            byte[] sign_bytes = "BAD".getBytes();
            String sign = "";
            MessageDigest MD5Digest = null;
            try {
                MD5Digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                LOG.severe(nsae.toString());
            }
            MD5Digest.update(hash_data.getBytes());
            sign_bytes = MD5Digest.digest();
            try {
                sign = new String(sign_bytes, ENCODING);
            } catch (UnsupportedEncodingException use) {
                LOG.severe(use.toString());
            }
            return sign;
        }

    }

}
