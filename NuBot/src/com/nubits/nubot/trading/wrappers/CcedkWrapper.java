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
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.TradeUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
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

public class CcedkWrapper implements TradeInterface {

    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final int SPACING_BETWEEN_CALLS = 1100;
    private final int TIME_OUT = 15000;
    private long lastSentTonce = 0L;
    private String checkConnectionUrl = "https://www.ccedk.com/";
    private boolean apiBusy = false;
    private final String SIGN_HASH_FUNCTION = "HmacSHA512";
    private final String ENCODING = "UTF-8";
    private final String API_BASE_URL = checkConnectionUrl + "api/v1/";
    private final String API_GET_INFO = "balance/list"; //post
    private final String API_TRADE = "order/new"; //post
    private final String API_ACTIVE_ORDERS = "";
    private final String API_ORDER = "";
    private final String API_CANCEL_ORDER = "";
    //For the ticker entry point, use getTicketPath(CurrencyPair pair)
    // Errors
    private ArrayList<ApiError> errors;
    private final String TOKEN_ERR = "errors";
    private final int ERROR_UNKNOWN = 8560;
    private final int ERROR_NO_CONNECTION = 8561;
    private final int ERROR_GENERIC = 8562;
    private final int ERROR_PARSING = 8563;
    private final int ERROR_CURRENCY_NOT_FOUND = 8564;
    private static final Logger LOG = Logger.getLogger(CcedkWrapper.class.getName());

    public CcedkWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        setupErrors();
    }

    protected String createNonce(String requester) {
        //This is a  workaround waiting for clarifications from CCEDK team


        return TradeUtils.getCCDKEvalidNonce();
        /*
         Long toReturn = 0L;
         if (!apiBusy) {
         toReturn = getNonceInternal(requester);
         } else {
         try {
         if (Global.options != null) {
         if (Global.options.isVerbose()) {
         LOG.info(System.currentTimeMillis() + " - Api is busy, I'll sleep and retry in a few ms (" + requester + ")");
         }
         }
         Thread.sleep(Math.round(2.2 * SPACING_BETWEEN_CALLS));
         createNonce(requester);
         } catch (InterruptedException e) {
         LOG.severe(e.getMessage());
         }
         }
         return Long.toString(toReturn);
         * */

    }

    private void setupErrors() {
        errors = new ArrayList<ApiError>();
        errors.add(new ApiError(ERROR_NO_CONNECTION, "Failed to connect to the exchange entrypoint. Verify your connection"));
        errors.add(new ApiError(ERROR_PARSING, "Parsing error"));
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

        String path = API_GET_INFO;
        HashMap<String, String> query_args = new HashMap<>();
        /*Params
         * nonce=<\d{10}>
         * order_by=<'field_name'>:'balance_id'
         * order_direction=<ASC|DESC>:ASC i
         * tems_per_page=<\d+>:100
         * page=<\d+>:1
         */


        /*Sample result
         *{"errors":false,
         * "response":
         *  {"entities":
         *      [{"currency_id":"1",
         *      "balance":"0.00000000",
         *      "address":"LLHVnrXrQP1sjxNXLrRQTnZmnpc9N33KL6"},
         *      {"currency_id":"2","balance":"0.75000000","address":"1GzUJoStC9CHpzFPBGtZF7D7or9c3PdsG7"},
         *      {"currency_id":"3","balance":"90.00000000","address":null},
         *      {"currency_id":"4","balance":"10.00000000","address":null},
         *      {"currency_id":"5","balance":"0.00000000","address":null}]},
         * "pagination":{"total_items":13,"items_per_page":5,"current_page":1,"total_pages":3}
         * }
         */



        String queryResult = query(API_BASE_URL, path, query_args, false);
        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean errors = true;
            try {
                errors = (boolean) httpAnswerJson.get(TOKEN_ERR);
            } catch (ClassCastException e) {
                errors = true;
            }

            if (errors) {
                //error
                JSONObject errorMessage = (JSONObject) httpAnswerJson.get(TOKEN_ERR);
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage.toJSONString());

                LOG.severe("Ccedk API returned an error: " + errorMessage);

                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct
                JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
                JSONArray entities = (JSONArray) dataJson.get("entities");

                //iterate on all currencies to find what I want
                if (currency == null) { //Get all balances
                    int NBTid = TradeUtils.getCCDKECurrencyId(pair.getOrderCurrency().getCode().toUpperCase());
                    int PEGid = TradeUtils.getCCDKECurrencyId(pair.getPaymentCurrency().getCode().toUpperCase());


                    boolean foundNBT = false;
                    boolean foundPEG = false;

                    Amount NBTTotal = new Amount(-1, pair.getOrderCurrency());
                    Amount PEGTotal = new Amount(-1, pair.getOrderCurrency());
                    for (int i = 0; i < entities.size(); i++) {
                        JSONObject temp = (JSONObject) entities.get(i);
                        int tempid = Integer.parseInt((String) temp.get("currency_id"));
                        if (tempid == NBTid) {
                            foundNBT = true;
                            double tempbalance = Double.parseDouble((String) temp.get("balance"));
                            NBTTotal = new Amount(tempbalance, pair.getOrderCurrency());
                        } else if (tempid == PEGid) {
                            double tempbalance = Double.parseDouble((String) temp.get("balance"));
                            PEGTotal = new Amount(tempbalance, pair.getPaymentCurrency());
                            foundPEG = true;
                        }
                    }

                    if (foundNBT && foundPEG) {
                        //Pack it into the ApiResponse
                        balance = new Balance(NBTTotal, PEGTotal);
                        apiResponse.setResponseObject(balance);
                    } else {
                        apiResponse.setError(new ApiError(ERROR_CURRENCY_NOT_FOUND, ""
                                + "Cannot find a currency with id = " + NBTid + " or " + PEGid));
                    }
                } else { //Specific currency requested
                    int id = TradeUtils.getCCDKECurrencyId(currency.getCode().toUpperCase());
                    boolean found = false;

                    Amount total = new Amount(-1, currency);
                    for (int i = 0; i < entities.size(); i++) {
                        JSONObject temp = (JSONObject) entities.get(i);
                        int tempid = Integer.parseInt((String) temp.get("currency_id"));
                        if (tempid == id) {
                            found = true;
                            double tempbalance = Double.parseDouble((String) temp.get("balance"));
                            total = new Amount(tempbalance, currency);
                        }
                    }

                    if (found) {
                        //Pack it into the ApiResponse
                        apiResponse.setResponseObject(total);
                    } else {
                        apiResponse.setError(new ApiError(ERROR_CURRENCY_NOT_FOUND, ""
                                + "Cannot find a currency with id = " + id));
                    }


                }
            }
        } catch (ParseException ex) {
            LOG.severe(ex.getMessage());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the balance response"));
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
        return enterOrder(Constant.SELL.toLowerCase(), pair, amount, rate);
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        return enterOrder(Constant.BUY.toLowerCase(), pair, amount, rate);
    }

    private ApiResponse enterOrder(String type, CurrencyPair pair, double amount, double price) {
        ApiResponse apiResponse = new ApiResponse();
        String order_id = "";
        HashMap<String, String> query_args = new HashMap<>();
        query_args.put("pair_id", Integer.toString(TradeUtils.getCCDKECurrencyPairId(pair)));
        query_args.put("type", type);
        query_args.put("price", Double.toString(price));
        query_args.put("amount", Double.toString(amount));

        String queryResult = query(API_BASE_URL, API_TRADE, query_args, false);

        /* Sample Answer
         * {"errors":false,
         * "response":
         *     {"entity":
         *         {"order_id":"2011",
         *          "transaction_id":"6517"}}}
         */


        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            boolean errors = true;
            try {
                errors = (boolean) httpAnswerJson.get(TOKEN_ERR);
            } catch (ClassCastException e) {
                errors = true;
            }

            if (errors) {
                //error
                JSONObject errorMessage = (JSONObject) httpAnswerJson.get(TOKEN_ERR);
                ApiError apiErr = new ApiError(ERROR_GENERIC, errorMessage.toJSONString());

                LOG.severe("Ccedk API returned an error: " + errorMessage);

                apiResponse.setError(apiErr);
                return apiResponse;
            } else {
                //correct
                JSONObject dataJson = (JSONObject) httpAnswerJson.get("response");
                JSONObject entity = (JSONObject) dataJson.get("entity");
                order_id = "" + (long) entity.get("order_id");
                apiResponse.setResponseObject(order_id);
            }
        } catch (ParseException ex) {
            LOG.severe(ex.getMessage());
            apiResponse.setError(new ApiError(ERROR_PARSING, "Error while parsing the balance response"));
            return apiResponse;
        }
        return apiResponse;

    }

    @Override
    public ApiResponse getActiveOrders() {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getOrderDetail(String orderID) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse cancelOrder(String orderID) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getTxFee() {
        return getTxFeeImpl();
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        return getTxFeeImpl();
    }

    private ApiResponse getTxFeeImpl() {
        double defaultFee = 0.2;

        if (Global.options != null) {
            return new ApiResponse(true, Global.options.getTxFee(), null);
        } else {
            return new ApiResponse(true, defaultFee, null);
        }
    }

    @Override
    public ApiResponse getPermissions() {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse orderExists(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse clearOrders() {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
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
        CcedkService query = new CcedkService(url, args);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(false, false);
        } else {
            LOG.severe("The bot will not execute the query, there is no connection to ccdek");
            queryResult = "error : no connection with CCEDK";
        }
        return queryResult;
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        CcedkService query = new CcedkService(base, method, args, keys);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, false);
        } else {
            LOG.severe("The bot will not execute the query, there is no connection to ccdek");
            queryResult = "error : no connection with CCEDK";
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

    //DO NOT USE THIS METHOD DIRECTLY, use CREATENONCE
    private long getNonceInternal(String requester) {
        apiBusy = true;
        long currentTime = System.currentTimeMillis() / 1000;
        if (Global.options != null) {
            if (Global.options.isVerbose()) {
                LOG.info(currentTime + " Now apiBusy! req : " + requester);
            }
        }
        long timeElapsedSinceLastCall = currentTime - lastSentTonce;
        if (timeElapsedSinceLastCall < SPACING_BETWEEN_CALLS) {
            try {
                long sleepTime = SPACING_BETWEEN_CALLS;
                Thread.sleep(sleepTime);
                currentTime = System.currentTimeMillis() / 1000;
                if (Global.options != null) {
                    if (Global.options.isVerbose()) {
                        LOG.info("Just slept " + sleepTime + "; req : " + requester);
                    }
                }
            } catch (InterruptedException e) {
                LOG.severe(e.getMessage());
            }
        }

        lastSentTonce = currentTime;
        if (Global.options != null) {
            if (Global.options.isVerbose()) {
                LOG.info("Final tonce to be sent: req : " + requester + " ; Tonce=" + lastSentTonce);
            }
        }
        apiBusy = false;
        return lastSentTonce;
    }

    private class CcedkService implements ServiceInterface {

        protected String base;
        protected String method;
        protected HashMap args;
        protected ApiKeys keys;
        protected String url;

        public CcedkService(String base, String method, HashMap<String, String> args, ApiKeys keys) {
            this.base = base;
            this.method = method;
            this.args = args;
            this.keys = keys;

        }

        private CcedkService(String url, HashMap<String, String> args) {
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
                    args.put("nonce", createNonce(""));
                    post_data = TradeUtils.buildQueryString(args, ENCODING);

                    // args signature with apache cryptografic tools
                    String toHash = post_data;

                    signature = signRequest(keys.getPrivateKey(), toHash);
                }
                // build URL

                URL queryUrl;
                if (needAuth) {
                    queryUrl = new URL(base + method);
                } else {
                    queryUrl = new URL(url);
                }


                connection = (HttpsURLConnection) queryUrl.openConnection();
                if (isGet) {
                    connection.setRequestMethod("GET");
                } else {
                    connection.setRequestMethod("POST");
                }
                // create and setup a HTTP connection

                connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; CCEDK PHP client; " + Global.settings.getProperty("app_name"));

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
                if (connection.getResponseCode() >= 400) {
                    httpError = true;
                    br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
                } else {
                    br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                }

                String output;

                if (httpError) {
                    LOG.severe("Http error - Post Data: " + post_data);
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
                        LOG.severe(ex.getMessage());

                    }
                }
            } //Capture Exceptions
            catch (IllegalStateException ex) {
                LOG.severe(ex.getMessage());

            } catch (NoRouteToHostException | UnknownHostException ex) {
                //Global.BtceExchange.setConnected(false);
                LOG.severe(ex.getMessage());

                answer = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
            } catch (IOException ex) {
                LOG.severe(ex.getMessage());
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
                LOG.severe("Unsupported encoding exception: " + uee.toString());
                return null;
            }

            // Create a new mac
            try {
                mac = Mac.getInstance(SIGN_HASH_FUNCTION);
            } catch (NoSuchAlgorithmException nsae) {
                LOG.severe("No such algorithm exception: " + nsae.toString());
                return null;
            }

            // Init mac with key.
            try {
                mac.init(key);
            } catch (InvalidKeyException ike) {
                LOG.severe("Invalid key exception: " + ike.toString());
                return null;
            }
            try {
                signature = Hex.encodeHexString(mac.doFinal(hash_data.getBytes(ENCODING)));

            } catch (UnsupportedEncodingException ex) {
                LOG.severe(ex.getMessage());
            }
            return signature;
        }
    }
}
