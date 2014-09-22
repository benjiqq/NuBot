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
import com.nubits.nubot.models.CryptoPegOptionsJSON;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OptionsJSON;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.jhipchat.messages.Message;
import com.nubits.nubot.pricefeed.PriceFeedManager;
import com.nubits.nubot.tasks.StrategyCryptoTask;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.TradeUtils;
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

        //Create log dir
        FileSystem.mkdir(Global.settings.getProperty("log_path"));

        //Load Options
        Global.options = OptionsJSON.parseOptions(optionsPath);
        if (Global.options == null) {
            LOG.severe("Error while loading options");
            System.exit(0);
        }
        Utils.printSeparator();



        try {
            NuLogger.setup(Global.options.isVerbose());
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
            Global.publicAddress = Global.options.getNubitAddress();
            Global.rpcClient = new NuRPCClient(Global.options.getNudIp(), Global.options.getNudPort(),
                    Global.options.getRpcUser(), Global.options.getRpcPass(), Global.options.isVerbose());

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
        //Switch strategy for different trading pair


        if (Global.options.getPair().equals(Constant.NBT_USD)
                || Global.options.getPair().equals(Constant.BTC_CNY) //TODO remove this line when peatio is fixed
                || Global.options.getPair().equals(Constant.PPC_LTC)) {
            Global.taskManager.getStrategyFiatTask().start(7);
        } else if (Global.options.getPair().equals(Constant.NBT_BTC)) {

            CryptoPegOptionsJSON cpo = Global.options.getCryptoPegOptions();
            //Peg to a USD price via crypto pair
            Currency toTrackCurrency = Global.options.getPair().getPaymentCurrency();
            CurrencyPair toTrackCurrencyPair = new CurrencyPair(toTrackCurrency, Constant.USD);

            PriceFeedManager pfm = new PriceFeedManager(cpo.getMainFeed(), cpo.getBackupFeedNames(), toTrackCurrencyPair);

            //Then set the pfm
            ((StrategyCryptoTask) (Global.taskManager.getStrategyCryptoTask().getTask())).setPriceFeedManager(pfm);

            //Set the priceDistance threshold
            ((StrategyCryptoTask) (Global.taskManager.getStrategyCryptoTask().getTask())).setDistanceTreshold(cpo.getDistanceTreshold());

            //Set the priceDistance threshold
            ((StrategyCryptoTask) (Global.taskManager.getStrategyCryptoTask().getTask())).setWallchangeThreshold(cpo.getWallchangeTreshold());


            //Compute the buy/sell prices in USD
            double sellPriceUSD = TradeUtils.getSellPrice(Global.options.getTxFee());
            double buyPriceUSD = TradeUtils.getBuyPrice(Global.options.getTxFee());

            //Add(remove) the offset % from prices
            sellPriceUSD = sellPriceUSD + ((sellPriceUSD / 100) * cpo.getPriceOffset());
            buyPriceUSD = buyPriceUSD - ((buyPriceUSD / 100) * cpo.getPriceOffset());

            if (Global.isDualSide) {
                LOG.fine("Computing USD pegs with offset " + cpo.getPriceOffset() + "% : sell @ " + sellPriceUSD + " buy @ " + buyPriceUSD);
            } else {
                LOG.fine("Computing USD pegs with offset " + cpo.getPriceOffset() + "% : sell @ " + sellPriceUSD);
            }


            //Set the prices in USD
            ((StrategyCryptoTask) (Global.taskManager.getStrategyCryptoTask().getTask())).setSellPriceUSD(sellPriceUSD);
            ((StrategyCryptoTask) (Global.taskManager.getStrategyCryptoTask().getTask())).setBuyPriceUSD(buyPriceUSD);



            //set the interval from options
            Global.taskManager.getStrategyCryptoTask().setInterval(cpo.getRefreshTime());

            //then start the thread
            Global.taskManager.getStrategyCryptoTask().start(2);


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
