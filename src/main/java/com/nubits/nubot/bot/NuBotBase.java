package com.nubits.nubot.bot;

import com.nubits.nubot.RPC.NuSetup;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.tasks.BalanceFetchTask;
import com.nubits.nubot.tasks.OrderFetchTask;
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

    /**
     * the strategy setup for specific NuBots to implement
     */
    abstract public void configureStrategy() throws NuBotConfigException;

    final static Logger LOG = LoggerFactory.getLogger(NuBotBase.class);

    /**
     * configuration options tied to one instance of NuBot
     */
    protected NuBotOptions opt;

    protected String mode;

    protected String logsFolder = "logs";

    protected boolean liveTrading;


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
        if (Global.options.getExchangeName().equalsIgnoreCase(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
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

        TradeInterface ti = ExchangeFacade.getInterfaceByName(Global.options.getExchangeName());
        LOG.info("Create a new TradeInterface object");
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);


        //TODO! handle on exchange level, not bot level
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
        //TODO! handle on exchange level, not bot level
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

        //TODO: opt should be passed in constructor, not set in global

        //TODO refactor so we can test validity here again

        LOG.info("----- new session -----");
        LOG.info("Setting up NuBot version : " + Utils.versionName());

        LOG.info("NuBot logging");

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

        setupAllConfig();

        Global.running = true;

        LOG.info("Create a TaskManager ");
        Global.taskManager = new TaskManager();

        if (Global.options.isSubmitliquidity()) {
            NuSetup.setupNuRPCTask();
            NuSetup.startTask();
           // setupNuRPCTask();
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
        String orders_outputPath = logsFolder + "/" + "orders_history.csv";
        String balances_outputPath = logsFolder + "/" + "balance_history.json";

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

        try{
            configureStrategy();
        }catch(NuBotConfigException e){
            exitWithNotice("can't configure strategy");
        }

        notifyOnline();

        OrderFetchTask ft = new OrderFetchTask();
        Global.taskManager.orderFetchTask = ft;
        Thread t1 = new Thread(ft);
        t1.start();

        BalanceFetchTask bt = new BalanceFetchTask(opt.getPair());
        Global.taskManager.balanceFetchTask = bt;
        Thread t2 = new Thread(bt);
        t2.start();


    }

    protected void notifyOnline() {
        String msg = "A new <strong>" + mode + "</strong> bot just came online on " + Global.options.getExchangeName() + " pair (" + Global.options.getPair().toStringSep() + ")";
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
