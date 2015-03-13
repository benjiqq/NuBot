package com.nubits.nubot.launch;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.NuBotSecondary;
import com.nubits.nubot.bot.NuBotSimple;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import org.json.simple.JSONObject;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * the main launcher class. starts bot based on configuration, not through UI
 */
public class MainLaunch {

    private static Thread mainThread;

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static boolean runui = false;

    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json> runui=true";

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        if (args.length > 2) {
            exitWithNotice("wrong argument number : run nubot with \n" + USAGE_STRING);
        }

        String configfile = args[0];

        if (args.length == 2) {
            String[] s = args[1].split("=");
            try {
                runui = new Boolean(s[1]).booleanValue();
            } catch (Exception e) {
                exitWithNotice("can't parse runui flag: run nubot with \n" + USAGE_STRING);
            }
        }

        NuBotOptions nuopt = null;

        //Load settings
        try {
            Utils.loadProperties("settings.properties");
        } catch (IOException e) {
            exitWithNotice("could not load settings");
        }

        LOG.info("settings loaded");

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            nuopt = ParseOptions.parseOptionsSingle(configfile);
        } catch (NuBotConfigException e) {
            exitWithNotice("" + e);
        }

        LOG.info("runui " + runui);

        if (runui)
            UILaunch.UIlauncher(".", configfile);
        else
            executeBot(nuopt);

    }


    /**
     * exit application and notify user
     *
     * @param msg
     */
    private static void exitWithNotice(String msg) {
        LOG.error(msg);
        System.exit(0);
    }


    /**
     * execute a NuBot based on valid options. Also make sure only one NuBot is running
     *
     * @param opt
     */
    public static void executeBot(NuBotOptions opt) {

        mainThread = Thread.currentThread();

        createShutDownHook();

        //exit if already running or show info to user
        if (Global.running) {
            exitWithNotice("NuBot is already running. Make sure to terminate other instances.");
        } else {
            if (opt.secondarypeg) {
                LOG.info("creating secondary bot");
                NuBotSecondary bot = new NuBotSecondary();
                bot.execute(opt);
            } else {
                LOG.info("creating simple bot");
                NuBotSimple bot = new NuBotSimple();
                bot.execute(opt);
            }
        }

    }


    /**
     * parse the command line arguments. first argument has to be the config file
     *
     * @param args
     * @return
     * @throws NuBotConfigException
     */
    private static String parseOptionsArgs(String args[]) throws NuBotConfigException {

        String configfile = "";

        //Load Options and test for critical configuration errors
        configfile = args[0];

        try {
            String p = System.getProperty("runui");
            LOG.info("runui " + p);
            runui = new Boolean(p).booleanValue();
        } catch (Exception e) {
            LOG.info("can't parse runui");
        }


        return configfile;
    }


    /**
     * shutdown mechanics
     */
    private static void createShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("Bot shutting down..");

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
                if (Global.rpcClient.isConnected() && Global.options.isSubmitliquidity()) {
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

                LOG.info("Exit. ");
                mainThread.interrupt();
                if (Global.taskManager != null) {
                    if (Global.taskManager.isInitialized()) {
                        try {
                            Global.taskManager.stopAll();
                        } catch (IllegalStateException e) {

                        }
                    }
                }


                Thread.currentThread().interrupt();
                return;
            }
        }));
    }
}

