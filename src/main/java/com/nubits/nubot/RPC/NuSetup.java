package com.nubits.nubot.RPC;

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

import com.nubits.nubot.bot.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NuSetup {

    final static Logger LOG = LoggerFactory.getLogger(NuSetup.class);

    /**
     * setup the task for checking Nu RPC
     */
    public static void setupNuRPCTask() {
        LOG.info("Setting up (verbose) RPC client on " + Global.options.getNudIp() + ":" + Global.options.getNudPort());

        Global.rpcClient = new NuRPCClient(Global.options.getNudIp(), Global.options.getNudPort(),
                Global.options.getRpcUser(), Global.options.getRpcPass(), Global.options.isVerbose(), true,
                Global.options.getNubitsAddress(), Global.options.getPair(), Global.options.getExchangeName());
    }

    public static void startTask(){
        LOG.info("Starting task : Check connection with Nud");
        Global.taskManager.getCheckNudTask().start();
    }
}
