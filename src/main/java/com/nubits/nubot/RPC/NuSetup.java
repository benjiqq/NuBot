package com.nubits.nubot.RPC;

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
