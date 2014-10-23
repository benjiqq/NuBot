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
import com.nubits.nubot.utils.Utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CcexWrapper implements TradeInterface {

    private ApiKeys keys;
    private Exchange exchange;
    private String checkConnectionUrl = "https://c-cex.com/";
    private static final Logger LOG = Logger.getLogger(CcexWrapper.class.getName());
    //Entry point(s)
    private final String API_BASE = "https://c-cex.com/t/r.html?";
    private String baseUrl;
    //Errors
    private ArrayList<ApiError> errors;
    private final String TOKEN_ERR = "error";
    private final int ERROR_NO_CONNECTION = 16561;
    private final int ERROR_GENERIC = 16562;
    private final int ERROR_PARSING = 16563;
    private final int ERROR_UNKNOWN = 16560;
    private final int ERROR_AUTH = 16561;

    public CcexWrapper(ApiKeys keys, Exchange exchange) {
        this.keys = keys;
        this.exchange = exchange;
        this.baseUrl = API_BASE + "key=" + keys.getPrivateKey();

        setupErrors();
    }

    public CcexWrapper() {
        setupErrors();
    }

    private void setupErrors() {
        errors = new ArrayList<ApiError>();
        errors.add(new ApiError(ERROR_NO_CONNECTION, "Failed to connect to the exchange entrypoint. Verify your connection"));
        errors.add(new ApiError(ERROR_PARSING, "Parsing error"));
        errors.add(new ApiError(ERROR_AUTH, "Wrong API key. Please verify"));

    }

    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        return getBalanceImpl(pair, null);
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        return getBalanceImpl(null, currency);
    }

    public ApiResponse getBalanceImpl(CurrencyPair pair, Currency currency) {
        ApiResponse apiResponse = new ApiResponse();
        Balance balance = new Balance();
        String url = baseUrl + "&a=getbalance";

        String queryResult = query(url, new HashMap<String, String>(), true);

        if (queryResult.startsWith(TOKEN_ERR)) {
            apiResponse.setError(getErrorByCode(ERROR_NO_CONNECTION));
            return apiResponse;
        }
        if (queryResult.startsWith("Access denied")) {
            apiResponse.setError(getErrorByCode(ERROR_AUTH));
            return apiResponse;
        }


        /*Sample result
         *
         * {"return":
         *  [
         *   {"usd":0},
         *   {"btc":0.04666311},
         *   ...
         *  ]
         * }
         *
         */

        JSONParser parser = new JSONParser();
        try {
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(queryResult));
            JSONArray balanceArray = (JSONArray) httpAnswerJson.get("return");

            if (currency != null) {
                //looking for a specific currency
                String lookingFor = currency.getCode().toLowerCase();
                boolean found = false;
                for (int i = 0; i < balanceArray.size(); i++) {
                    JSONObject tempElement = (JSONObject) balanceArray.get(i);
                    if (tempElement.containsKey(lookingFor)) {
                        found = true;
                        double foundBalance = Utils.getDouble(tempElement.get(lookingFor));
                        apiResponse.setResponseObject(new Amount(foundBalance, currency));
                    }
                }
                if (!found) {
                    //Specific currency not found
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

                Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency()),
                        NBTonOrder = new Amount(0, pair.getOrderCurrency());

                String NBTcode = pair.getOrderCurrency().getCode().toLowerCase();
                String PEGcode = pair.getPaymentCurrency().getCode().toLowerCase();


                for (int i = 0; i < balanceArray.size(); i++) {
                    JSONObject tempElement = (JSONObject) balanceArray.get(i);
                    if (tempElement.containsKey(NBTcode)) {

                        double tempAvailablebalance = Utils.getDouble(tempElement.get(NBTcode));
                        double tempLockedebalance = 0; //Not provided by the API

                        NBTAvail = new Amount(tempAvailablebalance, pair.getOrderCurrency());
                        NBTonOrder = new Amount(tempLockedebalance, pair.getOrderCurrency());

                        foundNBTavail = true;
                    } else if (tempElement.containsKey(PEGcode)) {
                        double tempAvailablebalance = Utils.getDouble(tempElement.get(PEGcode));
                        double tempLockedebalance = 0; //Not provided by the API

                        PEGAvail = new Amount(tempAvailablebalance, pair.getPaymentCurrency());
                        PEGonOrder = new Amount(tempLockedebalance, pair.getPaymentCurrency());

                        foundPEGavail = true;
                    }
                }

                balance = new Balance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);

                apiResponse.setResponseObject(balance);
                if (!foundNBTavail || !foundPEGavail) {
                    LOG.warning("Cannot find a balance for currency with code "
                            + "" + NBTcode + " or " + PEGcode + " in your balance. "
                            + "NuBot assumes that balance is 0");
                }
            }
        } catch (ParseException ex) {
            LOG.severe("httpresponse: " + queryResult + " \n" + ex.getMessage());
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
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
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
    public ApiResponse cancelOrder(String orderID, CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
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
        LOG.warning("CCex uses global TX fee, currency pair not supprted. \n"
                + "now calling getTxFee()");
        return getTxFee();
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse getLastTrades(CurrencyPair pair, long startTime) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse isOrderActive(String id) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ApiResponse clearOrders(CurrencyPair pair) {
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
        CcexService query = new CcexService(url);
        String queryResult = getErrorByCode(ERROR_NO_CONNECTION).getDescription();
        if (exchange.getLiveData().isConnected()) {
            queryResult = query.executeQuery(true, isGet);
        } else {
            LOG.severe("The bot will not execute the query, there is no connection to CCex");
            queryResult = "error : no connection with CCex";
        }


        return queryResult;
    }

    @Override
    public String query(String base, String method, HashMap<String, String> args, boolean isGet) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //TODO change body of generated methods, choose Tools | Templates.


    }

    private class CcexService implements ServiceInterface {

        protected ApiKeys keys;
        protected String url;

        private CcexService(String url) {
            //Used for ticker, does not require auth
            this.url = url;

        }

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
            String answer = "";

            // add header
            Header[] headers = new Header[1];
            headers[ 0] = new BasicHeader("Content-type", "application/x-www-form-urlencoded");

            URL queryUrl;
            try {
                queryUrl = new URL(url);
            } catch (MalformedURLException ex) {
                LOG.severe(ex.getMessage());
            }

            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = null;
            HttpGet get = null;
            HttpResponse response = null;


            try {
                get = new HttpGet(url);
                get.setHeaders(headers);
                response = client.execute(get);

            } catch (Exception e) {
                LOG.severe(e.getMessage());
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
                Logger.getLogger(BterWrapper.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalStateException ex) {
                Logger.getLogger(BterWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (Global.options
                    != null && Global.options.isVerbose()) {

                LOG.fine("\nSending request to URL : " + url + " ; get = " + isGet);
                if (post != null) {
                    System.out.println("Post parameters : " + post.getEntity());
                }
                LOG.fine("Response Code : " + response.getStatusLine().getStatusCode());
                LOG.fine("Response :" + response);

            }
            return answer;
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
