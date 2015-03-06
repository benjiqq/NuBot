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
package com.nubits.nubot.bot;

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.utils.FrozenBalancesManager;
import java.util.Properties;

/**
 * Global object for NuBot
 *
 */
public class Global {

    public static NuBotOptions options;

    /**
     * storage layer
     */
    public static Store store;


    public static Properties settings;
    public static String sessionId;
    public static boolean running = false;

    public static TaskManager taskManager;

    public static NuRPCClient rpcClient;

    public static double conversion = 1; //Change this? update SendLiquidityinfoTask
    public static FrozenBalancesManager frozenBalances;
    public static boolean swappedPair; //true if payment currency is NBT

    public static Exchange exchange;

    public static String publicAddress;
}
