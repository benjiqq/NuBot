package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.ApiError;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.ErrorManager;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Created by woolly_sammoth on 05/03/15.
 */
public class BittrexWrapper implements TradeInterface {

    private static final Logger LOG = LoggerFactory.getLogger(BittrexWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private String apiBaseUrl;
    private String checkConnectionUrl;
    //API Paths
    private final String API_BASE_URL = "https://bittrex.com/api/v1.1";
    private final String API_BALANCE = "/account/getbalance";
    private final String API_BALANCES = "/account/getbalances";
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
        setupErrors();
    }

    private void setupErrors() {
        errors.setExchangeName(exchange);
    }

    private ApiResponse getQuery(String url, String method, HashMap<String, String> query_args, boolean isGet) {
        ApiResponse apiResponse = new ApiResponse();
        String queryResult = query(url, method, query_args, isGet);
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
        return null;
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return null;
    }

    private ApiResponse getBalancesImpl(CurrencyPair pair, Currency currency) {
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL;
        String method = null;
        HashMap<String, String> args = new HashMap<>();
        if (pair == null) { //get just single currency
            method = API_BALANCE;
            args.put("currency", currency.getCode().toUpperCase());
        } else {
            method = API_BALANCES;
        }
        boolean isGet = false;

        ApiResponse response = getQuery(url, method, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            System.out.println(httpAnswerJson.toJSONString());
        } else {
            apiResponse = response;
        }
        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiResponse sell(CurrencyPair pair, double amount, double rate) {
        return null;
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        return null;
    }

    @Override
    public ApiResponse getActiveOrders() {
        return null;
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        return null;
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
        return null;
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        if (!exchange.getLiveData().isConnected()) {
            LOG.error("The bot will not execute the query, there is no connection to Bittrex");
            return TOKEN_BAD_RETURN;
        }
        String queryResult;
        BittrexService query = new BittrexService(base, method, args, keys);
        if (isGet) {
            queryResult = query.executeQuery(false, isGet);
        } else {
            queryResult = query.executeQuery(true, isGet);
        }
        return queryResult;
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
        this.apiBaseUrl = apiBaseUrl;
    }

    class BittrexService implements ServiceInterface {

        protected String url;
        protected String method;
        protected HashMap args;
        protected ApiKeys keys;

        public BittrexService(String url, String method, HashMap<String, String> args, ApiKeys keys) {
            this.url = url;
            this.method = method;
            this.args = args;
            this.keys = keys;
        }

        private BittrexService(String url, String method, HashMap<String, String> args) {
            //Used for ticker, does not require auth
            this.url = url;
            this.method = method;
            this.args = args;
        }

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
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
            args.put("nonce", System.currentTimeMillis());

            try {
                post_data = TradeUtils.buildQueryString(args, ENCODING);
                queryUrl = new URL(url + "/" + method + "?" + post_data);
            } catch (MalformedURLException mal) {
                LOG.error(mal.toString());
            }

            try {
                connection = (HttpsURLConnection) queryUrl.openConnection();
                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", Global.settings.getProperty("app_name"));
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

            /*
             if (httpError) {
             JSONParser parser = new JSONParser();
             try {
             JSONObject obj = (JSONObject) (parser.parse(answer));
             answer = (String) obj.get(TOKEN_ERR);
             } catch (ParseException pe) {
             LOG.error(pe.toStringSep());
             return null;
             }
             }
             */

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
