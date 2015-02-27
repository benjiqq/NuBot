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

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class TestWrappers {

    private static final Logger LOG = Logger.getLogger(TestWrappers.class.getName());
    /**
     * Configure tests
     */
    public static final String SETTINGS = FileUtils.getFile("settings.properties").getPath();
    private static final String TEST_OPTIONS_PATH = FileUtils.getFile("testconfig","alts.json").getAbsolutePath();
    public static final String testExchange = Constant.ALTSTRADE;
    public static final CurrencyPair testPair = Constant.NBT_BTC;
    public static final Currency testCurrency = Constant.NBT;

    public static void main(String[] args) {
        //Load settings
        Utils.loadProperties("settings.properties");
        init();
        String[] inputs = new String[1];
        inputs[0] = TEST_OPTIONS_PATH;
        try {
            Global.options = ParseOptions.parseOptions(inputs);
        } catch (NuBotConfigException ex) {
            LOG.severe(ex.toString());
        }

        WrapperTestUtils.configExchange(testExchange); //Replace to test a different API implementation

        runTests();
        System.exit(0);
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
        String folderName = "testwrappers_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;
        //Create log dir
        FileSystem.mkdir(logsFolder);
        try {
            NuLogger.setup(false, logsFolder);
        } catch (IOException ex) {
            LOG.severe(ex.toString());
        }
        try {
            Utils.installKeystore(false);
        } catch (Exception ex) {
            LOG.severe(ex.toString());
        }
    }
}
