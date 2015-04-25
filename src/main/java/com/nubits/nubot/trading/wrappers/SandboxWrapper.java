package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.*;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.Utils;

import java.util.AbstractMap;

/**
 * A wrapper to simulate a dummy exchange response
 */
public class SandboxWrapper implements TradeInterface {

    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();

        Amount NBTAvail = new Amount(0, pair.getOrderCurrency());
        Amount PEGAvail = new Amount(0, pair.getPaymentCurrency());

        Amount PEGonOrder = new Amount(0, pair.getPaymentCurrency());
        Amount NBTonOrder = new Amount(0, pair.getOrderCurrency());

        PairBalance balance = new PairBalance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
        apiResponse.setResponseObject(balance);
        return apiResponse;
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        ApiResponse apiResponse = new ApiResponse();

        double balanceD = 0;
        apiResponse.setResponseObject(new Amount(balanceD, currency));
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
        return null;
    }

    @Override
    public String query(String base, String method, AbstractMap<String, String> args, boolean needAuth, boolean isGet) {
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
}
