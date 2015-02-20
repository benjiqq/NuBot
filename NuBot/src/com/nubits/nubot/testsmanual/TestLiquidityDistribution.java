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
package com.nubits.nubot.testsmanual;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OrderToPlace;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurve;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurveLin;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurveLog;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityDistributionModel;
import com.nubits.nubot.trading.LiquidityDistribution.ModelParameters;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestLiquidityDistribution {

    private static final Logger LOG = Logger.getLogger(TestLiquidityDistribution.class.getName());
    private static final String TEST_OPTIONS_PATH = "res/options/private/old/options-full.json";
    private LiquidityDistributionModel ldm;
    private ModelParameters sellParams;
    private ModelParameters buyParams;
    private Amount balanceNBT;
    private Amount balancePEG;
    private CurrencyPair pair;
    private double txFee;
    private boolean execOrders;
    double pegPrice;

    public static void main(String a[]) {
        TestLiquidityDistribution test = new TestLiquidityDistribution();
        String[] inputs = new String[1];
        inputs[0] = TEST_OPTIONS_PATH;
        try {
            Global.options = ParseOptions.parseOptions(inputs);
        } catch (NuBotConfigException ex) {
            LOG.severe(ex.toString());
        }
        test.init(Constant.INTERNAL_EXCHANGE_PEATIO); //Pass an empty string to avoid placing the orders
        test.configureTest();
        test.exec();

    }

    private void init(String exchangeName) {
        Utils.loadProperties("settings.properties");
        //feed = new BitcoinaveragePriceFeed();
        String folderName = "tests_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;
        try {
            NuLogger.setup(false, logsFolder);
        } catch (IOException ex) {
            LOG.severe(ex.toString());
        }
        LOG.setLevel(Level.INFO);

        execOrders = false;
        if (!exchangeName.equals("")) {
            //Setup the exchange
            execOrders = true;
            pair = Constant.NBT_BTC;
            WrapperTestUtils.configExchange(exchangeName);
            WrapperTestUtils.testClearAllOrders(pair);
        }


    }

    private void configureTest() {
        LOG.info("Configuring test");

        //Custodian balance simulation
        balanceNBT = new Amount(27100.0, Constant.NBT);
        balancePEG = new Amount(100, Constant.BTC);
        if (execOrders) {
            configureBalances(pair);
        }

        pegPrice = 300; // value of 1 unit expressed in USD

        txFee = 0.2; // %

        //Configure sell Params
        double sellOffset = 0.01;
        double sellWallHeight = 60;
        double sellWallWidth = 0.15;
        double sellWallDensity = 0.020;

        //Configure Liquidity curve
        //LiquidityCurve sellCurve = new LiquidityCurveLin(LiquidityCurve.STEEPNESS_MID); //Linear
        //LiquidityCurve sellCurve = new LiquidityCurveExp(LiquidityCurve.STEEPNESS_LOW); //Exponential
        LiquidityCurve sellCurve = new LiquidityCurveLog(LiquidityCurve.STEEPNESS_LOW); //Logarithmic

        //Configure buy Params
        double buyOffset = 0.01;
        double buyWallHeight = 4;
        double buyWallWidth = 0.15;
        double buyWallDensity = 0.020;
        //Configure Liquidity curve
        LiquidityCurve buyCurve = new LiquidityCurveLin(LiquidityCurve.STEEPNESS_MID); //Linear
        //LiquidityCurve buyCurve = new LiquidityCurveExp(LiquidityCurve.STEEPNESS_HIGH); //Exponential
        //LiquidityCurve buyCurve = new LiquidityCurveLog(LiquidityCurve.STEEPNESS_LOW);//Logarithmic


        sellParams = new ModelParameters(sellOffset, sellWallHeight, sellWallWidth, sellWallDensity, sellCurve);
        buyParams = new ModelParameters(buyOffset, buyWallHeight, buyWallWidth, buyWallDensity, buyCurve);

        String config = "Sell order book configuration : " + sellParams.toString();
        config += "Buy order book configuration : " + buyParams.toString();
        config += "Pair : " + pair.toString();
        config += "\nbalanceNBT : " + balanceNBT.getQuantity();
        config += "\nbalancePEG : " + balancePEG.getQuantity();
        config += "\npegPrice : " + pegPrice;
        config += "\ntxFee : " + txFee;
        config += "\n\n -------------------";

        LOG.info(config);

    }

    private void exec() {
        ldm = new LiquidityDistributionModel(sellParams, buyParams);

        ArrayList<OrderToPlace> sellOrders = ldm.getOrdersToPlace(Constant.SELL, balanceNBT, pegPrice, pair, txFee);
        ArrayList<OrderToPlace> buyOrders = ldm.getOrdersToPlace(Constant.BUY, balancePEG, pegPrice, pair, txFee);

        printOrderBooks(sellOrders, buyOrders);
        Utils.drawOrderBooks(sellOrders, buyOrders, pegPrice);

        if (execOrders) {
            placeOrders(sellOrders, buyOrders);
        }

    }

    private void printOrderBooks(ArrayList<OrderToPlace> sellOrders, ArrayList<OrderToPlace> buyOrders) {
        String sellOrdersString = printOrderBook(sellOrders, Constant.SELL);
        String buyOrdersString = printOrderBook(buyOrders, Constant.BUY);

        LOG.info(sellOrdersString + "\n" + buyOrdersString);

    }

    private String printOrderBook(ArrayList<OrderToPlace> orders, String type) {
        String toReturn = "----- " + type + " order book\n";
        double sumSize = 0;
        for (int i = 0; i < orders.size(); i++) {
            OrderToPlace tempOrder = orders.get(i);
            toReturn += Utils.round(tempOrder.getPrice() * pegPrice, 6) + "," + tempOrder.getPrice() + "," + tempOrder.getSize() + "\n";
            sumSize += tempOrder.getSize();
        }
        toReturn += "Order book size = " + sumSize + " NBT ";

        double buyBalanceNBT = Utils.round(balancePEG.getQuantity() * pegPrice, 8);
        double sellBalanceNBT = balanceNBT.getQuantity();

        boolean overThreshold = false;
        if (type.equals(Constant.SELL) && sumSize > sellBalanceNBT) {
            overThreshold = true;
        }

        if (type.equals(Constant.BUY) && sumSize > buyBalanceNBT) {
            overThreshold = true;
        }

        if (overThreshold) {
            toReturn += "\n\n!The funds are not sufficient to satisfy current order books configuration!";
        }


        toReturn += "----- ";
        return toReturn;
    }

    private void placeOrders(ArrayList<OrderToPlace> sellOrders, ArrayList<OrderToPlace> buyOrders) {

        long startTime = System.nanoTime(); //TIC

        LOG.info("Placing sell orders on " + Global.exchange.getName());
        WrapperTestUtils.testMultipleOrders(sellOrders, pair);

        LOG.info("Placing buy orders on " + Global.exchange.getName());
        WrapperTestUtils.testMultipleOrders(buyOrders, pair);

        LOG.info("Total Time: " + (System.nanoTime() - startTime) / 1000000 + " ms"); //TOC

    }

    private boolean configureBalances(CurrencyPair pair) {
        boolean success = true;
        ApiResponse balanceNBTResponse = Global.exchange.getTrade().getAvailableBalance(Constant.NBT);
        if (balanceNBTResponse.isPositive()) {
            Amount balance = (Amount) balanceNBTResponse.getResponseObject();
            LOG.info("NBT Balance : " + balance.toString());
            balanceNBT = balance;
        } else {
            LOG.severe(balanceNBTResponse.getError().toString());
            success = false;
        }

        ApiResponse balancePEGResponse = Global.exchange.getTrade().getAvailableBalance(pair.getPaymentCurrency());
        if (balancePEGResponse.isPositive()) {
            Amount balance = (Amount) balancePEGResponse.getResponseObject();
            LOG.info(pair.getPaymentCurrency().getCode() + " Balance : " + balance.toString());
            balancePEG = balance;
        } else {
            LOG.severe(balancePEGResponse.getError().toString());
            success = false;
        }

        return success;
    }
}
