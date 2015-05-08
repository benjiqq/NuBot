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

package com.nubits.nubot.testsmanual;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.utils.InitTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestWrappers {

    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }


    private static final Logger LOG = LoggerFactory.getLogger(TestWrappers.class.getName());

    /**
     * Configure tests
     */

    private static final String TEST_OPTIONS_PATH = "config/myconfig/bitcoincoid.json";

    public static final CurrencyPair testPair = CurrencyList.NBT_BTC;
    public static final Currency testCurrency = CurrencyList.NBT;

    public static void main(String[] args) {
        init();
        runTests();
    }

    public static void runTests() {
        long startTime = System.nanoTime(); //TIC

        //Methods strictly necessary for NuBot to run-------------
        //-------------


        //WrapperTestUtils.testSell(0.3, 0.00830509, testPair);  //ok

        WrapperTestUtils.testGetAvailableBalances(testPair);
        //WrapperTestUtils.testClearAllOrders(testPair);

        //WrapperTestUtils.testSell(0.3, 0.00830509, testPair);  //ok
        //WrapperTestUtils.testSell(0.3, 0.00830509, testPair);  //ok
        //WrapperTestUtils.testSell(0.3, 0.00830509, testPair);  //ok
        //WrapperTestUtils.testBuy(0.3, 0.00300, testPair);  //ok
        //WrapperTestUtils.testBuy(0.3, 0.00300, testPair);  //ok
        //WrapperTestUtils.testBuy(0.3, 0.00300, testPair);  //ok
        //WrapperTestUtils.testGetAvailableBalances(testPair);

        //WrapperTestUtils.testGetActiveOrders(); //Try with 0 active orders also . for buy orders, check in which currency is the amount returned.
        //WrapperTestUtils.testClearAllOrders(CurrencyList.NBT_BTC);
        //WrapperTestUtils.testGetActiveOrders(testPair);
        //WrapperTestUtils.testGetActiveOrders(); //Try with 0 active orders also . for buy orders, check in which currency is the amount returned.

        //WrapperTestUtils.testGetAvailableBalances(testPair);

        //WrapperTestUtils.testSell(0.3, 0.00830509, testPair);  //ok
        //WrapperTestUtils.testBuy(0.3, 0.00100, testPair);  //ok

        //WrapperTestUtils.testGetActiveOrders();
        //WrapperTestUtils.testCancelOrder("459324", testPair);
        //WrapperTestUtils.testClearAllOrders(testPair);
        //WrapperTestUtils.testSell(1, 0.1830509, testPair);  //ok
        //WrapperTestUtils.testBuy(0.0000120, 0.0000120, testPair);  //ok
        //WrapperTestUtils.testGetActiveOrders();
        //WrapperTestUtils.testCancelOrder("2063803", testPair);
        //WrapperTestUtils.testClearAllOrders(testPair);
        //WrapperTestUtils.testGetActiveOrders(testPair, true); //Read active orders (should be 0)
        //WrapperTestUtils.testGetOrderDetail("1139");
        //WrapperTestUtils.testIsOrderActive("7d27fb8b-05bd-4937-b404-a808766f2dfc");
        //WrapperTestUtils.testGetTxFee();
        //WrapperTestUtils.testGetTxFeeWithArgs(testPair);

        //WrapperTestUtils.testClearAllOrders(testPair);
        //WrapperTestUtils.testGetLastTrades(testPair);

/*
        try {
            long waitTime = 500;//ms
            WrapperTestUtils.testGetActiveOrders(testPair, false); //How many any active orders prior to starting the test?

            WrapperTestUtils.testClearAllOrders(testPair); //Clear all orders
            Thread.sleep(waitTime); //Wait
            LOG.info("Forcing waiting ms:" + waitTime);
            WrapperTestUtils.testGetActiveOrders(testPair, false); //Read active orders (should be 0)

            //Place some orders
            int count = 0;
            for (int i = 0; i < 30; i++) {
                WrapperTestUtils.testBuy(0.4, 0.00300, testPair);  //ok
                WrapperTestUtils.testSell(0.4, 0.09000, testPair);  //ok
                count += 2;
            }

            LOG.info("\n\n" + count + " orders placed \n\n");

            for (int i = 0; i < 5; i++) {
                Thread.sleep(waitTime); //wait
                LOG.info("Forcing waiting ms:" + waitTime);
                WrapperTestUtils.testGetActiveOrders(testPair, false); //try to get active orders
            }

            WrapperTestUtils.testClearAllOrders(testPair); //Clear all orders
            Thread.sleep(waitTime); //wait
            LOG.info("Forcing waiting ms:" + waitTime);
            WrapperTestUtils.testGetActiveOrders(testPair, false); //Read active orders (should be 0)

        } catch (InterruptedException e) {
            LOG.error(e.toString());
        }

*/
        //-------- Stress test start ---------
/*
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    WrapperTestUtils.testGetActiveOrders();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 100; i++) {
                    WrapperTestUtils.testClearAllOrders(testPair);
                }
            }
        });


        t.start();
        t2.start();

        for (int i = 0; i < 100; i++) {
            WrapperTestUtils.testGetAvailableBalance(testCurrency);
        }

        //-------- Stress test end ---------
*/


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

        System.exit(0);
    }

    private static void init() {
        InitTests.setLoggingFilename(TestWrappers.class.getSimpleName());
        InitTests.loadConfig(TEST_OPTIONS_PATH);  //Load settings

        //Load keystore
        boolean trustAll = false;
        if (Global.options.getExchangeName().equalsIgnoreCase(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
            trustAll = true;
        }
        InitTests.loadKeystore(trustAll);

        try {
            LOG.info("Public API key: " + Global.options.getApiKey());
            LOG.info("Exchange: " + Global.options.getExchangeName());
            WrapperTestUtils.configureExchange(Global.options.getExchangeName());
            InitTests.startConnectionCheck();

        } catch (NuBotConfigException ex) {
            LOG.error(ex.toString());
        }


        Global.sessionLogFolder = Settings.TEST_LOGFOLDER;
    }


}
