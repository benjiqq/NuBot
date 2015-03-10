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
package com.nubits.nubot.tasks.strategy;

import com.nubits.nubot.launch.toolkit.NuPriceMonitor;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class NuPriceMonitorTask extends MonitorTask {

    private static final Logger LOG = LoggerFactory.getLogger(NuPriceMonitorTask.class.getName());

    private LastPrice lastPrice;

    private double wallchangeThreshold;

    private boolean isFirstTime = true;
    private LastPrice currentWallPEGPrice;

    private double sellPriceUSDsingleside, sellPriceUSDdoubleside, buyPriceUSD;
    private String outputPath, recipient;
    private boolean sendEmails;
    private boolean isFirstEmail = true;
    private String emailHistory = "";

    @Override
    public void run() {
        LOG.info("Executing task : CheckLastPriceTask ");

        ArrayList<LastPrice> priceList = pfm.getLastPrices().getPrices();

        LOG.info("CheckLastPrice received values from remote feeds. ");

        LOG.info("Positive response from " + priceList.size() + "/" + pfm.getFeedList().size() + " feeds");
        for (int i = 0; i < priceList.size(); i++) {
            LastPrice tempPrice = priceList.get(i);
            LOG.info(tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
                    + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode());
        }

        boolean allok = priceList.size() == pfm.getFeedList().size();
        if (allok) {
            //All feeds returned a positive value

            //Check if mainPrice is close enough to the others
            // I am assuming that mainPrice is the first element of the list
            if (sanityCheck(priceList, 0)) {
                //mainPrice is reliable compared to the others
                this.updateLastPrice(priceList.get(0));
                tryMoveWalls();
            } else {
                //mainPrice is not reliable compared to the others
                //Check if other backup prices are close enough to each other
                boolean foundSomeValiBackUp = false;
                LastPrice goodPrice = null;
                for (int l = 1; l < priceList.size(); l++) {
                    if (sanityCheck(priceList, l)) {
                        goodPrice = priceList.get(l);
                        foundSomeValiBackUp = true;
                        break;
                    }
                }

                if (foundSomeValiBackUp) {
                    //goodPrice is a valid price backup!
                    this.updateLastPrice(goodPrice);
                    tryMoveWalls();
                } else {

                    //None of the source are in accord with others.
                    //send a notification
                    notifyDeviation(priceList);

                }
            }
        }

    }

    public void setPriceFeedManager(PriceFeedManager pfm) {
        this.pfm = pfm;
    }

    public double getDistanceTreshold() {
        return distanceTreshold;
    }

    public void setDistanceTreshold(double distanceTreshold) {
        this.distanceTreshold = distanceTreshold;
    }

    public void updateLastPrice(LastPrice lp) {
        this.lastPrice = lp;
        LOG.info("Price Updated." + lp.getSource() + ":1 " + lp.getCurrencyMeasured().getCode() + " = "
                + "" + lp.getPrice().getQuantity() + " " + lp.getPrice().getCurrency().getCode() + "\n");
    }

    public LastPrice getLastPriceFromFeeds() {
        return this.lastPrice;
    }

    private void tryMoveWalls() {

        LOG.info("Executing tryMoveWalls");
        String notificationEmail = "";
        if (isFirstTime) {
            currentWallPEGPrice = lastPrice;
            //Compute price for walls
            computePrices();
            notificationEmail = "First time wall-setup with PEGprice = " + currentWallPEGPrice.getPrice().getQuantity();
            LOG.info(notificationEmail);
        } else {
            //check if price moved more than x% from when the wall was setup
            if (needToMoveWalls(lastPrice)) {
                LOG.error("We should move the walls now!");
                //Compute price for walls

                currentWallPEGPrice = lastPrice;
                computePrices();

            } else {
                LOG.info("No need to move walls");
            }

        }

        isFirstTime = false;
    }

    private boolean needToMoveWalls(LastPrice last) {
        double currentWallPEGprice = currentWallPEGPrice.getPrice().getQuantity();
        double distance = Math.abs(last.getPrice().getQuantity() - currentWallPEGprice);
        double percentageDistance = Utils.round((distance * 100) / currentWallPEGprice, 4);
        LOG.info("d=" + percentageDistance + "% (old : " + currentWallPEGprice + " new " + last.getPrice().getQuantity() + ")");

        if (percentageDistance < wallchangeThreshold) {
            return false;
        } else {
            return true;
        }
    }

    private void computePrices() {
        //Sell-side custodian sell-wall

        double peg_price = lastPrice.getPrice().getQuantity();

        //convert sell price to PEG
        int precision = 8;
        double sellPricePEG = Utils.round(sellPriceUSDsingleside / peg_price, precision);
        double sellPricePEGdual = Utils.round(sellPriceUSDdoubleside / peg_price, precision);
        double buyPricePEG = Utils.round(buyPriceUSD / peg_price, 8);

        LOG.info("Sell wall prices: \n"
                + "Sell Price (sell-side custodians) " + sellPricePEG + "\n"
                + "Sell Price (dual-side custodians) " + sellPricePEGdual + "\n"
                + "Buy Price  " + buyPricePEG);


        //------------ here for output csv
        String source = currentWallPEGPrice.getSource();
        double price = currentWallPEGPrice.getPrice().getQuantity();
        String currency = currentWallPEGPrice.getPrice().getCurrency().getCode();
        String crypto = pfm.getPair().getOrderCurrency().getCode();

        String row = new Date() + ","
                + source + ","
                + crypto + ","
                + price + ","
                + currency + ","
                + sellPricePEG + ","
                + sellPricePEGdual + ","
                + buyPricePEG + ",";

        String otherPricesAtThisTime = "";

        ArrayList<LastPrice> priceList = pfm.getLastPrices().getPrices();

        for (int i = 0; i < priceList.size(); i++) {
            LastPrice tempPrice = priceList.get(i);
            otherPricesAtThisTime += "{ feed : " + tempPrice.getSource() + " - price : " + tempPrice.getPrice().getQuantity() + "}  ";
        }
        row += otherPricesAtThisTime + "\n";

        if (sendEmails) {
            String title = "[" + pfm.getPair().toString() + "] price changed more than " + wallchangeThreshold + "%";
            if (isFirstEmail) {
                title = "[" + pfm.getPair().toString() + "] price tracking started";
            }

            String messageNow = NuPriceMonitor.HEADER + row;
            emailHistory += messageNow;


            String tldr = pfm.getPair().toString() + " price changed more than " + wallchangeThreshold + "% since last notification: "
                    + "now is " + price + " " + pfm.getPair().getPaymentCurrency().getCode().toUpperCase() + ".\n"
                    + "Here are the prices you should used in the new orders : \n"
                    + "If you are a sell-side custodian, sell at " + sellPricePEG + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + "\n"
                    + "If you area dual-side custodian, sell at " + sellPricePEGdual + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + " "
                    + "and buy at " + buyPricePEG + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + "\n"
                    + "\n#########\n"
                    + "Below you can see the history of price changes. You can copy paste to create a csv report."
                    + "For each row you should have shifted the sell/buy walls.\n\n";
            if (isFirstEmail) {
                tldr = pfm.getPair().getOrderCurrency().getCode().toUpperCase() + " price is now " + price + " " + pfm.getPair().getPaymentCurrency().getCode() + ""
                        + "(" + source + ").\n"
                        + "Here are the prices you should used in the first order : \n"
                        + "If you are a sell-side custodian, sell at " + sellPricePEG + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + "\n"
                        + "If you area dual-side custodian, sell at " + sellPricePEGdual + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + " "
                        + "and buy at " + buyPricePEG + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + ".\nDetails.csv below \n\n\n";
            }

            MailNotifications.send(recipient, title, tldr + emailHistory);
            isFirstEmail = false;


        }
        FileSystem.writeToFile(row, outputPath, true);

    }

    public double getWallchangeThreshold() {
        return wallchangeThreshold;
    }

    public void setWallchangeThreshold(double wallchangeThreshold) {
        this.wallchangeThreshold = wallchangeThreshold;
    }

    public double getSellPriceUSDsingleside() {
        return sellPriceUSDsingleside;
    }

    public void setSellPriceUSDsingleside(double sellPriceUSDsingleside) {
        this.sellPriceUSDsingleside = sellPriceUSDsingleside;
    }

    public double getSellPriceUSDdoubleside() {
        return sellPriceUSDdoubleside;
    }

    public void setSellPriceUSDdoubleside(double sellPriceUSDdoubleside) {
        this.sellPriceUSDdoubleside = sellPriceUSDdoubleside;
    }

    public double getBuyPriceUSD() {
        return buyPriceUSD;
    }

    public void setBuyPriceUSD(double buyPriceUSD) {
        this.buyPriceUSD = buyPriceUSD;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSendEmails(boolean sendEmails) {
        this.sendEmails = sendEmails;
    }
}
