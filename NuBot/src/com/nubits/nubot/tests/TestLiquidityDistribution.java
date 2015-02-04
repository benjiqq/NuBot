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
package com.nubits.nubot.tests;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OrderToPlace;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurve;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurveLin;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityDistributionModel;
import com.nubits.nubot.trading.LiquidityDistribution.ModelParameters;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import static easyjcckit.QuickPlot.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestLiquidityDistribution {

    private static final Logger LOG = Logger.getLogger(TestLiquidityDistribution.class.getName());
    private LiquidityDistributionModel ldm;
    private ModelParameters sellParams;
    private ModelParameters buyParams;
    private Amount balanceNBT;
    private Amount balancePEG;
    private CurrencyPair pair;
    private double txFee;
    double pegPrice;

    public static void main(String a[]) {
        TestLiquidityDistribution test = new TestLiquidityDistribution();
        test.init();

        test.configureTest();

        test.exec();

    }

    private void init() {
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
    }

    private void configureTest() {
        LOG.info("Configuring test");

        pair = Constant.NBT_BTC;
        //Custodian balance simulation
        Amount balanceNBT = new Amount(10000, Constant.NBT);
        Amount balancePEG = new Amount(100, Constant.BTC);

        pegPrice = 300; // value of 1 unit expressed in USD

        txFee = 0.2; // %

        //Configure sell Params
        double sellOffset = 0.05;
        double sellWallHeight = 1000;
        double sellWallWidth = 0.2;
        double sellWallDensity = 0.025;
        String sellCurveSteepness = LiquidityCurve.STEEPNESS_MID;

        //Configure buy Params
        double buyOffset = 0.05;
        double buyWallHeight = 1000;
        double buyWallWidth = 0.2;
        double buyWallDensity = 0.025;
        String buyCurveSteepness = LiquidityCurve.STEEPNESS_HIGH;

        sellParams = new ModelParameters(sellOffset, sellWallHeight, sellWallWidth, sellWallDensity, new LiquidityCurveLin(sellCurveSteepness));
        buyParams = new ModelParameters(buyOffset, buyWallHeight, buyWallWidth, buyWallDensity, new LiquidityCurveLin(buyCurveSteepness));

        String config = "Sell order book configuration : " + sellParams.toString();
        config += "Buy order book configuration : " + buyParams.toString();
        config += "Pair : " + pair.toString();
        config += "\nbalanceNBT : " + balanceNBT.toString();
        config += "\nbalancePEG : " + balancePEG.toString();
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
        drawOrderBooks(sellOrders, buyOrders);
    }

    private void printOrderBooks(ArrayList<OrderToPlace> sellOrders, ArrayList<OrderToPlace> buyOrders) {
        String sellOrdersString = printOrderBook(sellOrders, Constant.SELL);
        String buyOrdersString = printOrderBook(buyOrders, Constant.BUY);

        LOG.info(sellOrdersString + "\n" + buyOrdersString);

    }

    private String printOrderBook(ArrayList<OrderToPlace> orders, String type) {
        String toReturn = "----- " + type + " order book\n";
        for (int i = 0; i < orders.size(); i++) {
            OrderToPlace tempOrder = orders.get(i);
            toReturn += Utils.round(tempOrder.getPrice() * pegPrice, 6) + "," + tempOrder.getPrice() + "," + tempOrder.getSize() + "\n";
        }
        toReturn += "----- ";
        return toReturn;
    }

    private void drawOrderBooks(ArrayList<OrderToPlace> sellOrders, ArrayList<OrderToPlace> buyOrders) {
        double[] xSell = new double[sellOrders.size()];
        double[] ySell = new double[sellOrders.size()];
        double[] xBuy = new double[buyOrders.size()];
        double[] yBuy = new double[buyOrders.size()];


        for (int i = 0; i < sellOrders.size(); i++) {
            OrderToPlace tempOrder = sellOrders.get(i);
            xSell[i] = tempOrder.getPrice() * pegPrice;
            ySell[i] = tempOrder.getSize();

        }

        for (int i = 0; i < buyOrders.size(); i++) {
            OrderToPlace tempOrder = buyOrders.get(i);
            xBuy[i] = tempOrder.getPrice() * pegPrice;
            yBuy[i] = tempOrder.getSize();

        }

        plot(xSell, ySell); // create a plot using xaxis and yvalues
        addPlot(xBuy, yBuy); // create a second plot on top of first

    }
}
