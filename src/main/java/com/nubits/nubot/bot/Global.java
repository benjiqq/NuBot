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

package com.nubits.nubot.bot;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Properties;

/**
 * Global object for NuBot
 */
public class Global {

    final static Logger LOG = LoggerFactory.getLogger(Global.class);

    public static Thread mainThread;

    public static NuBotOptions options;

    //path of logs in this session
    public static String sessionLogFolders;

    public static boolean running = false;

    public static TaskManager taskManager;

    public static NuRPCClient rpcClient;

    public static double conversion = 1; //Change this? update SendLiquidityinfoTask
    public static FrozenBalancesManager frozenBalances;
    public static boolean swappedPair; //true if payment currency is NBT

    public static Exchange exchange;

    public static String sessionId;
    public static long sessionStarted, sessionStopped;
    public static SessionData sessiondata;


    public static void moveSessionLogs() {

        LOG.debug("moving session logs");

        String wdir = System.getProperty("user.dir");

        LOG.debug("wdir is: " + wdir);

        File f = new File(wdir + "/" + Settings.LOGS_PATH + Settings.CURRENT_LOGS_FOLDER); // current directory

        File[] files = f.listFiles();

        File past = new File(wdir + "/" + Settings.LOGS_PATH + Settings.PAST_LOGS_FOLDER);
        if (!past.exists()) {
            try {
                past.mkdir();
            } catch (Exception e) {

            }
        }

        for (File file : files) {
            if (file.isDirectory()) {
                String currentLogfoldername = file.getName();
                LOG.trace(currentLogfoldername);

                File sessionLogDir = new File(Global.sessionLogFolders = Settings.LOGS_PATH  + Settings.CURRENT_LOGS_FOLDER + currentLogfoldername);
                File historyDir = new File(Settings.LOGS_PATH + Settings.PAST_LOGS_FOLDER + currentLogfoldername);
                LOG.debug("move from: " + sessionLogDir + " >> to: " + historyDir);
                try {
                    FileUtils.moveDirectory(sessionLogDir, historyDir);
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * shutdown mechanics
     */
    public static void createShutDownHook() {

        LOG.info("adding shutdown hook");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("Bot shutting down..");

                String additionalInfo = "after "+ Utils.getBotUptime()+ " uptime on "
                        + Global.options.getExchangeName() + " ["
                        + Global.options.getPair().toStringSep()+"]";

                HipChatNotifications.sendMessageCritical("Bot shut-down " + additionalInfo);

                //Try to cancel all orders, if any
                if (exchange.getTrade() != null && options.getPair() != null) {
                    LOG.info("Clearing out active orders ... ");

                    ApiResponse deleteOrdersResponse = exchange.getTrade().clearOrders(options.getPair());
                    if (deleteOrdersResponse.isPositive()) {
                        boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

                        if (deleted) {
                            LOG.info("Order clear request successful");
                        } else {
                            LOG.error("Could not submit request to clear orders");
                        }

                    } else {
                        LOG.error(deleteOrdersResponse.getError().toString());
                    }
                }

                //reset liquidity info
                if (options.isSubmitliquidity()) {
                    if (rpcClient.isConnected()) {
                        //tier 1
                        LOG.info("Resetting Liquidity Info before quit");

                        JSONObject responseObject1 = rpcClient.submitLiquidityInfo(rpcClient.USDchar,
                                0, 0, 1);
                        if (null == responseObject1) {
                            LOG.error("Something went wrong while sending liquidityinfo");
                        } else {
                            LOG.info(responseObject1.toJSONString());
                        }

                        JSONObject responseObject2 = rpcClient.submitLiquidityInfo(rpcClient.USDchar,
                                0, 0, 2);
                        if (null == responseObject2) {
                            LOG.error("Something went wrong while sending liquidityinfo");
                        } else {
                            LOG.info(responseObject2.toJSONString());
                        }
                    }
                }

                Logger sessionLOG = LoggerFactory.getLogger("SessionLOG");
                Global.sessionStopped = System.currentTimeMillis();
                sessionLOG.info("session end;" + Global.sessionStopped);

                LOG.info("move session log dir");
                moveSessionLogs();


                LOG.info("Exit. ");
                mainThread.interrupt();
                if (taskManager != null) {
                    if (taskManager.isInitialized()) {
                        try {
                            taskManager.stopAll();
                        } catch (IllegalStateException e) {

                        }
                    }
                }

                //TODO! this shuts down UI as well
                Thread.currentThread().interrupt();
                return;
            }
        }));
    }
}
