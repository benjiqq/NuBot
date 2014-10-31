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
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OptionsJSON;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.models.Trade;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.BtceWrapper;
import com.nubits.nubot.trading.wrappers.BterWrapper;
import com.nubits.nubot.trading.wrappers.CcedkWrapper;
import com.nubits.nubot.trading.wrappers.CcexWrapper;
import com.nubits.nubot.trading.wrappers.PeatioWrapper;
import com.nubits.nubot.trading.wrappers.PoloniexWrapper;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
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
    private static final String TEST_OPTIONS_PATH = "options.json";
    //These are the key pair associated with desrever's test account on btc-e

    public static void main(String[] args) {
        //Load settings
        Utils.loadProperties("settings.properties");
        init();
        Global.options = OptionsJSON.parseOptions(TEST_OPTIONS_PATH);

        configExchange(Constant.CCEDK); //Replace to test a differe API implementation

        runTests();
        System.exit(0);
    }

    public static void runTests() {
        //Methods strictly necessary for NuBot to run---------------
        //---------------
        testGetAvailableBalance(Constant.NBT); //
        //testGetAvailableBalances(Constant.BTC_NBT);
        //testGetActiveOrders(Constant.BTC_NBT)
        //testGetActiveOrders(); //Try with 0 active orders also . for buy orders, check in which currency is the amount returned.
        //testSell(0.3, 0.00830509, Constant.NBT_BTC);  //ok
        //testBuy(1, 0.000199999, Constant.NBT_BTC);  //ok
        //testCancelOrder("4678290", Constant.BTC_NBT);
        //testClearAllOrders(Constant.NBT_BTC);
        testIsOrderActive("41496587");
        //testGetTxFee();
        //testGetTxFeeWithArgs(Constant.BTC_USD);
        //Methods NOT strucly necessary for NuBot to run---------------
        //---------------
        //testGetLastPrice(Constant.NBT_BTC);
        //testGetOrderDetail("681944811"); //Try getting an existing order,  a non-existing order, and putting a wrong id "DKos3"
        //testGetLastTrades(Constant.NBT_BTC);
        //testGetLastTrades(Constant.BTC_NBT, 1409566800);


        /*for (int i = 0; i < 5000; i++) {
         LOG.info(TradeUtils.getCCDKEvalidNonce());
         try {
         Thread.sleep(300);
         } catch (InterruptedException ex) {
         Logger.getLogger(TestWrappers.class.getName()).log(Level.SEVERE, null, ex);
         }
         }*/


        //stimulating ccedk wrong nonce



        for (int i = 0; i < 5000; i++) {
            testGetActiveOrders();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestWrappers.class.getName()).log(Level.SEVERE, null, ex);
            }

            testGetAvailableBalances(Constant.NBT_PPC);

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestWrappers.class.getName()).log(Level.SEVERE, null, ex);
            }
            testGetOrderDetail("3454");

            try {
                Thread.sleep(300);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestWrappers.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static void testGetAvailableBalances(CurrencyPair pair) {
        //Get all the balances  associated with the account
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(pair);
        if (balancesResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance() ");
            Balance balance = (Balance) balancesResponse.getResponseObject();

            LOG.info(balance.toString());

        } else {
            LOG.severe(balancesResponse.getError().toString());
        }
    }

    private static void testGetAvailableBalance(Currency cur) {
        //Get the USD balance associated with the account
        ApiResponse balanceResponse = Global.exchange.getTrade().getAvailableBalance(cur);
        if (balanceResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance(CurrencyPair pair) ");
            Amount balance = (Amount) balanceResponse.getResponseObject();

            LOG.info(balance.toString());
        } else {
            LOG.severe(balanceResponse.getError().toString());
        }
    }

    private static void testGetLastPrice(CurrencyPair pair) {
        //Get lastPrice for a given CurrencyPair
        ApiResponse lastPriceResponse = Global.exchange.getTrade().getLastPrice(pair);
        if (lastPriceResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getLastPrice(CurrencyPair pair) ");
            Ticker ticker = (Ticker) lastPriceResponse.getResponseObject();
            LOG.info("Last price : 1" + Constant.BTC_USD.getOrderCurrency().getCode() + " = "
                    + ticker.getLast() + " " + Constant.BTC_USD.getPaymentCurrency().getCode());
            LOG.info("ask  : 1" + Constant.BTC_USD.getOrderCurrency().getCode() + " = "
                    + ticker.getAsk() + " " + Constant.BTC_USD.getPaymentCurrency().getCode());
            LOG.info("bid  : 1" + Constant.BTC_USD.getOrderCurrency().getCode() + " = "
                    + ticker.getBid() + " " + Constant.BTC_USD.getPaymentCurrency().getCode());

        } else {
            LOG.severe(lastPriceResponse.getError().toString());
        }

    }

    private static void testSell(double amountSell, double priceSell, CurrencyPair pair) {
        //Place a sell order


        ApiResponse sellResponse = Global.exchange.getTrade().sell(pair, amountSell, priceSell);
        if (sellResponse.isPositive()) {

            LOG.info("\nPositive response  from TradeInterface.sell(...) ");
            LOG.warning("Strategy : Submit order : "
                    + "sell" + amountSell + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceSell + " " + pair.getPaymentCurrency().getCode());

            String sellResponseString = (String) sellResponse.getResponseObject();
            LOG.info("Response = " + sellResponseString);
        } else {
            LOG.severe(sellResponse.getError().toString());
        }
    }

    private static void testBuy(double amountBuy, double priceBuy, CurrencyPair pair) {
        //Place a buy order

        ApiResponse buyResponse = Global.exchange.getTrade().buy(pair, amountBuy, priceBuy);
        if (buyResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.buy(...) ");
            LOG.info(": Submit order : "
                    + "buy" + amountBuy + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceBuy + " " + pair.getPaymentCurrency().getCode());
            String buyResponseString = (String) buyResponse.getResponseObject();
            LOG.info("Response = " + buyResponseString);

        } else {
            LOG.severe(buyResponse.getError().toString());
        }
    }

    private static void testGetActiveOrders() {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders();
        if (activeOrdersResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getActiveOrders() ");
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

            LOG.info("Active orders : " + orderList.size());
            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                LOG.info(tempOrder.toString());
            }

        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
        }
    }

    private static void testGetActiveOrders(CurrencyPair pair) {
        //Get active orders associated with a specific CurrencyPair
        ApiResponse activeOrdersUSDNTBResponse = Global.exchange.getTrade().getActiveOrders(pair);
        if (activeOrdersUSDNTBResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getActiveOrders(CurrencyPair pair) ");
            ArrayList<Order> orderListUSDNBT = (ArrayList<Order>) activeOrdersUSDNTBResponse.getResponseObject();

            LOG.info("Active orders : " + orderListUSDNBT.size());
            for (int i = 0; i < orderListUSDNBT.size(); i++) {
                Order tempOrder = orderListUSDNBT.get(i);
                LOG.info(tempOrder.toString());
            }
        } else {
            LOG.severe(activeOrdersUSDNTBResponse.getError().toString());
        }
    }

    private static void testGetOrderDetail(String order_id_detail) {
        //Get the order details for a specific order_id
        ApiResponse orderDetailResponse = Global.exchange.getTrade().getOrderDetail(order_id_detail);
        if (orderDetailResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getOrderDetail(id) ");
            Order order = (Order) orderDetailResponse.getResponseObject();
            LOG.info(order.toString());
        } else {
            LOG.info(orderDetailResponse.getError().toString());
        }
    }

    private static void testCancelOrder(String order_id_delete, CurrencyPair pair) {
        //Cancel an order
        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(order_id_delete, pair);
        if (deleteOrderResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrderResponse.getResponseObject();

            if (deleted) {
                LOG.info("Order deleted succesfully");
            } else {
                LOG.info("Could not delete order");
            }

        } else {
            LOG.severe(deleteOrderResponse.getError().toString());
        }
    }

    private static void testGetTxFee() {
        //Get current trascation fee
        ApiResponse txFeeResponse = Global.exchange.getTrade().getTxFee();
        if (txFeeResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getTxFee()");
            double txFee = (Double) txFeeResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFee + "%");
        } else {
            LOG.severe(txFeeResponse.getError().toString());
        }
    }

    private static void testGetTxFeeWithArgs(CurrencyPair pair) {
        //Get the current transaction fee associated with a specific CurrencyPair
        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(pair);
        if (txFeeNTBUSDResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getTxFee(CurrencyPair pair)");
            double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFeeUSDNTB + "%");
        } else {
            LOG.severe(txFeeNTBUSDResponse.getError().toString());
        }
    }

    private static void testIsOrderActive(String orderId) {
        //Check if orderId is active
        ApiResponse orderDetailResponse = Global.exchange.getTrade().isOrderActive(orderId);
        if (orderDetailResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.isOrderActive(id) ");
            boolean exist = (boolean) orderDetailResponse.getResponseObject();
            LOG.info("Order " + orderId + "  active? " + exist);
        } else {
            LOG.severe(orderDetailResponse.getError().toString());
        }
    }

    private static void testClearAllOrders(CurrencyPair pair) {
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(pair);
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

            if (deleted) {
                LOG.info("Order clear request succesfully");
            } else {
                LOG.info("Could not submit request to clear orders");
            }

        } else {
            LOG.severe(deleteOrdersResponse.getError().toString());
        }
    }

    private static void testGetLastTrades(CurrencyPair pair) {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair);
        if (activeOrdersResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getLastTrades(pair) ");
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            LOG.info("Last 24h trades : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);
                LOG.info(tempTrade.toString());
            }
        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
        }
    }

    private static void testGetLastTrades(CurrencyPair pair, long startTime) {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair, startTime);
        if (activeOrdersResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getLastTrades(pair,startTime) ");
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            LOG.info("Last trades from " + startTime + " : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);
                LOG.info(tempTrade.toString());
            }
        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
        }
    }

    private static void init() {
        String folderName = "testwrappers_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;
        //Create log dir
        FileSystem.mkdir(logsFolder);
        try {
            NuLogger.setup(false, logsFolder);
        } catch (IOException ex) {
            LOG.severe(ex.toString());
        }

        System.setProperty("javax.net.ssl.trustStore", Global.settings.getProperty("keystore_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", Global.settings.getProperty("keystore_pass"));
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
            keys = new ApiKeys(Passwords.INTERNAL_PEATIO_SECRET, Passwords.INTERNAL_PEATIO_KEY);

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
        } else if (exchangeName.equals(Constant.BTER)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.BTER_SECRET, Passwords.BTER_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new BterWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.POLONIEX)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.POLONIEX_SECRET, Passwords.POLONIEX_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new PoloniexWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.CCEX)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.CCEX_SECRET, "");

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new CcexWrapper(keys, Global.exchange));
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
            LOG.severe(ex.toString());
        }

        /* Setup (end) ------------------------------------------------------------------------------------------------------ */
    }
}
