package com.nubits.nubot.launch;

import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import org.json.simple.JSONObject;

import org.slf4j.LoggerFactory; import org.slf4j.Logger;

/**
 * the main launcher class. starts bot based on configuration, not through UI
 */
public class MainLaunch {

    private static Thread mainThread;
    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());
    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json> [path/to/options-part2.json] ... [path/to/options-partN.json]";

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     * @author desrever <desrever at nubits.com>
     */
    public static void main(String args[]) {

        NuBotOptions opt = null;

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            opt = parseOptionsArgs(args);
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
     * @param opt
     */
    public static void executeBot(NuBotOptions opt) {

        mainThread = Thread.currentThread();

        createShutDownHook();

        //exit if already running or show info to user
        if (Global.running) {
            exitWithNotice("NuBot is already running. Make sure to terminate other instances.");
        } else {
            NuBot app = new NuBot();
            app.execute(opt);
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

                if (Global.options != null) {
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
                            Global.taskManager.stopAll();
                        }
                    }
                }

                Thread.currentThread().interrupt();
                return;
            }
        }));
    }


    /**
     * check if arguments to NuBot are valid supported are arguments larger then
     * 0
     *
     * @param args
     * @return
     */
    private static boolean isValidArgs(String[] args) {
        boolean valid = args.length > 0;

        return valid;
    }

    /**
     * parse the command line arguments
     *
     * @param args
     * @return
     * @throws NuBotConfigException
     */
    private static NuBotOptions parseOptionsArgs(String args[]) throws NuBotConfigException {

        if (!isValidArgs(args)) {
            throw new NuBotConfigException("wrong argument number : run nubot with \n" + USAGE_STRING);
        }

        NuBotOptions opt = null;
        //Load Options and test for critical configuration errors
        if (args.length > 1) {
            //more than one file path given
            try {
                opt = ParseOptions.parseOptions(args);
            } catch (NuBotConfigException ex) {
                throw new NuBotConfigException("NuBot wrongly configured");
            }

        } else {
            try {
                opt = ParseOptions.parseOptionsSingle(args[0]);
            } catch (NuBotConfigException ex) {
                throw new NuBotConfigException("NuBot wrongly configured");
            }
        }
        if (opt == null)
            throw new NuBotConfigException("");

        return opt;
    }

}
