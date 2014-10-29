package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.*;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.omg.CORBA.TIMEOUT;

import javax.crypto.Mac;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
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
public abstract class AllCoinWrapper implements TradeInterface {

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
    //Errors
    private ArrayList<ApiError> errors;
    private final String TOKEN_ERR = "error";
    private final int ERROR_UNKNOWN = 12560;
    private final int ERROR_NO_CONNECTION = 12561;
    private final int ERROR_GENERIC = 12562;
    private final int ERROR_PARSING = 12563;
    private final int ERROR_CURRENCY_NOT_FOUND = 12567;
    private final int ERROR_GET_INFO = 12564;

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
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        return getBalanceImpl(null, pair);
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return getBalanceImpl(currency, null);
    }

    private ApiResponse getBalanceImpl(Currency currency, CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();
        Balance balance = new Balance();

        boolean isGet = false;

        TreeMap<String, String> query_args = new TreeMap<>();

        /*Params

         */

        String url = API_AUTH_URL;

        String queryResult = query(url, query_args, isGet);
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

        return apiResponse;
    }

    @Override
    public String query(String url, String method, TreeMap<String, String> args, boolean isGet) {
        AllCoinService query = new AllCoinService(url, method, args, keys);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, isGet);

        } else {
            LOG.severe("The bot will not execute the query, there is no connection to AllCoin");
            queryResult = "error : no connection with AllCoin";
        }
        return queryResult;
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
            String answer = "";

            try {
                queryUrl = new URL(url);
            } catch (MalformedURLException mal) {
                LOG.severe(mal.toString());
            }

            if (needAuth) {
                //add the access key, timestamp, method and sign to the args
                args.put("access_key", keys.getApiKey());
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


            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("User-Agent", Global.settings.getProperty("app_name"));

            connection.setDoOutput(true);
            connection.setDoInput(true);

            try {
                connection = (HttpsURLConnection) queryUrl.openConnection();
                if (isGet) {
                    connection.setRequestMethod("GET");
                } else {
                    connection.setRequestMethod("POST");
                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(post_data);
                    os.flush();
                    os.close();
                }

            } catch (IOException io) {
                LOG.severe(io.toString());
            }

            BufferedReader br = null;
            try {
                if (connection.getResponseCode() >= 400) {
                    httpError = true;
                    response = connection.getResponseCode();
                    br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                } else {
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
            String sign = "";
            try {
                Mac mac = null;
                SecretKeySpec key = null;
                //create a new secret key
                try {
                    key = new SecretKeySpec(secret.getBytes(ENCODING), SIGN_HASH_FUNCTION);
                } catch (UnsupportedEncodingException uee) {
                    LOG.severe("Unsupported encoding exception: " + uee.toString());
                }

                //create a new mac
                try {
                    mac = Mac.getInstance(SIGN_HASH_FUNCTION);
                } catch (NoSuchAlgorithmException nsae) {
                    LOG.severe("No such Algorithm exception: " + nsae.toString());
                }

                //Init mac with key
                try {
                    mac.init(key);
                } catch (InvalidKeyException ike) {
                    LOG.severe("Invalid key exception: " + ike.toString());
                }

                sign = Hex.encodeHexString(mac.doFinal(hash_data.getBytes(ENCODING)));

            } catch (UnsupportedEncodingException ex) {
                LOG.severe(ex.getMessage());
            }
            return sign;
        }

    }

}
