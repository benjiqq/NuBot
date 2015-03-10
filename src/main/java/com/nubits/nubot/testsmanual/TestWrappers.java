/*
 * Copyright (C) 2014-2015 Nu Development Team
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
package com.nubits.nubot.testsmanual;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class TestWrappers {

    static {
        System.setProperty("logback.configurationFile", "allconfig  /testlog.xml");
    }


    private static final Logger LOG = LoggerFactory.getLogger(TestWrappers.class.getName());
    /**
     * Configure tests
     */
    private static final String TEST_OPTIONS_PATH = "testconfig/poloniex.json";
    //private static final String TEST_OPTIONS_PATH = "options.json";
    public static final String testExchange = ExchangeFacade.POLONIEX;
    public static final CurrencyPair testPair = CurrencyList.NBT_BTC;
    public static final Currency testCurrency = CurrencyList.NBT;

    public static void main(String[] args) {

        //Load settings
        try{
            Utils.loadProperties("settings.properties");
        }catch(IOException e){
            System.out.println("can't load settings");
            System.exit(0);
        }
        init();

        try {
            Global.options = ParseOptions.parseOptionsSingle(TEST_OPTIONS_PATH);
            LOG.info("using key: " + Global.options.getApiKey());
            LOG.info("config exchange " + testExchange);
            WrapperTestUtils.configExchange(testExchange); //Replace to test a different API implementation
        } catch (NuBotConfigException ex) {
            LOG.error(ex.toString());
        }


        runTests();
    }

    public static void runTests() {
        long startTime = System.nanoTime(); //TIC

        //Methods strictly necessary for NuBot to run-------------
        //-------------

        //WrapperTestUtils.testGetAvailableBalance(testCurrency);
        WrapperTestUtils.testGetAvailableBalances(testPair);
        //WrapperTestUtils.testGetActiveOrders(testPair);
        //WrapperTestUtils.testGetActiveOrders(); //Try with 0 active orders also . for buy orders, check in which currency is the amount returned.
        //WrapperTestUtils.testClearAllOrders(Constant.NBT_BTC);
        //WrapperTestUtils.testGetAvailableBalances(testPair);
        //WrapperTestUtils.testSell(0.3, 0.00830509, Constant.NBT_BTC);  //ok
        //WrapperTestUtils.testBuy(0.003, 0.0000120, Constant.NBT_BTC);  //ok
        //WrapperTestUtils.testGetActiveOrders();
        //WrapperTestUtils.testCancelOrder("1139", Constant.NBT_BTC);
        //WrapperTestUtils.testClearAllOrders(Constant.NBT_BTC);
        //WrapperTestUtils.testSell(1, 0.1830509, testPair);  //ok
        //WrapperTestUtils.testBuy(0.0000120, 0.0000120, testPair);  //ok
        //WrapperTestUtils.testGetActiveOrders();
        //WrapperTestUtils.testCancelOrder("2063803", testPair);
        //WrapperTestUtils.testClearAllOrders(testPair);
        //WrapperTestUtils.testGetOrderDetail("1139");
        //WrapperTestUtils.testIsOrderActive("1139");
        //WrapperTestUtils.testGetTxFee();
        //WrapperTestUtils.testGetTxFeeWithArgs(testPair);

        //WrapperTestUtils.testClearAllOrders(testPair);


        //Create multiple orders for testing
        /*
         ArrayList<OrderToPlace> orders = new ArrayList<>();
         for (int i = 0; i < 10; i++) {
         orders.add(new OrderToPlace(Constant.BUY, testPair, 0.5, 0.001));
         }

         for (int i = 0; i < 10; i++) {
         orders.add(new OrderToPlace(Constant.SELL, testPair, 0.5, 0.009));
         }

         WrapperTestUtils.testMultipleOrders(orders, testPair);
         */

        //Methods NOT strictly necessary for NuBot to run---------------
        //---------------
        //WrapperTestUtils.testGetLastPrice(testPair);
        //WrapperTestUtils.testGetLastTrades(testPair, 1388534400);
        //WrapperTestUtils.testGetLastTrades(testPair);


        LOG.info("Total Time: " + (System.nanoTime() - startTime) / 1000000 + " ms"); //TOC

    }

    public static void init() {
        //init logging when testing



        try {
            LOG.info("install keystore");
            Utils.installKeystore(false);
        } catch (Exception ex) {
            LOG.error(ex.toString());
        }
    }
}
