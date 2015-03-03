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
package com.nubits.nubot.launch;

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.global.NuBotConnectionException;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.*;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.tasks.strategy.PriceMonitorTriggerTask;
import com.nubits.nubot.tasks.strategy.StrategyPrimaryPegTask;
import com.nubits.nubot.tasks.strategy.StrategySecondaryPegTask;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.CcexWrapper;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.Utils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * NuBot. launched from command line or UI
 *
 * @author desrever <desrever at nubits.com>
 */
public class NuBot {

    private boolean liveTrading;

    //private String logsFolder;
    final static Logger LOG = LoggerFactory.getLogger(NuBot.class);

    public NuBot() {

    }

    /**
     * setup logging
     */
    private void setupLog() {
        //Setting up log folder for this session :

        /*String folderName = "NuBot_" + Utils.getTimestampLong() + "_" + Global.options.getExchangeName() + "_" + Global.options.getPair().toString().toUpperCase() + "/";
        logsFolder = Global.settings.getProperty("log_path") + folderName;

        //Create log dir
        FileSystem.mkdir(logsFolder);
        try {
            NuLogger.setup(Global.options.isVerbose(), logsFolder);
        } catch (IOException ex) {
            LOG.error(ex.toString());
        }
        LOG.info("Setting up  NuBot version : " + Global.settings.getProperty("version"));

        LOG.info("Init logging system");*/

        //Disable hipchat debug logging https://github.com/evanwong/hipchat-java/issues/16
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    private void setupSSL() {
        LOG.info("Set up SSL certificates");
        boolean trustAllCertificates = false;
        if (Global.options.getExchangeName().equalsIgnoreCase(Constant.INTERNAL_EXCHANGE_PEATIO)) {
            trustAllCertificates = true;
        }
        Utils.installKeystore(trustAllCertificates);
        Utils.printSeparator();
    }

    /**
     * all setups
     */
    private void setupConfigBot() {

        Utils.printSeparator();

        //Generate Bot Session unique id
        Global.sessionId = Utils.generateSessionID();
        LOG.info("Session ID = " + Global.sessionId);

        setupLog();

        setupSSL();

        setupExchange();
    }

    private void setupExchange() {
        LOG.info("Wrap the keys into a new ApiKeys object");
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());
        Utils.printSeparator();

        LOG.info("Creating an Exchange object");

        Global.exchange = new Exchange(Global.options.getExchangeName());
        Utils.printSeparator();

        LOG.info("Create e ExchangeLiveData object to accommodate liveData from the exchange");
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);
        Utils.printSeparator();


        LOG.info("Create a new TradeInterface object");
        TradeInterface ti = Exchange.getTradeInterface(Global.options.getExchangeName());
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);
        if (Global.options.getExchangeName().equals(Constant.CCEX)) {
            ((CcexWrapper) (ti)).initBaseUrl();
        }


        if (Global.options.getPair().getPaymentCurrency().equals(Constant.NBT)) {
            Global.swappedPair = true;
        } else {
            Global.swappedPair = false;
        }

        LOG.info("Swapped pair mode : " + Global.swappedPair);


        String apibase = "";
        if (Global.options.getExchangeName().equalsIgnoreCase(Constant.INTERNAL_EXCHANGE_PEATIO)) {
            ti.setApiBaseUrl(Constant.INTERNAL_EXCHANGE_PEATIO_API_BASE);
        }


        Global.exchange.setTrade(ti);
        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());
        Utils.printSeparator();


        //For a 0 tx fee market, force a price-offset of 0.1%
        ApiResponse txFeeResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
        if (txFeeResponse.isPositive()) {
            double txfee = (Double) txFeeResponse.getResponseObject();
            if (txfee == 0) {
                LOG.warn("The bot detected a 0 TX fee : forcing a priceOffset of 0.1% [if required]");
                double maxOffset = 0.1;
                if (Global.options.getSpread() < maxOffset) {
                    Global.options.setSpread(maxOffset);
                }
            }
        }
    }

    private void setupNuTask() {
        LOG.info("Setting up (verbose) RPC client on " + Global.options.getNudIp() + ":" + Global.options.getNudPort());
        Global.publicAddress = Global.options.getNubitsAddress();
        Global.rpcClient = new NuRPCClient(Global.options.getNudIp(), Global.options.getNudPort(),
                Global.options.getRpcUser(), Global.options.getRpcPass(), Global.options.isVerbose(), true,
                Global.options.getNubitsAddress(), Global.options.getPair(), Global.options.getExchangeName());

        Utils.printSeparator();
        LOG.info("Starting task : Check connection with Nud");
        Global.taskManager.getCheckNudTask().start();
    }

    private void checkNuConn() throws NuBotConnectionException {
        Utils.printSeparator();
        LOG.info("Check connection with nud");
        if (Global.rpcClient.isConnected()) {
            LOG.info("RPC connection OK!");
        } else {
            //TODO: recover?
            throw new NuBotConnectionException("problem with nu connectivity");
        }
    }

    /**
     * execute the NuBot based on a configuration
     */
    public void execute(NuBotOptions opt) {

        //Load settings
        Utils.loadProperties("settings.properties");

        LOG.info("NuBot logging");
        LOG.info("Setting up  NuBot version : " + Global.settings.getProperty("version"));


        //DANGER ZONE : This variable set to true will cause orders to execute
        if (opt.isExecuteOrders()){
            liveTrading = true;
            //inform user about real trading (he should be informed by now)
        } else{

            liveTrading = false;
            //inform user we're in demo mode
        }

        Global.options = opt;

        Global.running = true;

        setupConfigBot();

        LOG.info("Create a TaskManager ");
        Global.taskManager = new TaskManager();
        Utils.printSeparator();

        if (Global.options.isSubmitliquidity()) {
            setupNuTask();
        }

        Utils.printSeparator();
        LOG.info("Starting task : Check connection with exchange");
        int conn_delay = 1;
        Global.taskManager.getCheckConnectionTask().start(conn_delay);


        Utils.printSeparator();
        LOG.info("Waiting  a for the connectionThreads to detect connection");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

        //Set the fileoutput for active orders
        //TODO! handle logging locally
        String logsFolder = "logs";
        String orders_outputPath = logsFolder + "orders_history.csv";
        String balances_outputPath = logsFolder + "balance_history.json";

        ((SubmitLiquidityinfoTask) (Global.taskManager.getSendLiquidityTask().getTask())).setOutputFiles(orders_outputPath, balances_outputPath);
        FileSystem.writeToFile("timestamp,activeOrders, sells,buys, digest\n", orders_outputPath, false);


        //Start task to check orders
        int start_delay = 40;
        Global.taskManager.getSendLiquidityTask().start(start_delay);

        Utils.printSeparator();

        if (Global.options.isSubmitliquidity()) {
            try {
                checkNuConn();
            } catch (NuBotConnectionException e) {
                exitWithNotice("" + e);
            }
        }

        Utils.printSeparator();
        LOG.info("Checking bot working mode");
        Global.isDualSide = Global.options.isDualSide();

        if (Global.options.isDualSide()) {
            LOG.info("Configuring NuBot for Dual-Side strategy");
        } else {
            LOG.info("Configuring NuBot for Sell-Side strategy");
        }

        Utils.printSeparator();

        LOG.info("Start trading Strategy specific for " + Global.options.getPair().toString());

        LOG.info(Global.options.toStringNoKeys());


        // Set the frozen balance manager in the global variable
        Global.frozenBalances = new FrozenBalancesManager(Global.options.getExchangeName(), Global.options.getPair(), Global.settings.getProperty("frozen_folder"));

        //Switch strategy for different trading pair

        if (!PegOptions.requiresSecondaryPegStrategy(Global.options.getPair())) {
            // set liquidityinfo task to the strategy
            ((StrategyPrimaryPegTask) (Global.taskManager.getStrategyFiatTask().getTask()))
                    .setSendLiquidityTask(((SubmitLiquidityinfoTask) (Global.taskManager.getSendLiquidityTask().getTask())));

            int delay = 7;
            Global.taskManager.getStrategyFiatTask().start(delay);

        } else {

            //Peg to a USD price via crypto pair
            Currency toTrackCurrency;

            if (Global.swappedPair) { //NBT as paymentCurrency
                toTrackCurrency = Global.options.getPair().getOrderCurrency();
            } else {
                toTrackCurrency = Global.options.getPair().getPaymentCurrency();
            }

            CurrencyPair toTrackCurrencyPair = new CurrencyPair(toTrackCurrency, Constant.USD);

            // set trading strategy to the price monitor task
            ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask()))
                    .setStrategy(((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask())));

            // set price monitor task to the strategy
            ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask()))
                    .setPriceMonitorTask(((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())));

            // set liquidityinfo task to the strategy

            ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask()))
                    .setSendLiquidityTask(((SubmitLiquidityinfoTask) (Global.taskManager.getSendLiquidityTask().getTask())));

            PriceFeedManager pfm = new PriceFeedManager(opt.getMainFeed(), opt.getBackupFeedNames(), toTrackCurrencyPair);
            //Then set the pfm
            ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setPriceFeedManager(pfm);

            //Set the priceDistance threshold
            ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setDistanceTreshold(opt.getDistanceThreshold());

            //Set the wallet shift threshold
            ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setWallchangeThreshold(opt.getWallchangeThreshold());

            //Set the outputpath for wallshifts

            String outputPath = logsFolder + "wall_shifts.csv";
            ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setOutputPath(outputPath);
            FileSystem.writeToFile("timestamp,source,crypto,price,currency,sellprice,buyprice,otherfeeds\n", outputPath, false);

            //read the delay to sync with remote clock
            //issue 136 - multi custodians on a pair.
            //walls are removed and re-added every three minutes.
            //Bot needs to wait for next 3 min window before placing walls
            //set the interval from settings

            int reset_every = NuBotAdminSettings.reset_every_minutes;

            int interval = 1;
            if (Global.options.isMultipleCustodians()) {
                interval = 60 * reset_every;
            } else {
                interval = NuBotAdminSettings.refresh_time_seconds;
            }
            Global.taskManager.getPriceTriggerTask().setInterval(interval);

            if (Global.options.isMultipleCustodians()) {
                //Force the a spread to avoid collisions
                double forcedSpread = 0.9;
                LOG.info("Forcing a " + forcedSpread + "% minimum spread to protect from collisions");
                if (Global.options.getSpread() < forcedSpread) {
                    Global.options.setSpread(forcedSpread);
                }
            }

            int delaySeconds = 0;

            if (Global.options.isMultipleCustodians()) {
                delaySeconds = Utils.getSecondsToNextwindow(reset_every);
                LOG.info("NuBot will be start running in " + delaySeconds + " seconds, to sync with remote NTP and place walls during next wall shift window.");
            } else {
                LOG.warn("NuBot will not try to sync with other bots via remote NTP : 'multiple-custodians' is set to false");
            }
            //then start the thread
            Global.taskManager.getPriceTriggerTask().start(delaySeconds);
        }


        notifyOnline();
    }

    private static void notifyOnline() {
        String mode = "sell-side";
        if (Global.options.isDualSide()) {
            mode = "dual-side";
        }
        String msg = "A new <strong>" + mode + "</strong> bot just came online on " + Global.options.getExchangeName() + " pair (" + Global.options.getPair().toString("_") + ")";
        HipChatNotifications.sendMessage(msg, MessageColor.GREEN);
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
}
