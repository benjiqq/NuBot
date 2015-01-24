package com.nubits.nubot.tests;

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.global.Passwords;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.models.Trade;
import com.nubits.nubot.options.OptionsJSON;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by woolly_sammoth on 24/01/15.
 */
public class TestWrapperReturns {

    private static final Logger LOG = Logger.getLogger(TestWrapperReturns.class.getName());
    /**
     * Configure tests
     */
    //private static final String TEST_OPTIONS_PATH = "res/options/private/old/options-full.json";
    private static final String TEST_OPTIONS_PATH = "options.json";
    public static ArrayList<String> testExchanges = new ArrayList<>();
    public static final CurrencyPair testPair = Constant.NBT_BTC;
    public static final Currency testCurrency = Constant.NBT;

    public static void main(String[] args) {
        //Load settings
        Utils.loadProperties("settings.properties");
        init();
        String[] inputs = new String[1];
        inputs[0] = TEST_OPTIONS_PATH;
        Global.options = OptionsJSON.parseOptions(inputs);
        testExchanges = populateExchanges();

        for (Iterator<String> exchange = testExchanges.iterator(); exchange.hasNext();) {
            String testExchange = exchange.next();
            configExchange(testExchange);
            runTests();
        }
        System.exit(0);
    }


    private static void runTests() {

        LOG.warning("Testing " + Global.exchange.getName());
        sell(testPair, 1, 0.4);


    }

    private static void sell(CurrencyPair pair, double amount, double price) {
        //test that sell requests are processed correctly
        ApiResponse response = Global.exchange.getTrade().sell(pair, amount, price);
        if (response.isPositive()) {
            LOG.warning(response.getResponseObject().toString());
        } else {
            LOG.severe(response.getError().toString());
        }
    }

    private static void init() {
        String folderName = "testwrappers_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;
        //Create log dir
        FileSystem.mkdir(logsFolder);
        try {
            NuLogger.setup(false, logsFolder);
        } catch (IOException ex) {
            LOG.severe(ex.toString());
        }

        System.setProperty("javax.net.ssl.trustStore", Global.settings.getProperty("keystore_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", Global.settings.getProperty("keystore_pass"));
    }

    private static ArrayList<String> populateExchanges() {
        ArrayList<String> testExchanges = new ArrayList<>();
        testExchanges.add(Constant.BTCE);
        testExchanges.add(Constant.INTERNAL_EXCHANGE_PEATIO);
        testExchanges.add(Constant.BTER);
        testExchanges.add(Constant.CCEDK);
        testExchanges.add(Constant.POLONIEX);
        testExchanges.add(Constant.CCEX);
        testExchanges.add(Constant.ALLCOIN);
        testExchanges.add(Constant.BITSPARK_PEATIO);
        testExchanges.add(Constant.EXCOIN);
        testExchanges.add(Constant.BITCOINCOID);

        return testExchanges;
    }

    public static void configExchange(String exchangeName) {
        ApiKeys keys;

        Global.exchange = new Exchange(exchangeName);

        //Create e ExchangeLiveData object to accomodate liveData from the Global.exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        Global.options.setExchangeName(exchangeName);

        if (exchangeName.equals(Constant.BTCE)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.BTCE_SECRET, Passwords.BTCE_KEY);
            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange

            Global.exchange.setTrade(new BtceWrapper(keys, Global.exchange));

        } else if (exchangeName.equals(Constant.INTERNAL_EXCHANGE_PEATIO)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.INTERNAL_PEATIO_SECRET, Passwords.INTERNAL_PEATIO_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new PeatioWrapper(keys, Global.exchange, Constant.INTERNAL_EXCHANGE_PEATIO_API_BASE));
        } else if (exchangeName.equals(Constant.CCEDK)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.CCEDK_SECRET, Passwords.CCEDK_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new CcedkWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.BTER)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.BTER_SECRET, Passwords.BTER_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new BterWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.POLONIEX)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.POLONIEX_SECRET, Passwords.POLONIEX_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new PoloniexWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.CCEX)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.CCEX_SECRET, "");

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new CcexWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.ALLCOIN)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.ALLCOIN_SECRET, Passwords.ALLCOIN_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new AllCoinWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.BITSPARK_PEATIO)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.BITSPARK_SECRET, Passwords.BITSPARK_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new BitSparkWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.EXCOIN)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.EXCOIN_SECRET, Passwords.EXCOIN_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new ExcoinWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(Constant.BITCOINCOID)) {
            //Wrap the keys into a new ApiKeys object
            keys = new ApiKeys(Passwords.BITCOINCOID_SECRET, Passwords.BITCOINCOID_KEY);

            //Create a new TradeInterface object using the custom implementation
            //Assign the TradeInterface to the exchange
            Global.exchange.setTrade(new BitcoinCoIDWrapper(keys, Global.exchange));
        } else {
            LOG.severe("Exchange " + exchangeName + " not supported");
            System.exit(0);
        }

        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());

        //Create a TaskManager and
        Global.taskManager = new TaskManager();
        //Start checking for connection
        Global.taskManager.getCheckConnectionTask().start();


        //Wait a couple of seconds for the connectionThread to get live
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.severe(ex.toString());
        }

        /* Setup (end) ------------------------------------------------------------------------------------------------------ */
    }
}
