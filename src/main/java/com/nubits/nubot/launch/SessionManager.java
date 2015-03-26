package com.nubits.nubot.launch;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class.getName());

    /**
     * main launch of a bot
     *
     * @param configfile
     * @param runui
     */
    public static void mainLaunch(String configfile, boolean runui) {


        LOG.debug("main launch. with configfile " + configfile + " " + " runui " + runui);

        NuBotOptions nuopt = null;

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            nuopt = ParseOptions.parseOptionsSingle(configfile);
        } catch (NuBotConfigException e) {
            MainLaunch.exitWithNotice("" + e);
        }

        LOG.debug("-- new main launched --");

        LOG.debug("** run command line **");
        executeBot(nuopt);

    }

    /**
     * execute a NuBot based on valid options. Also make sure only one NuBot is running
     *
     * @param opt
     */
    public static void executeBot(NuBotOptions opt) {

        Global.mainThread = Thread.currentThread();

        createShutDownHook();

        //exit if already running or show info to user
        if (Global.running) {
            MainLaunch.exitWithNotice("NuBot is already running. Make sure to terminate other instances.");
        } else {
            if (opt.requiresSecondaryPegStrategy()) {
                LOG.debug("creating secondary bot object");
                NuBotSecondary bot = new NuBotSecondary();
                bot.execute(opt);
            } else {
                LOG.debug("creating simple bot object");
                NuBotSimple bot = new NuBotSimple();
                bot.execute(opt);
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
                if (Global.exchange.getTrade() != null && Global.options.getPair() != null) {
                    LOG.info("Clearing out active orders ... ");

                    ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
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
                if (Global.options.isSubmitliquidity()) {
                    if (Global.rpcClient.isConnected()) {
                        //tier 1
                        LOG.info("Resetting Liquidity Info before quit");

                        JSONObject responseObject1 = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
                                0, 0, 1);
                        if (null == responseObject1) {
                            LOG.error("Something went wrong while sending liquidityinfo");
                        } else {
                            LOG.info(responseObject1.toJSONString());
                        }

                        JSONObject responseObject2 = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
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

                LOG.info("change session logs");
                //TODO: any post-processing
                //moveSessionLogs();


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
