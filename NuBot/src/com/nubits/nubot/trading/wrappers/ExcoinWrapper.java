package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.ApiError;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.trading.ServiceInterface;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.ErrorManager;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.*;
import java.util.*;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by sammoth on 30/11/14.
 */
public class ExcoinWrapper implements TradeInterface{

    private static final Logger LOG = Logger.getLogger(ExcoinWrapper.class.getName());
    //Class fields
    private ApiKeys keys;
    private Exchange exchange;
    private final String SIGN_HASH_FUNCTION = "HmacSHA256";
    private final String ENCODING = "UTF-8";
    private final int EXPIRE_TIMESTAMP = 900; //15 minutes
    //API Paths
    private final String API_BASE_URL = "https://api.exco.in/v1/account";
    private final String API_SUMMARY = "summary";
    private final String API_TRADES = "trades";
    private final String API_ORDERS = "orders";


    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        return null;
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
    public ApiResponse clearOrders(CurrencyPair pair) {
        return null;
    }

    @Override
    public ApiError getErrorByCode(int code) {
        return null;
    }

    @Override
    public String getUrlConnectionCheck() {
        return null;
    }

    @Override
    public String query(String url, HashMap<String, String> args, boolean isGet) {
        return null;
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

    }

    @Override
    public void setExchange(Exchange exchange) {

    }

    @Override
    public void setApiBaseUrl(String apiBaseUrl) {

    }

    private class ExcoinService implements ServiceInterface {

        @Override
        public String executeQuery(boolean needAuth, boolean isGet) {
            return null;
        }

        @Override
        public String signRequest(String secret, String hash_data) {
            return null;
        }
    }
}
