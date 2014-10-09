/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
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
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OptionsJSON;
import com.nubits.nubot.models.SecondaryPegOptionsJSON;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.jhipchat.messages.Message;
import com.nubits.nubot.pricefeed.PriceFeedManager;
import com.nubits.nubot.tasks.CheckOrdersTask;
import com.nubits.nubot.tasks.PriceMonitorTriggerTask;
import com.nubits.nubot.tasks.StrategySecondaryPegTask;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class NuBot {

    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json>";
    private String optionsPath;
    private String logsFolder;
    private static Thread mainThread;
    private static final Logger LOG = Logger.getLogger(NuBot.class.getName());

    public static void main(String args[]) {
        mainThread = Thread.currentThread();

        NuBot app = new NuBot();

        Utils.printSeparator();
        if (app.readParams(args)) {
            createShutDownHook();
            if (!Global.running) {
                app.execute();
            } else {
                LOG.severe("NuBot is already running. Make sure to terminate other instances.");
            }
        } else {
            System.exit(0);
        }
    }

    private void execute() {
        Global.running = true;

        //Load settings
        Utils.loadProperties("settings.properties");

      
        //Load Options
        Global.options = OptionsJSON.parseOptions(optionsPath);
        if (Global.options == null) {
            LOG.severe("Error while loading options");
            System.exit(0);
        }
        Utils.printSeparator();


        //Setting up log folder for this session :
        
        String folderName = "NuBot_"+System.currentTimeMillis()+"_"+Global.options.getExchangeName()+"_"+Global.options.getPair().toString().toUpperCase()+"/";
        logsFolder = Global.settings.getProperty("log_path")+folderName;
        
        //Create log dir
        FileSystem.mkdir(logsFolder);
        try {
            NuLogger.setup(Global.options.isVerbose(),logsFolder);
        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        LOG.fine("Setting up  NuBot" + Global.settings.getProperty("version"));

        LOG.fine("Init logging system");

        LOG.fine("Set up SSL certificates");
        System.setProperty("javax.net.ssl.trustStore", Global.settings.getProperty("keystore_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", Global.settings.getProperty("keystore_pass"));
        Utils.printSeparator();

        LOG.fine("Load options from " + optionsPath);
        Utils.printSeparator();


        LOG.fine("Wrap the keys into a new ApiKeys object");
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());
        Utils.printSeparator();


        LOG.fine("Creating an Exchange object");

        Global.exchange = new Exchange(Global.options.getExchangeName());
        Utils.printSeparator();

        LOG.fine("Create e ExchangeLiveData object to accomodate liveData from the exchange");
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);
        Utils.printSeparator();


        LOG.fine("Create a new TradeInterface object");
        TradeInterface ti = Exchange.getTradeInterface(Global.options.getExchangeName());
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);

        String apibase = "";
        if (Global.options.getExchangeName().equalsIgnoreCase(Constant.PEATIO_BTCCNY)) {
            ti.setApiBaseUrl(Constant.PEATIO_BTCCNY_API_BASE);
        } else if (Global.options.getExchangeName().equalsIgnoreCase(Constant.PEATIO_MULTIPAIR_API_BASE)) {
            ti.setApiBaseUrl(Constant.PEATIO_MULTIPAIR_API_BASE);
        }


        Global.exchange.setTrade(ti);
        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());
        Utils.printSeparator();


        LOG.fine("Create a TaskManager ");
        Global.taskManager = new TaskManager();
        Utils.printSeparator();

        if (Global.options.isSendRPC()) {
            LOG.fine("Setting up (verbose) RPC client on " + Global.options.getNudIp() + ":" + Global.options.getNudPort());
            Global.publicAddress = Global.options.getNubitsAddress();
            Global.rpcClient = new NuRPCClient(Global.options.getNudIp(), Global.options.getNudPort(),
                    Global.options.getRpcUser(), Global.options.getRpcPass(), Global.options.isVerbose(), true,
                    Global.options.getNubitsAddress(), Global.options.getPair(), Global.options.getExchangeName());

            Utils.printSeparator();
            LOG.fine("Starting task : Check connection with Nud  ");
            Global.taskManager.getCheckNudTask().start();
        }

        Utils.printSeparator();
        LOG.fine("Starting task : Check connection with exchange  ");
        Global.taskManager.getCheckConnectionTask().start(1);


        Utils.printSeparator();
        LOG.fine("Waiting  a for the connectionThreads to detect connection");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOG.severe(ex.getMessage());
        }


        //Set the fileoutput for active orders
        

        String orders_outputPath =  logsFolder + "orders_history.csv";
        ((CheckOrdersTask) (Global.taskManager.getCheckOrdersTask().getTask())).setOutputFile(orders_outputPath);
        FileSystem.writeToFile("timestamp,activeOrders, sells,buys, digest\n", orders_outputPath, false);

        
        //Start task to check orders
        Global.taskManager.getCheckOrdersTask().start(13);

        Utils.printSeparator();
        /*
         LOG.fine("Validating API keys" );
         ApiResponse permissionResponse = exchange.getTrade().getPermissions();
         if(permissionResponse.isPositive()) {
         LOG.fine("ApiKeys OK: ");
         ApiPermissions permissions = (ApiPermissions) permissionResponse.getResponseObject();

         LOG.fine("Keys Valid :"+permissions.isValid_keys() +"\n" +
         "getinfo : "+ permissions.isGet_info() +"\n" +
         "trade : "  + permissions.isTrade());
         }
         else{
         LOG.severe("Problem with ApiKeys");
         permissionResponse.getError().println();
         System.exit(0);
         }

         */
        if (Global.options.isSendRPC()) {
            Utils.printSeparator();
            LOG.fine("Check connection with nud");
            if (Global.rpcClient.isConnected()) {
                LOG.fine("RPC connection OK!");
            } else {
                LOG.severe("Problem while connecting with nud");
                System.exit(0);
            }
        }


        Utils.printSeparator();
        LOG.fine("Checking bot working mode");
        Global.isDualSide = Global.options.isDualSide();

        if (Global.options.isDualSide()) {
            LOG.info("Configuring NuBot for Dual-Side strategy");
        } else {
            LOG.info("Configuring NuBot for Sell-Side strategy");
        }

        Utils.printSeparator();


        //DANGER ZONE : This variable set to true will cause orders to execute
        Global.executeOrders = Global.options.isExecuteOrders();


        LOG.info("Start trading Strategy specific for " + Global.options.getPair().toString());

        LOG.info(Global.options.toStringNoKeys());


        // Set the frozen balance manager in the global variable
        Global.frozenBalances = new FrozenBalancesManager(Global.options.getExchangeName(), Global.options.getPair(), Global.settings.getProperty("frozen_folder"));


        //Switch strategy for different trading pair


        if (Utils.isSupported(Global.options.getPair())) {
            if (!Utils.requiresSecondaryPegStrategy(Global.options.getPair())) {
                Global.taskManager.getStrategyFiatTask().start(7);
            } else {

                SecondaryPegOptionsJSON cpo = Global.options.getSecondaryPegOptions();
                if (cpo == null) {
                    LOG.severe("To run in secondary peg mode, you need to specify the crypto-peg-options");
                    System.exit(0);
                }

                //Peg to a USD price via crypto pair
                Currency toTrackCurrency = Global.options.getPair().getPaymentCurrency();
                CurrencyPair toTrackCurrencyPair = new CurrencyPair(toTrackCurrency, Constant.USD);

                //Set the wallet shift threshold

                StrategySecondaryPegTask secondaryPegStrategy = ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask()));

                //Then set trading strategy
                ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setStrategy(secondaryPegStrategy);

                PriceFeedManager pfm = new PriceFeedManager(cpo.getMainFeed(), cpo.getBackupFeedNames(), toTrackCurrencyPair);
                //Then set the pfm
                ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setPriceFeedManager(pfm);

                //Set the priceDistance threshold
                ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setDistanceTreshold(cpo.getDistanceTreshold());

                //Set the wallet shift threshold
                ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setWallchangeThreshold(cpo.getWallchangeTreshold());

                //Set the outputpath for wallshifts

                String outputPath = logsFolder + "wall_shifts.csv";
                ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setOutputPath(outputPath);
                FileSystem.writeToFile("timestamp,source,crypto,price,currency,sellprice,buyprice,otherfeeds\n", outputPath, false);

                
                //set the interval from options
                Global.taskManager.getPriceTriggerTask().setInterval(cpo.getRefreshTime());

                //then start the thread
                Global.taskManager.getPriceTriggerTask().start(2);
            }
        } else {
            LOG.severe("This bot doesn't work yet with trading pair " + Global.options.getPair().toString());
            System.exit(0);
        }
        
        
       
        
        String mode = "sell-side";
        if (Global.options.isDualSide()) {
            mode = "dual-side";
        }
        HipChatNotifications.sendMessage("A new <strong>" + mode + "</strong> bot just came online on " + Global.options.getExchangeName() + " pair (" + Global.options.getPair().toString("_") + ")", Message.Color.GREEN);
    }

    private boolean readParams(String[] args) {
        boolean ok = false;
        if (args.length != 1) {
            LOG.severe("wrong argument number : call it with \n" + USAGE_STRING);
            System.exit(0);
        }
        optionsPath = args[0];
        ok = true;
        return ok;
    }

    private static void createShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {

                LOG.info("Bot shut down");
                NuBot.mainThread.interrupt();
                if (Global.taskManager != null) {
                    if (Global.taskManager.isInitialized()) {
                        Global.taskManager.stopAll();
                    }
                }
                Thread.currentThread().interrupt();
                return;
            }
        }));
    }
}
