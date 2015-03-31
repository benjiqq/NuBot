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

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.utils.InitTests;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestRPCLiquidityInfo {

    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestRPCLiquidityInfo.class.getName());
    /**
     * Configure tests
     */
    private static final String TEST_OPTIONS_PATH = "config/myconfig/comkort.json";
    private static String ipTest = "127.0.0.1";
    private static int portTest = 9091;
    private static boolean verbose = false;
    private static boolean useIdentifier = false;


    public static void main(String[] args) {

        InitTests.setLoggingFilename(LOG);
        InitTests.loadConfig(TEST_OPTIONS_PATH);  //Load settings

        //Default values
        String custodian = Settings.CUSTODIAN_PUBLIC_ADDRESS;
        String user = Settings.NUD_RPC_USER;
        String pass = Settings.NUD_RPC_PASS;
        double sell = 13.1;
        double buy = 11.0;
        //java -jar testRPC user pass custodian sell buy
        if (args.length == 5) {
            LOG.info("Reading input parameters");
            user = args[0];
            pass = args[1];
            custodian = args[2];
            sell = Double.parseDouble(args[3]);
            buy = Double.parseDouble(args[4]);
        }

        Global.rpcClient = new NuRPCClient("127.0.0.1", 9091,
                user, pass, true, custodian, CurrencyList.NBT_BTC, "");

        TestRPCLiquidityInfo test = new TestRPCLiquidityInfo();

        test.testCheckNudTask();

        test.setup(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO, custodian, CurrencyList.NBT_BTC, user, pass);

        //test.testGetInfo();
        //test.testIsConnected();

        test.testSendLiquidityInfo(buy, sell, 1);

        //test.testGetLiquidityInfo();
        //test.testGetLiquidityInfo(Constant.SELL, Passwords.CUSTODIA_PUBLIC_ADDRESS);
        //test.testGetLiquidityInfo(Constant.BUY, Passwords.CUSTODIA_PUBLIC_ADDRESS);


    }

    private void testSendLiquidityInfo(double amountBuy, double amountSell, int tier) {
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar, amountBuy, amountSell, tier);
            if (null == responseObject) {
                LOG.error("Something went wrong while sending liquidityinfo");
            } else {
                LOG.info(responseObject.toJSONString());
                if ((boolean) responseObject.get("submitted")) {
                    LOG.info("Now calling getliquidityinfo");
                    JSONObject infoObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
                    LOG.info(infoObject.toJSONString());
                }
            }
        } else {
            LOG.error("Nu Client offline. ");
        }

    }

    private void testGetInfo() {
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject = Global.rpcClient.getInfo();
            LOG.info(responseObject.toJSONString());
        } else {
            LOG.error("Nu Client offline. ");
        }
    }

    private void testIsConnected() {
        String connectedString = "offline";
        if (Global.rpcClient.isConnected()) {
            connectedString = "online";
        }
        LOG.info("Nud is " + connectedString + " @ " + Global.rpcClient.getIp() + ":" + Global.rpcClient.getPort());
    }

    private void setup(String exchangeName, String custodianAddress, CurrencyPair pair, String user, String pass) {
        String folderName = "tests_" + System.currentTimeMillis() + "/";

        Utils.installKeystore(true);

        String custodian = Settings.CUSTODIAN_PUBLIC_ADDRESS;

        //Create the client
        Global.rpcClient = new NuRPCClient(ipTest, portTest, user, pass, useIdentifier, custodian, pair, exchangeName);
    }

    private void testCheckNudTask() {
        //Create a TaskManager and
        Global.taskManager = new TaskManager(false);
        Global.taskManager.setNudTask();
        //Start checking for connection
        Global.taskManager.getCheckNudTask().start();

        //Wait a couple of seconds for the connectionThread to get live
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.error("" + ex);
        }
    }

    private void testGetLiquidityInfo() {
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
            if (null == responseObject) {
                LOG.error("Something went wrong while sending liquidityinfo");
            } else {
                LOG.info(responseObject.toJSONString());
            }
        } else {
            LOG.error("Nu Client offline. ");
        }
    }

    private void testGetLiquidityInfo(String type, String address) {
        if (Global.rpcClient.isConnected()) {
            double response = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar, type, address);
            if (response == -1) {
                LOG.error("Something went wrong while sending liquidityinfo");
            } else {
                LOG.info("Total " + type + " liquidity : " + response + " " + CurrencyList.NBT.getCode());
            }
        } else {
            LOG.error("Nu Client offline. ");
        }

    }
}
