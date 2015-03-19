package com.nubits.nubot.testsmanual;

/*
 * Copyright (C) 2015 Nu Development Team
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

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by woolly_sammoth on 24/01/15.
 */
public class TestWrapperReturns {

    private static final Logger LOG = LoggerFactory.getLogger(TestWrapperReturns.class.getName());
    /**
     * Configure tests
     */
    //private static final String TEST_OPTIONS_PATH = "res/options/private/old/options-full.json";
    private static final String TEST_OPTIONS_PATH = "options.json";
    public static ArrayList<String> testExchanges = new ArrayList<>();
    public static CurrencyPair testPair = CurrencyList.NBT_BTC;
    public static final double testNBTAmount = 1;
    public static final double sellPrice = 0.04;
    public static final double buyPrice = 0.0004;

    public static void main(String[] args) {
        //Load settings
        try{
            Utils.loadProperties("settings.properties");
        }catch(IOException e){

        }
        init();


        try {
            Global.options = ParseOptions.parseOptionsSingle(TEST_OPTIONS_PATH);
        } catch (NuBotConfigException ex) {
            LOG.error("" +  ex);
        }
        testExchanges = populateExchanges();

        //configExchange(Constant.BTER);
        //getOpenOrders();
        //runTests();

        for (Iterator<String> exchange = testExchanges.iterator(); exchange.hasNext();) {
            String testExchange = exchange.next();
            try{
                WrapperTestUtils.configExchange(testExchange);
                runTests();
            }catch(NuBotConfigException ex){

            }

        }

    }

    private static void runTests() {
        if (Global.exchange.getName().equals(ExchangeFacade.EXCOIN)) {
            testPair = CurrencyList.BTC_NBT;
        } else {
            testPair = CurrencyList.NBT_BTC;
        }
        print("Testing " + Global.exchange.getName());
        clearOrders(testPair);
        String order_id;
        print("SELL " + testNBTAmount + " NBT @ " + sellPrice + " BTC");
        order_id = sell(testPair, testNBTAmount, sellPrice);
        sleep();
        if (order_id != null) {
            Order order = getOrderDetail(order_id);
            Amount amount = order.getAmount();
            if (!amount.getCurrency().equals(testPair.getOrderCurrency())) {
                print("BAD - Amount not listed in " + testPair.getOrderCurrency().getCode() + "\nReturned " + amount.getCurrency().getCode());
            } else if (amount.getQuantity() != testNBTAmount) {
                print("BAD - Amount Quantity does not equal " + Double.toString(testNBTAmount));
            } else {
                print("Amount OK");
            }
            Amount price = order.getPrice();
            if (!price.getCurrency().equals(testPair.getPaymentCurrency())) {
                print("BAD - Price not listed in " + testPair.getPaymentCurrency().getCode());
            } else if (price.getQuantity() != sellPrice) {
                print("BAD - Price Quantity does not equal " + Double.toString(sellPrice));
            } else {
                print("Price OK");
            }
            cancelOrder(order_id, testPair);
        } else {
            print("BAD - Order not placed");
        }

        print("BUY " + testNBTAmount + " NBT @ " + buyPrice + " BTC");
        order_id = buy(testPair, testNBTAmount, buyPrice);
        sleep();
        if (order_id != null) {
            Order order = getOrderDetail(order_id);
            Amount amount = order.getAmount();
            if (!amount.getCurrency().equals(testPair.getOrderCurrency())) {
                print("BAD - Amount not listed in " + testPair.getOrderCurrency().getCode() + "\nReturned " + amount.getCurrency().getCode());
            } else if (amount.getQuantity() != testNBTAmount) {
                print("BAD - Amount Quantity does not equal " + Double.toString(testNBTAmount));
            } else {
                print("Amount OK");
            }
            Amount price = order.getPrice();
            if (!price.getCurrency().equals(testPair.getPaymentCurrency())) {
                print("BAD - Price not listed in " + testPair.getPaymentCurrency().getCode());
            } else if (price.getQuantity() != buyPrice) {
                print("BAD - Price Quantity does not equal " + Double.toString(buyPrice));
            } else {
                print("Price OK");
            }
            cancelOrder(order_id, testPair);
        } else {
            print("BAD - Order not placed");
        }


    }

    private static void clearOrders(CurrencyPair pair) {
        ApiResponse response = Global.exchange.getTrade().clearOrders(pair);
        if (response.isPositive()) {
            //LOG.warn("\nresponse object: " + response.getResponseObject().toStringSep());
        } else {
            print("Error: " + response.getError().toString());
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            print(ie.toString());
        }
    }

    private static void print(String s) {
        System.out.println(s);
    }

    private static boolean cancelOrder(String order_id, CurrencyPair pair) {
        ApiResponse response = Global.exchange.getTrade().cancelOrder(order_id, pair);
        if (response.isPositive()) {
            //LOG.warn("\nresponse object: " + response.getResponseObject().toStringSep());
            return (boolean) response.getResponseObject();
        } else {
            print("Error: " + response.getError().toString());
            return false;
        }
    }

    private static Order getOrderDetail(String order_id) {
        ApiResponse response = Global.exchange.getTrade().getOrderDetail(order_id);
        if (response.isPositive()) {
            //LOG.warn("\nresponse object: " + response.getResponseObject().toStringSep());
            return (Order) response.getResponseObject();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static String getOpenOrders() {
        ApiResponse response = Global.exchange.getTrade().getActiveOrders();
        if (response.isPositive()) {
            print("Response object: " + response.getResponseObject().toString());
            return response.getResponseObject().toString();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static void getBalance(CurrencyPair pair) {
        //get the available balances for pair
        ApiResponse response = Global.exchange.getTrade().getAvailableBalances(pair);
        if (response.isPositive()) {
            //LOG.warn("\nresponse object: " + response.getResponseObject().toStringSep());
        } else {
            print("Error: " + response.getError().toString());
        }
    }

    private static String sell(CurrencyPair pair, double amount, double price) {
        //test that sell requests are processed correctly
        ApiResponse response = Global.exchange.getTrade().sell(pair, amount, price);
        if (response.isPositive()) {
            //LOG.warn("\nresponse object: " + response.getResponseObject().toStringSep());
            return response.getResponseObject().toString();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static String buy(CurrencyPair pair, double amount, double price) {
        //test that sell requests are processed correctly
        ApiResponse response = Global.exchange.getTrade().buy(pair, amount, price);
        if (response.isPositive()) {
            //LOG.warn("\nresponse object: " + response.getResponseObject().toStringSep());
            return response.getResponseObject().toString();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static void init() {
        String folderName = "testwrapperreturns_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;
        //Create log dir
        FileSystem.mkdir(logsFolder);


        System.setProperty("javax.net.ssl.trustStore", Global.settings.getProperty("keystore_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", Global.settings.getProperty("keystore_pass"));
    }

    private static ArrayList<String> populateExchanges() {
        ArrayList<String> testExchanges = new ArrayList<>();
        //testExchanges.add(Constant.BTCE);
        testExchanges.add(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO);
        testExchanges.add(ExchangeFacade.BTER);
        testExchanges.add(ExchangeFacade.CCEDK);
        testExchanges.add(ExchangeFacade.POLONIEX);
        testExchanges.add(ExchangeFacade.ALLCOIN);
        testExchanges.add(ExchangeFacade.BITSPARK_PEATIO);
        testExchanges.add(ExchangeFacade.EXCOIN);
        testExchanges.add(ExchangeFacade.BITCOINCOID);

        return testExchanges;
    }
}
