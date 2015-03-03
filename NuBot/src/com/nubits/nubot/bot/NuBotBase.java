package com.nubits.nubot.bot;

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.CcexWrapper;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.Utils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract NuBot. implements all primitives without the strategy itself
 */
public abstract class NuBotBase {

    final static Logger LOG = LoggerFactory.getLogger(NuBotBase.class);

    /**
     * configuration options tied to one instance of NuBot
     */
    protected NuBotOptions opt;

    protected String mode;

    protected String logsFolder = "logs";

    protected boolean liveTrading;

    abstract public void configureStrategy();

    /**
     * all setups
     */
    protected void setupAllConfig() {

        //Generate Bot Session unique id
        Global.sessionId = Utils.generateSessionID();
        LOG.info("Session ID = " + Global.sessionId);

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
        //Setting up log folder for this session
        //done over logback.xml

        //Disable hipchat debug logging https://github.com/evanwong/hipchat-java/issues/16
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "error");
    }

    protected void setupSSL() {
        LOG.info("Set up SSL certificates");
        boolean trustAllCertificates = false;
        if (Global.options.getExchangeName().equalsIgnoreCase(Constant.INTERNAL_EXCHANGE_PEATIO)) {
            trustAllCertificates = true;
        }
        Utils.installKeystore(trustAllCertificates);
    }


    protected void setupExchange() {
        LOG.info("setup Exchange object");

        LOG.info("Wrap the keys into a new ApiKeys object");
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());

        Global.exchange = new Exchange(Global.options.getExchangeName());

        LOG.info("Create e ExchangeLiveData object to accommodate liveData from the exchange");
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        LOG.info("Create a new TradeInterface object");
        TradeInterface ti = Global.exchange.getTradeInterface();
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);
        //TODO! handle on exchange level, not bot level
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
        //TODO! handle on exchange level, not bot level
        if (Global.options.getExchangeName().equalsIgnoreCase(Constant.INTERNAL_EXCHANGE_PEATIO)) {
            ti.setApiBaseUrl(Constant.INTERNAL_EXCHANGE_PEATIO_API_BASE);
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

    /**
     * setup the task for checking Nu RPC
     */
    protected void setupNuRPCTask() {
        LOG.info("Setting up (verbose) RPC client on " + Global.options.getNudIp() + ":" + Global.options.getNudPort());
        Global.publicAddress = Global.options.getNubitsAddress();
        Global.rpcClient = new NuRPCClient(Global.options.getNudIp(), Global.options.getNudPort(),
                Global.options.getRpcUser(), Global.options.getRpcPass(), Global.options.isVerbose(), true,
                Global.options.getNubitsAddress(), Global.options.getPair(), Global.options.getExchangeName());

        LOG.info("Starting task : Check connection with Nud");
        Global.taskManager.getCheckNudTask().start();
    }

    protected void checkNuConn() throws NuBotConnectionException {

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

        setupAllConfig();

        //TODO: opt should be passed in constructor, not set in global

        //TODO refactor so we can test validity here again

        LOG.info("NuBot logging");
        LOG.info("Setting up  NuBot version : " + Global.settings.getProperty("version"));

        //DANGER ZONE : This variable set to true will cause orders to execute
        if (opt.isExecuteOrders()) {
            liveTrading = true;
            //inform user about real trading (he should be informed by now)
        } else {
            liveTrading = false;
            //inform user we're in demo mode
        }

        //TODO set to this class
        Global.options = opt;

        Global.running = true;

        LOG.info("Create a TaskManager ");
        Global.taskManager = new TaskManager();

        if (Global.options.isSubmitliquidity()) {
            setupNuRPCTask();
        }


        LOG.info("Starting task : Check connection with exchange");
        int conn_delay = 1;
        Global.taskManager.getCheckConnectionTask().start(conn_delay);


        LOG.info("Waiting  a for the connectionThreads to detect connection");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

        //Set the fileoutput for active orders
        //TODO! handle logging locally
        String orders_outputPath = logsFolder + "orders_history.csv";
        String balances_outputPath = logsFolder + "balance_history.json";

        ((SubmitLiquidityinfoTask) (Global.taskManager.getSendLiquidityTask().getTask())).setOutputFiles(orders_outputPath, balances_outputPath);
        FileSystem.writeToFile("timestamp,activeOrders, sells,buys, digest\n", orders_outputPath, false);

        //Start task to check orders
        int start_delay = 40;
        Global.taskManager.getSendLiquidityTask().start(start_delay);

        if (Global.options.isSubmitliquidity()) {
            try {
                checkNuConn();
            } catch (NuBotConnectionException e) {
                exitWithNotice("" + e);
            }
        }

        LOG.info("Checking bot working mode");

        LOG.info("Start trading Strategy specific for " + Global.options.getPair().toString());

        LOG.info(Global.options.toStringNoKeys());

        // Set the frozen balance manager in the global variable
        Global.frozenBalances = new FrozenBalancesManager(Global.options.getExchangeName(), Global.options.getPair(), Global.settings.getProperty("frozen_folder"));

        configureStrategy();

        notifyOnline();
    }

    protected void notifyOnline() {
        String msg = "A new <strong>" + mode + "</strong> bot just came online on " + Global.options.getExchangeName() + " pair (" + Global.options.getPair().toString("_") + ")";
        HipChatNotifications.sendMessage(msg, MessageColor.GREEN);
    }


    /**
     * exit application and notify user
     *
     * @param msg
     */
    protected static void exitWithNotice(String msg) {
        LOG.error(msg);
        System.exit(0);
    }

}
