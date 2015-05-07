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

package com.nubits.nubot.bot;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.launch.MainLaunch;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.CcexWrapper;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.VersionInfo;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract NuBot. implements all primitives without the strategy itself
 */
public abstract class NuBotBase {

    /**
     * the strategy setup for specific NuBots to implement
     */
    abstract public void configureStrategy() throws NuBotConfigException;

    final static Logger LOG = LoggerFactory.getLogger(NuBotBase.class);

    protected String mode;

    protected boolean liveTrading;


    /**
     * all setups
     */
    protected void setupAllConfig() {

        //Generate Bot Session unique id
        SessionManager.sessionId = Utils.generateSessionID();
        LOG.info("Session ID = " + SessionManager.sessionId);

        this.mode = "sell-side";
        if (Global.options.isDualSide()) {
            this.mode = "dual-side";
        }

        setupLog();

        setupSSL();

        setupExchange();

    }


    /**
     * setup logging
     */
    protected void setupLog() {

        //for debug purposes: determine the logback.xml file used
        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        LOG.debug("Logback used '{}' as the configuration file.", mainURL);

        //Disable hipchat debug logging https://github.com/evanwong/hipchat-java/issues/16
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");

        List<ch.qos.logback.classic.Logger> llist = loggerContext.getLoggerList();

        Iterator<ch.qos.logback.classic.Logger> it = llist.iterator();
        while (it.hasNext()) {
            ch.qos.logback.classic.Logger l = it.next();
            LOG.trace("" + l);
        }
    }

    protected void setupSSL() {
        LOG.info("Set up SSL certificates");
        boolean trustAllCertificates = false;
        if (Global.options.getExchangeName().equalsIgnoreCase(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
            trustAllCertificates = true;
        }
        Utils.installKeystore(trustAllCertificates);
    }


    protected void setupExchange() {

        LOG.debug("setup Exchange object");

        LOG.debug("Wrap the keys into a new ApiKeys object");
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());

        Global.exchange = new Exchange(Global.options.getExchangeName());

        LOG.debug("Create e ExchangeLiveData object to accommodate liveData from the exchange");
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        TradeInterface ti = null;
        try {
            ti = ExchangeFacade.getInterfaceByName(Global.options.getExchangeName(), keys, Global.exchange);
        } catch (Exception e) {
            MainLaunch.exitWithNotice("exchange unknown");
        }


        //TODO handle on exchange level, not bot level
        if (Global.options.getExchangeName().equals(ExchangeFacade.CCEX)) {
            ((CcexWrapper) (ti)).initBaseUrl();
        }

        if (Global.options.getPair().getPaymentCurrency().equals(CurrencyList.NBT)) {
            Global.swappedPair = true;
        } else {
            Global.swappedPair = false;
        }

        LOG.info("Swapped pair mode : " + Global.swappedPair);

        String apibase = "";
        //TODO handle on exchange level, not bot level
        if (Global.options.getExchangeName().equalsIgnoreCase(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
            ti.setApiBaseUrl(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO_API_BASE);
        }

        //TODO exchange and tradeinterface are circular referenced
        Global.exchange.setTrade(ti);
        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());


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

    protected void checkNuConn() throws NuBotConnectionException {
        if (Global.rpcClient.isConnected()) {
            LOG.info("Nud RPC connection ok.");
        } else {
            //TODO: recover?
            throw new NuBotConnectionException("Problem with nud connectivity");
        }
    }

    /**
     * test setup exchange
     *
     * @throws NuBotRunException
     */
    public void testExchange() throws NuBotRunException {

        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
        } else {
            throw new NuBotRunException("could not query exchange: [ " + activeOrdersResponse.getError() + " ]");
        }
    }

    /**
     * execute the NuBot based on a configuration
     */
    public void execute(NuBotOptions opt) throws NuBotRunException {

        LOG.info("Setting up NuBot version : " + VersionInfo.getVersionName());

        if (opt.isExecuteOrders()) {
            liveTrading = true;
            LOG.info("Live mode : Trades will be executed");
        } else {
            LOG.info("Demo mode: Trades will not be executed [executetrade:false]");
            liveTrading = false;
        }

        Global.options = opt;

        setupAllConfig();

        LOG.debug("Create a TaskManager ");
        Global.taskManager = new TaskManager();
        Global.taskManager.setTasks();

        if (Global.options.isSubmitliquidity()) {
            Global.taskManager.setupNuRPCTask();
            Global.taskManager.startTaskNu();
        }

        LOG.debug("Starting task : Check connection with exchange");

        Global.taskManager.getCheckConnectionTask().start(Settings.DELAY_CONN);

        LOG.info("Waiting a for the connectionThreads to detect connection");
        try {
            Thread.sleep(Settings.WAIT_CHECK_INTERVAL);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

        testExchange();

        //Start task to check orders
        try {
            Global.taskManager.getSendLiquidityTask().start(Settings.DELAY_LIQUIIDITY);
        } catch (Exception e) {
            throw new NuBotRunException("" + e);
        }

        if (Global.options.isSubmitliquidity()) {
            try {
                checkNuConn();
            } catch (NuBotConnectionException e) {
                MainLaunch.exitWithNotice("can't connect to Nu " + e);
            }
        }

        LOG.info("Start trading Strategy specific for " + Global.options.getPair().toString());

        LOG.info("Options loaded : " + Global.options.toStringNoKeys());

        // Set the frozen balance manager in the global variable

        Global.frozenBalancesManager = new FrozenBalancesManager(Global.options.getExchangeName(), Global.options.getPair());

        try {
            configureStrategy();
        } catch (Exception e) {
            throw new NuBotRunException("" + e);
        }

        notifyOnline();

    }

    protected void notifyOnline() {
        String exc = Global.options.getExchangeName();
        String p = Global.options.getPair().toStringSep();
        String msg = "A new <strong>" + mode + "</strong> bot just came online on " + exc + " pair (" + p + ")";
        LOG.debug("notify online " + msg);
        HipChatNotifications.sendMessage(msg, MessageColor.GREEN);
    }

    private void logSessionStatistics() {

        LOG.info("session statistics");
        //log closing statistics
        LOG.info("totalOrdersSubmitted " + Global.orderManager.getTotalOrdersSubmitted());

        String openStrongTaging = "<strong>";
        String closingStrongTaging = "</strong>";

        String additionalInfo = "after " + Utils.getBotUptimeDate() + " uptime on "
                + openStrongTaging + Global.options.getExchangeName() + closingStrongTaging + " ["
                + Global.options.getPair().toStringSep() + "]";

        LOG.info(additionalInfo.replace(closingStrongTaging, "").replace(openStrongTaging, "")); //Remove html tags
        HipChatNotifications.sendMessageCritical("Bot shut-down " + additionalInfo);

        LOG.debug("startup duration [msec]: " + SessionManager.startupDuration);
    }

    public void shutdownBot() {

        LOG.info("Bot shutting down sequence started.");

        //Interrupt all BotTasks

        if (Global.taskManager != null) {
            if (Global.taskManager.isInitialized()) {
                try {
                    LOG.info("try to shutdown all tasks");
                    Global.taskManager.stopAll();
                } catch (IllegalStateException e) {
                    LOG.error(e.toString());
                }
            }
        }

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
                LOG.error("error canceling orders: " + deleteOrdersResponse.getError().toString());
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
                    LOG.debug(responseObject1.toJSONString());
                }

                JSONObject responseObject2 = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
                        0, 0, 2);
                if (null == responseObject2) {
                    LOG.error("Something went wrong while sending liquidityinfo");
                } else {
                    LOG.debug(responseObject2.toJSONString());
                }
            }
        }


        LOG.info("Logs of this session saved in " + Global.sessionPath);
        SessionManager.setModeHalted();
        SessionManager.sessionStopped = System.currentTimeMillis();
        LOG.info("** end of the session **");

        logSessionStatistics();

    }

}
