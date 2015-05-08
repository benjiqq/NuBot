package com.nubits.nubot.trading.wrappers;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.*;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;

/**
 * A wrapper to simulate a dummy exchange response
 * uses internal data for mockup data: balances, orders
 */
public class SimulationWrapper implements TradeInterface {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationWrapper.class.getName());

    public SimulationWrapper(ApiKeys keys, Exchange exchange) {

    }

    //dummy data
    ArrayList<Order> activeOrderList;

    private void addDummyOrder() {
        Order order = new Order();

        /* {"orderNumber":"120466","type":"sell","rate":"0.025","amount":"100","total":"2.5" */

        order.setType("sell");
        order.setId("123");
        order.setAmount(new Amount(100.0, CurrencyList.NBT));
        order.setPrice(new Amount(0.004, CurrencyList.BTC));
        order.setCompleted(false);
        order.setInsertedDate(new Date());
        CurrencyPair pair = new CurrencyPair(CurrencyList.NBT, CurrencyList.BTC);
        order.setPair(pair);
        //order.setInsertedDate(new Date()); //Not provided
        activeOrderList.add(order);
    }

    @Override
    public ApiResponse getAvailableBalances(CurrencyPair pair) {
        ApiResponse apiResponse = new ApiResponse();

        Amount NBTAvail = new Amount(50, pair.getOrderCurrency());
        Amount PEGAvail = new Amount(1, pair.getPaymentCurrency());

        Amount PEGonOrder = new Amount(50, pair.getPaymentCurrency());
        Amount NBTonOrder = new Amount(1, pair.getOrderCurrency());

        PairBalance balance = new PairBalance(PEGAvail, NBTAvail, PEGonOrder, NBTonOrder);
        apiResponse.setResponseObject(balance);
        return apiResponse;
    }

    @Override
    public ApiResponse getAvailableBalance(Currency currency) {
        ApiResponse apiResponse = new ApiResponse();

        double balance = 10;
        if (currency.equals(CurrencyList.BTC))
            balance = 1;

        apiResponse.setResponseObject(new Amount(balance, currency));
        return apiResponse;
    }

    @Override
    public ApiResponse getLastPrice(CurrencyPair pair) {

        return null;
    }

    @Override
    public ApiResponse sell(CurrencyPair pair, double amount, double rate) {
        ApiResponse apiResponse = new ApiResponse();
        String order_id = "123";
        apiResponse.setResponseObject(order_id);
        return null;
    }

    @Override
    public ApiResponse buy(CurrencyPair pair, double amount, double rate) {
        ApiResponse apiResponse = new ApiResponse();
        String order_id = "123";
        apiResponse.setResponseObject(order_id);
        return null;
    }

    @Override
    public ApiResponse getActiveOrders() {
        ApiResponse apiResponse = new ApiResponse();
        activeOrderList = new ArrayList<Order>();
        addDummyOrder();

        apiResponse.setResponseObject(activeOrderList);
        return apiResponse;
    }

    @Override
    public ApiResponse getActiveOrders(CurrencyPair pair) {

        ApiResponse apiResponse = new ApiResponse();
        ArrayList<Order> orderList = new ArrayList<Order>();

        Order order = new Order();

        /* {"orderNumber":"120466","type":"sell","rate":"0.025","amount":"100","total":"2.5" */

        order.setType("sell");
        order.setId("123");
        order.setAmount(new Amount(100.0, CurrencyList.NBT));
        order.setPrice(new Amount(0.004, CurrencyList.BTC));
        order.setCompleted(false);
        order.setPair(pair);
        //order.setInsertedDate(new Date()); //Not provided
        orderList.add(order);

        apiResponse.setResponseObject(orderList);
        return apiResponse;
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
        return new ApiResponse(true, 0.0, null);
    }

    @Override
    public ApiResponse getTxFee(CurrencyPair pair) {
        return new ApiResponse(true, 0.0, null);
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
        LOG.debug("simuatlion. clear orders called for pair " + pair);
        //clear orders
        activeOrderList = new ArrayList<Order>();
        ApiResponse toReturn = new ApiResponse();
        toReturn.setResponseObject(true);
        return toReturn;
    }

    @Override
    public ApiError getErrorByCode(int code) {
        return null;
    }

    @Override
    public String getUrlConnectionCheck() {
        return "http://www.google.com";
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
