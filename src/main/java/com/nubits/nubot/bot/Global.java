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
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.strategy.BalanceManager;
import com.nubits.nubot.strategy.OrderManager;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.utils.FrozenBalancesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global object for NuBot
 */
public class Global {

    final static Logger LOG = LoggerFactory.getLogger(Global.class);

    public static Thread mainThread;

    public static NuBotOptions options;

    /**
     * the bot connected to the global thread
     */
    public static NuBotBase bot;

    public static OrderManager orderManager;

    public static BalanceManager balanceManager;

    public static Exchange exchange;

    public static TaskManager taskManager;

    //path of logs in this session
    public static String sessionLogFolder;

    public static NuRPCClient rpcClient;

    public static double conversion = 1; //Change this? update SendLiquidityinfoTask

    public static FrozenBalancesManager frozenBalancesManager;

    public static boolean swappedPair; //true if payment currency is NBT

    public static String sessionPath;

    public static String currentOptionsFile;

    /**
     * shutdown mechanics
     */
    public static void createShutDownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                //shutdown logic of the bot handled in the bot related to the global thread
                if (SessionManager.isSessionRunning()) {
                    Global.bot.shutdownBot();
                    SessionManager.setModeHalting();
                    SessionManager.sessionStopped = System.currentTimeMillis();
                }

                //Interrupt mainThread
                if (Global.mainThread != null)
                    Global.mainThread.interrupt();

                LOG.info("Exit main");

                //this shuts down UI as well
                Thread.currentThread().interrupt();
                return;
            }
        }));
    }


}
