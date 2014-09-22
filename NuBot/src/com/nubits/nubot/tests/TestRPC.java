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

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.global.Passwords;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class TestRPC {

    private static final Logger LOG = Logger.getLogger(TestRPC.class.getName());
    private static String ipTest = "127.0.0.1";
    private static int portTest = 9091;
    private static boolean verbose = true;

    public static void main(String[] args) {
        TestRPC test = new TestRPC();

        test.setup();
        test.testCheckNudTask();
        test.testGetInfo();
        //test.testIsConnected();
        //test.testSendLiquidityInfo();

        //testGetLiquidityInfo();
        //System.exit(0);

    }

    private void testSendLiquidityInfo() {
        double amountSell = 15;
        double amountBuy = 994;

        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar, amountBuy, amountSell, Passwords.CUSTODIA_PUBLIC_ADDRESS);
            if (null == responseObject) {
                LOG.severe("Something went wrong while sending liquidityinfo");
            } else {
                LOG.fine(responseObject.toJSONString());
                if ((boolean) responseObject.get("submitted")) {
                    LOG.fine("Now calling getliquidityinfo");
                    JSONObject infoObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
                    LOG.fine(infoObject.toJSONString());
                }
            }
        } else {
            LOG.severe("Nu Client offline. ");
        }

    }

    private void testGetInfo() {
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject = Global.rpcClient.getInfo();
            LOG.fine(responseObject.toJSONString());
        } else {
            LOG.severe("Nu Client offline. ");
        }
    }

    private void testIsConnected() {
        String connectedString = "offline";
        if (Global.rpcClient.isConnected()) {
            connectedString = "online";
        }
        LOG.fine("Nud is " + connectedString + " @ " + Global.rpcClient.getIp() + ":" + Global.rpcClient.getPort());
    }

    private void setup() {
        try {
            NuLogger.setup(true);
        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        LOG.setLevel(Level.FINE);

        System.setProperty("javax.net.ssl.trustStore", Global.settings.getProperty("keystore_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", Global.settings.getProperty("keystore_pass"));

        Global.publicAddress = Passwords.CUSTODIA_PUBLIC_ADDRESS;

        //Create the client
        Global.rpcClient = new NuRPCClient(ipTest, portTest, Passwords.NUD_RPC_USER, Passwords.NUD_RPC_PASS, verbose);
    }

    private void testCheckNudTask() {
        //Create a TaskManager and
        Global.taskManager = new TaskManager();
        //Start checking for connection
        Global.taskManager.getCheckNudTask().start();


        //Wait a couple of seconds for the connectionThread to get live

    }

    private void testGetLiquidityInfo() {
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
            if (null == responseObject) {
                LOG.severe("Something went wrong while sending liquidityinfo");
            } else {
                LOG.fine(responseObject.toJSONString());
            }
        } else {
            LOG.severe("Nu Client offline. ");
        }
    }
}
