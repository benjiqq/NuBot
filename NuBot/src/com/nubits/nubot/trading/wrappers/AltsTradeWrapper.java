package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiError;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.ErrorManager;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Created by sammoth on 27/02/15.
 */
public class AltsTradeWrapper implements TradeInterface {

    private static final Logger LOG = Logger.getLogger(ExcoinWrapper.class.getName());
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
        ApiResponse apiResponse = new ApiResponse();
        String url = API_BASE_URL + "/" + API_BALANCE;
        HashMap<String, String> args = new HashMap<>();
        boolean isGet = false;
        
        ApiResponse response = getQuery(url, args, isGet);
        if (response.isPositive()) {
            JSONObject httpAnswerJson = (JSONObject) response.getResponseObject();
            LOG.severe(httpAnswerJson.toJSONString());
        } else {
            apiResponse = response;
        }
        
        return apiResponse;
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return null;
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
        if (!exchange.getLiveData().isConnected()) {
            LOG.severe("The bot will not execute the query, there is no connection to Alts.Trade");
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
            HttpsURLConnection connection = null;
            URL queryUrl = null;
            String post_data = "";
            boolean httpError = false;
            String output;
            int response = 200;
            String answer = null;

            if (needAuth) {
                args.put("nonce", Objects.toString(System.currentTimeMillis()));
                post_data = TradeUtils.buildQueryString(args, ENCODING);
            }
            
            try {
                if (isGet) {
                    queryUrl = new URL(url + "&" + post_data);
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
                    connection.setRequestProperty("Rest-Key", keys.getApiKey());
                    connection.setRequestProperty("Rest-Sign", signRequest(keys.getPrivateKey(), post_data));
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
                        + "Data : " + post_data
                        + "\nHTTP Response : " + Objects.toString(response));
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
                key = new SecretKeySpec(Base64.decode(secret), SIGN_HASH_FUNCTION);
                // Create a new mac
                mac = Mac.getInstance(SIGN_HASH_FUNCTION);
                // Init mac with key.
                mac.init(key);
                sign = Base64.encode(mac.doFinal(hash_data.getBytes(ENCODING)));
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
