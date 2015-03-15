package com.nubits.nubot.testsmanual;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * a utility for launch a bot based on predefined config
 */
public class TestLaunch {

    private static Thread mainThread;
    private static final Logger LOG = LoggerFactory.getLogger(TestLaunch.class.getName());

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        NuBotOptions opt = null;

        //Load settings
        try {
            Utils.loadProperties("settings.properties");
        } catch (IOException e) {
            LOG.error("could not load settings");
            System.exit(0);
        }
        LOG.info("settings loaded");


        try {
            opt = ParseOptions.parseOptionsSingle("testconfig/poloniex.json");
        } catch (NuBotConfigException e) {
            exitWithNotice("" + e);
        }

        executeBot(opt);
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
     * shutdown mechanics
     * TODO: some of the logic can be handled by NuBot in an async way
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
                            LOG.info("Order clear request succesfully");
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
