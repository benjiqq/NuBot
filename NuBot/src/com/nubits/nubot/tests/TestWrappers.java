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
package com.nubits.nubot.tests;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.global.Passwords;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OptionsJSON;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.keys.ApiPermissions;
import com.nubits.nubot.trading.wrappers.BtceWrapper;
import com.nubits.nubot.trading.wrappers.CcedkWrapper;
import com.nubits.nubot.trading.wrappers.PeatioWrapper;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class TestWrappers {

    private static final Logger LOG = Logger.getLogger(TestWrappers.class.getName());
    private static final String TEST_OPTIONS_PATH = "res/options/test/options-reduced.json";
    //These are the key pair associated with desrever's test account on btc-e

    public static void main(String[] args) {
        init();
        Global.options = OptionsJSON.parseOptions(TEST_OPTIONS_PATH);

        configExchange(Constant.CCEDK); //Replace to test a differe API implementation

        runTests();

        System.exit(0);
    }

    public static void runTests() {
        //testGetPermissions();
        //testGetBalances();
        //testGetBalanceWithArgs();
        //testGetLastPrice();
        //testSell();
        testBuy();
        //testGetOrders();
        //testGetOrdersWithArgs();
        //testGetOrderDetail();
        //testCancelOrder();
        //testGetTxFee();
        //testGetTxFeeWithArgs();
        //testClearAllOrders();
        //testIsOrderActive("21312");
        //testIsOrderActive("362849485");
    }

    private static void testGetPermissions() {
        //Test if the given apikey have permissions to trade and getinfo

        ApiResponse permissionResponse = Global.exchange.getTrade().getPermissions();
        if (permissionResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.getPermissions() ");
            ApiPermissions permissions = (ApiPermissions) permissionResponse.getResponseObject();

            LOG.fine("Keys Valid :" + permissions.isValid_keys() + "\n"
                    + "getinfo : " + permissions.isGet_info() + "\n"
                    + "trade : " + permissions.isTrade());
        } else {
            LOG.severe(permissionResponse.getError().toString());
        }
    }

    private static void testGetBalances() {
        //Get all the balances  associated with the account
        CurrencyPair pair = new CurrencyPair(Constant.PPC, Constant.USD);
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(pair);
        if (balancesResponse.isPositive()) {
            LOG.info("Positive response  from TradeInterface.getBalance() ");
            Balance balance = (Balance) balancesResponse.getResponseObject();

            LOG.info("PEGs Balance = " + balance.getPEGBalance().getQuantity() + " " + balance.getPEGBalance().getCurrency().getCode() + "\n"
                    + "NBT Balance = " + balance.getNubitsBalance().getQuantity() + " " + balance.getNubitsBalance().getCurrency().getCode());

        } else {
            LOG.severe(balancesResponse.getError().toString());
        }
    }

    private static void testGetBalanceWithArgs() {
        //Get the USD balance associated with the account
        ApiResponse USDBalancesResponse = Global.exchange.getTrade().getAvailableBalance(Constant.USD);
        if (USDBalancesResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.getBalance(CurrencyPair pair) ");
            Amount USDBalance = (Amount) USDBalancesResponse.getResponseObject();

            LOG.fine("USD Balance " + USDBalance.getQuantity() + " " + USDBalance.getCurrency().getSymbol());
        } else {
            LOG.severe(USDBalancesResponse.getError().toString());
        }
    }

    private static void testGetLastPrice() {
        //Get lastPrice for a given CurrencyPair
        ApiResponse lastPriceResponse = Global.exchange.getTrade().getLastPrice(Constant.BTC_USD);
        if (lastPriceResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.getLastPrice(CurrencyPair pair) ");
            Ticker ticker = (Ticker) lastPriceResponse.getResponseObject();
            LOG.fine("Last price : 1" + Constant.BTC_USD.getOrderCurrency().getCode() + " = "
                    + ticker.getLast() + " " + Constant.BTC_USD.getPaymentCurrency().getCode());
            LOG.fine("ask  : 1" + Constant.BTC_USD.getOrderCurrency().getCode() + " = "
                    + ticker.getAsk() + " " + Constant.BTC_USD.getPaymentCurrency().getCode());
            LOG.fine("bid  : 1" + Constant.BTC_USD.getOrderCurrency().getCode() + " = "
                    + ticker.getBid() + " " + Constant.BTC_USD.getPaymentCurrency().getCode());

        } else {
            LOG.severe(lastPriceResponse.getError().toString());
        }

    }

    private static void testSell() {
        //Place a sell order

        double amountSell = 1;
        double priceSell = 0.005;
        CurrencyPair pair = Constant.PPC_BTC;

        ApiResponse sellResponse = Global.exchange.getTrade().sell(pair, amountSell, priceSell);
        if (sellResponse.isPositive()) {

            LOG.info("Positive response  from TradeInterface.sell(...) ");
            LOG.warning("Strategy : Submit order : "
                    + "sell" + amountSell + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceSell + " " + pair.getPaymentCurrency().getCode());

            String sellResponseString = (String) sellResponse.getResponseObject();
            LOG.info("Response = " + sellResponseString);
        } else {
            LOG.severe(sellResponse.getError().toString());
        }
    }

    private static void testBuy() {
        //Place a buy order
        double amountBuy = 1;
        double priceBuy = 1;
        CurrencyPair pair = Constant.PPC_BTC;

        ApiResponse buyResponse = Global.exchange.getTrade().buy(pair, amountBuy, priceBuy);
        if (buyResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.buy(...) ");
            LOG.warning("Strategy : Submit order : "
                    + "buy" + amountBuy + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceBuy + " " + pair.getPaymentCurrency().getCode());
            String buyResponseString = (String) buyResponse.getResponseObject();
            LOG.fine("Response = " + buyResponseString);

        } else {
            LOG.severe(buyResponse.getError().toString());
        }
    }

    private static void testGetOrders() {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders();
        if (activeOrdersResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.getActiveOrders() ");
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

            LOG.fine("Active orders : " + orderList.size());
            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                LOG.fine(tempOrder.toString());
            }

        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
        }
    }

    private static void testGetOrdersWithArgs() {
        //Get active orders associated with a specific CurrencyPair
        ApiResponse activeOrdersUSDNTBResponse = Global.exchange.getTrade().getActiveOrders(Constant.PPC_USD);
        if (activeOrdersUSDNTBResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.getActiveOrders(CurrencyPair pair) ");
            ArrayList<Order> orderListUSDNBT = (ArrayList<Order>) activeOrdersUSDNTBResponse.getResponseObject();

            LOG.fine("Active orders : " + orderListUSDNBT.size());
            for (int i = 0; i < orderListUSDNBT.size(); i++) {
                Order tempOrder = orderListUSDNBT.get(i);
                LOG.fine(tempOrder.toString());
            }
        } else {
            LOG.severe(activeOrdersUSDNTBResponse.getError().toString());
        }
    }

    private static void testGetOrderDetail() {
        //Get the order details for a specific order_id
        String order_id_detail = "362840306";
        ApiResponse orderDetailResponse = Global.exchange.getTrade().getOrderDetail(order_id_detail);
        if (orderDetailResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.getOrderDetail(id) ");
            Order order = (Order) orderDetailResponse.getResponseObject();
            LOG.fine(order.toString());
        } else {
            LOG.severe(orderDetailResponse.getError().toString());
        }
    }

    private static void testCancelOrder() {
        //Cancel an order
        String order_id_delete = "319991270";
        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(order_id_delete);
        if (deleteOrderResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrderResponse.getResponseObject();

            if (deleted) {
                LOG.fine("Order deleted succesfully");
            } else {
                LOG.fine("Could not delete order");
            }

        } else {
            LOG.severe(deleteOrderResponse.getError().toString());
        }
    }

    private static void testGetTxFee() {
        //Get current trascation fee
        ApiResponse txFeeResponse = Global.exchange.getTrade().getTxFee();
        if (txFeeResponse.isPositive()) {
            LOG.info("Positive response  from TradeInterface.getTxFee()");
            double txFee = (Double) txFeeResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFee + "%");
        } else {
            LOG.severe(txFeeResponse.getError().toString());
        }
    }

    private static void testGetTxFeeWithArgs() {
        //Get the current transaction fee associated with a specific CurrencyPair
        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(Constant.BTC_USD);
        if (txFeeNTBUSDResponse.isPositive()) {
            LOG.info("Positive response  from TradeInterface.getTxFee(CurrencyPair pair)");
            double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFeeUSDNTB + "%");
        } else {
            LOG.severe(txFeeNTBUSDResponse.getError().toString());
        }
    }

    private static void testIsOrderActive(String orderId) {
        //Check if orderId is active
        ApiResponse orderDetailResponse = Global.exchange.getTrade().orderExists(orderId);
        if (orderDetailResponse.isPositive()) {
            LOG.fine("Positive response  from TradeInterface.orderExists(id) ");
            boolean exist = (boolean) orderDetailResponse.getResponseObject();
            LOG.fine("Order " + orderId + "  exist? " + exist);
        } else {
            LOG.severe(orderDetailResponse.getError().toString());
        }
    }

    private static void testClearAllOrders() {
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders();
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

            if (deleted) {
                LOG.fine("Order clear request succesfully");
            } else {
                LOG.fine("Could not submit request to clear orders");
            }

        } else {
            LOG.severe(deleteOrdersResponse.getError().toString());
        }
    }

    private static void init() {
        try {
            NuLogger.setup(true);
        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        LOG.setLevel(Level.FINE);

        System.setProperty("javax.net.ssl.trustStore", Settings.KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.trustStorePassword", Settings.KEYSTORE_PWD);
    }

    public static void configExchange(String exchangeName) {
        ApiKeys keys;


        Global.exchange = new Exchange(exchangeName);

        //Create e ExchangeLiveData object to accomodate liveData from the Global.exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        Global.options.setExchangeName(exchangeName);

        if (exchangeName.equals(Constant.BTCE)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.BTCE_SECRET, Passwords.BTCE_KEY);
            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange

            Global.exchange.setTrade(new BtceWrapper(keys, Global.exchange));

        } else if (exchangeName.equals(Constant.PEATIO_BTCCNY)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.PEATIO_SECRET, Passwords.PEATIO_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new PeatioWrapper(keys, Global.exchange, Constant.PEATIO_BTCCNY_API_BASE));

        } else if (exchangeName.equals(Constant.PEATIO_MULTIPAIR)) {
            LOG.severe("Exchange " + exchangeName + " not supported");
            System.exit(0);
        } else if (exchangeName.equals(Constant.CCEDK)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.CCEDK_SECRET, Passwords.CCEDK_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new CcedkWrapper(keys, Global.exchange));
        } else {
            LOG.severe("Exchange " + exchangeName + " not supported");
            System.exit(0);
        }

        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());

        //Create a TaskManager and
        Global.taskManager = new TaskManager();
        //Start checking for connection
        Global.taskManager.getCheckConnectionTask().start();


        //Wait a couple of seconds for the connectionThread to get live
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.severe(ex.getMessage());
        }

        /* Setup (end) ------------------------------------------------------------------------------------------------------ */
    }
}
