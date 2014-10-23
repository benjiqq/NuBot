package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.*;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.omg.CORBA.TIMEOUT;

import javax.crypto.Mac;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;

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
    private final String API_BASE_URL = "https://www.allcoin.com/api2/";
    private final String API_AUTH_URL = "auth_api/";
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

        String url = API_BASE_URL + API_AUTH_URL;

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
            args.put("access_key", keys.getApiKey());
            Date currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTime().getTime());
            args.put("created", currentTimestamp.toString());
            args.put("method", method);
            args.put("sign", "");

            LOG.fine("Calling " + method + " with params: " + args)
            Document doc;
            String response = null;
            try {
                String url = API_BASE_URL + API_AUTH_URL;
                Connection connection = HttpUtils.getConnectionForPost(url, args).timeout(TIME_OUT);

                connection.ignoreHttpErrors(true);
                if ("post".equalsIgnoreCase())
            }
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            String sign = "";
            try {
                Mac mac = null;
                SecretKeySpec key = null;
                //create a new secret key
                try {
                    key = new SecretKeySpec(secret.getBytes(ENCODING), SIGN_HASH_FUNCTION));
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
