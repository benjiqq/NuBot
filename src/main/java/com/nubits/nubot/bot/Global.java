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

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global object for NuBot
 */
public class Global {

    final static Logger LOG = LoggerFactory.getLogger(Global.class);

    public static Thread mainThread;

    public static NuBotOptions options;

    //path of logs in this session
    public static String sessionLogFolder;

    public static TaskManager taskManager;

    public static NuRPCClient rpcClient;

    public static double conversion = 1; //Change this? update SendLiquidityinfoTask
    public static FrozenBalancesManager frozenBalances;
    public static boolean swappedPair; //true if payment currency is NBT

    public static Exchange exchange;

    public static String sessionId;
    public static String sessionPath;
    public static long sessionStarted, sessionStopped;

    public static NuBotBase bot;


    /**
     * shutdown mechanics
     */
    public static void createShutDownHook() {

        LOG.info("adding shutdown hook");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                Global.bot.shutdownBot();

                Logger sessionLOG = LoggerFactory.getLogger("SessionLOG");
                Global.sessionStopped = System.currentTimeMillis();
                sessionLOG.info("session end;" + Global.sessionStopped);

                LOG.info("change session logs");
                //TODO: any post-processing


                LOG.info("Exit. ");

                Global.mainThread.interrupt();
                if (Global.taskManager != null) {
                    if (Global.taskManager.isInitialized()) {
                        try {
                            Global.taskManager.stopAll();
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
