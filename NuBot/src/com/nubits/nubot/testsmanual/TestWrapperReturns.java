package com.nubits.nubot.testsmanual;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
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
    public static CurrencyPair testPair = Constant.NBT_BTC;
    public static final double testNBTAmount = 1;
    public static final double sellPrice = 0.04;
    public static final double buyPrice = 0.0004;

    public static void main(String[] args) {
        //Load settings
        Utils.loadProperties("settings.properties");
        init();
        String[] inputs = new String[1];
        inputs[0] = TEST_OPTIONS_PATH;
        try {
            Global.options = ParseOptions.parseOptions(inputs);
        } catch (NuBotConfigException ex) {
            Logger.getLogger(TestWrapperReturns.class.getName()).log(Level.SEVERE, null, ex);
        }
        testExchanges = populateExchanges();

        //configExchange(Constant.BTER);
        //getOpenOrders();
        //runTests();

        for (Iterator<String> exchange = testExchanges.iterator(); exchange.hasNext();) {
            String testExchange = exchange.next();
            WrapperTestUtils.configExchange(testExchange);
            runTests();
        }

        System.exit(0);
    }

    private static void runTests() {
        if (Global.exchange.getName().equals(Constant.EXCOIN)) {
            testPair = Constant.BTC_NBT;
        } else {
            testPair = Constant.NBT_BTC;
        }
        print("Testing " + Global.exchange.getName());
        clearOrders(testPair);
        String order_id;
        print("SELL " + testNBTAmount + " NBT @ " + sellPrice + " BTC");
        order_id = sell(testPair, testNBTAmount, sellPrice);
        sleep();
        if (order_id != null) {
            Order order = getOrderDetail(order_id);
            Amount amount = order.getAmount();
            if (!amount.getCurrency().equals(testPair.getOrderCurrency())) {
                print("BAD - Amount not listed in " + testPair.getOrderCurrency().getCode() + "\nReturned " + amount.getCurrency().getCode());
            } else if (amount.getQuantity() != testNBTAmount) {
                print("BAD - Amount Quantity does not equal " + Double.toString(testNBTAmount));
            } else {
                print("Amount OK");
            }
            Amount price = order.getPrice();
            if (!price.getCurrency().equals(testPair.getPaymentCurrency())) {
                print("BAD - Price not listed in " + testPair.getPaymentCurrency().getCode());
            } else if (price.getQuantity() != sellPrice) {
                print("BAD - Price Quantity does not equal " + Double.toString(sellPrice));
            } else {
                print("Price OK");
            }
            cancelOrder(order_id, testPair);
        } else {
            print("BAD - Order not placed");
        }

        print("BUY " + testNBTAmount + " NBT @ " + buyPrice + " BTC");
        order_id = buy(testPair, testNBTAmount, buyPrice);
        sleep();
        if (order_id != null) {
            Order order = getOrderDetail(order_id);
            Amount amount = order.getAmount();
            if (!amount.getCurrency().equals(testPair.getOrderCurrency())) {
                print("BAD - Amount not listed in " + testPair.getOrderCurrency().getCode() + "\nReturned " + amount.getCurrency().getCode());
            } else if (amount.getQuantity() != testNBTAmount) {
                print("BAD - Amount Quantity does not equal " + Double.toString(testNBTAmount));
            } else {
                print("Amount OK");
            }
            Amount price = order.getPrice();
            if (!price.getCurrency().equals(testPair.getPaymentCurrency())) {
                print("BAD - Price not listed in " + testPair.getPaymentCurrency().getCode());
            } else if (price.getQuantity() != buyPrice) {
                print("BAD - Price Quantity does not equal " + Double.toString(buyPrice));
            } else {
                print("Price OK");
            }
            cancelOrder(order_id, testPair);
        } else {
            print("BAD - Order not placed");
        }


    }

    private static void clearOrders(CurrencyPair pair) {
        ApiResponse response = Global.exchange.getTrade().clearOrders(pair);
        if (response.isPositive()) {
            //LOG.warning("\nresponse object: " + response.getResponseObject().toString());
        } else {
            print("Error: " + response.getError().toString());
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            print(ie.toString());
        }
    }

    private static void print(String s) {
        System.out.println(s);
    }

    private static boolean cancelOrder(String order_id, CurrencyPair pair) {
        ApiResponse response = Global.exchange.getTrade().cancelOrder(order_id, pair);
        if (response.isPositive()) {
            //LOG.warning("\nresponse object: " + response.getResponseObject().toString());
            return (boolean) response.getResponseObject();
        } else {
            print("Error: " + response.getError().toString());
            return false;
        }
    }

    private static Order getOrderDetail(String order_id) {
        ApiResponse response = Global.exchange.getTrade().getOrderDetail(order_id);
        if (response.isPositive()) {
            //LOG.warning("\nresponse object: " + response.getResponseObject().toString());
            return (Order) response.getResponseObject();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static String getOpenOrders() {
        ApiResponse response = Global.exchange.getTrade().getActiveOrders();
        if (response.isPositive()) {
            print("Response object: " + response.getResponseObject().toString());
            return response.getResponseObject().toString();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static void getBalance(CurrencyPair pair) {
        //get the available balances for pair
        ApiResponse response = Global.exchange.getTrade().getAvailableBalances(pair);
        if (response.isPositive()) {
            //LOG.warning("\nresponse object: " + response.getResponseObject().toString());
        } else {
            print("Error: " + response.getError().toString());
        }
    }

    private static String sell(CurrencyPair pair, double amount, double price) {
        //test that sell requests are processed correctly
        ApiResponse response = Global.exchange.getTrade().sell(pair, amount, price);
        if (response.isPositive()) {
            //LOG.warning("\nresponse object: " + response.getResponseObject().toString());
            return response.getResponseObject().toString();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static String buy(CurrencyPair pair, double amount, double price) {
        //test that sell requests are processed correctly
        ApiResponse response = Global.exchange.getTrade().buy(pair, amount, price);
        if (response.isPositive()) {
            //LOG.warning("\nresponse object: " + response.getResponseObject().toString());
            return response.getResponseObject().toString();
        } else {
            print("Error: " + response.getError().toString());
            return null;
        }
    }

    private static void init() {
        String folderName = "testwrapperreturns_" + System.currentTimeMillis() + "/";
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
        //testExchanges.add(Constant.BTCE);
        testExchanges.add(Constant.INTERNAL_EXCHANGE_PEATIO);
        testExchanges.add(Constant.BTER);
        testExchanges.add(Constant.CCEDK);
        testExchanges.add(Constant.POLONIEX);
        testExchanges.add(Constant.ALLCOIN);
        testExchanges.add(Constant.BITSPARK_PEATIO);
        testExchanges.add(Constant.EXCOIN);
        testExchanges.add(Constant.BITCOINCOID);

        return testExchanges;
    }
}
