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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;


public class TestWrappers {

    //define Logging by using predefined Settings which points to an XML
    static {
        String wdir = System.getProperty("user.dir");
        File f = new File(wdir + Settings.TEST_LOGXML);
        if (f.exists())
            System.setProperty("logback.configurationFile", f.getAbsolutePath());

    }


    private static final Logger LOG = LoggerFactory.getLogger(TestWrappers.class.getName());

    /**
     * Configure tests
     */
    private static final String TEST_OPTIONS_PATH = "config/myconfig/peatio.json";

    public static final CurrencyPair testPair = CurrencyList.NBT_BTC;
    public static final Currency testCurrency = CurrencyList.NBT;

    public static void main(String[] args) {

        InitTests.loadConfig(TEST_OPTIONS_PATH);  //Load settings

        //Load keystore
        boolean trustAll = false;
        if (Global.options.getExchangeName().equalsIgnoreCase(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO))
        {
            trustAll = true;
        }
        InitTests.loadKeystore(trustAll);

        try {
            LOG.info("using key: " + Global.options.getApiKey());
            LOG.info("config exchange " + Global.options.getExchangeName());
            WrapperTestUtils.configureExchange(Global.options.getExchangeName());
            InitTests.startConnectionCheck();

        } catch (NuBotConfigException ex) {
            LOG.error(ex.toString());
        }

        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger)LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        LOG.debug("Logback used '{}' as the configuration file.", mainURL);

        Global.sessionLogFolders = Settings.TEST_LOGFOLDER;

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
        //WrapperTestUtils.testClearAllOrders(CurrencyList.NBT_BTC);
        //WrapperTestUtils.testGetAvailableBalances(testPair);
        //WrapperTestUtils.testSell(0.3, 0.00830509, testPair);  //ok
        //WrapperTestUtils.testBuy(0.003, 0.0000120, testPair);  //ok
        //WrapperTestUtils.testGetActiveOrders();
        //WrapperTestUtils.testCancelOrder("123199680", testPair);
        //WrapperTestUtils.testClearAllOrders(testPair);
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

        System.exit(0);
    }


}
