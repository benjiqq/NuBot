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
package com.nubits.nubot.tasks;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.notifications.jhipchat.messages.Message.Color;
import com.nubits.nubot.pricefeed.PriceFeedManager;
import com.nubits.nubot.pricefeed.PriceFeedManager.LastPriceResponse;
import com.nubits.nubot.utils.TradeUtils;
import com.nubits.nubot.utils.Utils;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class StrategyCryptoTask extends TimerTask {

    private static final Logger LOG = Logger.getLogger(StrategyCryptoTask.class.getName());
    //Used to identify which order is being processed by the method renewWall
    private static final String SELL1 = "sellWall1";
    private static final String SELL2 = "sellWall2";
    private static final String BUY1 = "buyWall1";
    private static final String BUY2 = "buyWall2";
    private PriceFeedManager pfm = null;
    private double distanceTreshold;
    private LastPrice lastPrice; //here we store the price updated every xx seconds
    private boolean isFirstTime = true;
    private LastPrice currentWallBtcPrice; //here we store the price of the current walls
    private double wallchangeThreshold;
    private double sellPriceUSD, buyPriceUSD;
    private String outputPath;
    private double sellPriceBTC;
    private double buyPriceBTC;
    private String sellWallOrderID1 = "-1", sellWallOrderID2 = "-1",
            buyWallOrderID1 = "-1", buyWallOrderID2 = "-1";

    @Override
    public void run() {
        LOG.info("Executing task : StrategyCryptoTask ");
        if (pfm == null) {
            LOG.severe("CheckLastPrice task needs a PriceFeedManager to work. Please assign it before running it");
            System.exit(0);
        } else {
            LastPriceResponse lpr = pfm.getLastPrices();
            ArrayList<LastPrice> priceList = lpr.getPrices();

            LOG.fine("CheckLastPrice received values from remote feeds. ");
            LOG.fine("Main feed valid? " + lpr.isMainFeedValid() + " .\n Positive response from " + priceList.size() + "/" + pfm.getFeedList().size() + " feeds. ");
            for (int i = 0; i < priceList.size(); i++) {
                LastPrice tempPrice = priceList.get(i);
                LOG.fine(tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
                        + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode());
            }


            /*
             Possible cases here:
             *  there are backups available
             *  mainPrice is reliable and there are no backups
             no valid feeds at all
             */
            int numberOfBackups = priceList.size() - 1;

            if (numberOfBackups >= 1) {
                //At least one backup

                //Check if first price is close enough to the others
                // In a normal situation, mainPrice is the first element of the list
                if (sanityCheck(priceList, 0)) {
                    //first price is reliable compared to the others
                    this.updateLastPrice(priceList.get(0));
                } else {
                    //first price is not reliable compared to the others
                    //Check if other backup prices are close enough to each other
                    boolean foundSomeValidBackUp = false;
                    LastPrice goodPrice = null;
                    for (int l = 1; l < priceList.size(); l++) {
                        if (sanityCheck(priceList, l)) {
                            goodPrice = priceList.get(l);
                            foundSomeValidBackUp = true;
                            break;
                        }
                    }

                    if (foundSomeValidBackUp) {
                        //goodPrice is a valid price backup!
                        this.updateLastPrice(goodPrice);
                    } else {
                        //None of the source are in accord with others.

                        //Try to send a notification
                        if (Global.options != null) {
                            String title = "Problems while updating " + pfm.getPair().getOrderCurrency().getCode() + " price. Cannot find a reliable feed.";
                            String message = "Positive response from " + priceList.size() + "/" + pfm.getFeedList().size() + " feeds\n";
                            for (int i = 0; i < priceList.size(); i++) {
                                LastPrice tempPrice = priceList.get(i);
                                message += (tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
                                        + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode()) + "\n";
                            }


                            MailNotifications.send(Global.options.getMailRecipient(), title, message);
                            HipChatNotifications.sendMessage(title + message, Color.RED);
                            LOG.severe(title + message);
                        }

                    }
                }
            } else if (lpr.isMainFeedValid() && numberOfBackups < 1) {
                //mainPrice valid, no backups available
                LastPrice mainPrice = priceList.get(0);
                this.updateLastPrice(mainPrice);
                LOG.severe("New price updated. However,it could be unreliable. There were no backup feeds at the time");

            } else {
                //No valid price from feeds
                String message = "Cannot get last price of" + pfm.getPair().getOrderCurrency().getCode() + "All feeds were not valid.";
                String title = "The bot couldn't update last price";
                MailNotifications.send(Global.options.getMailRecipient(), title, message);
                HipChatNotifications.sendMessage(title + message, Color.RED);
                LOG.severe(title + message);

            }
        }
    }

    private boolean sanityCheck(ArrayList<LastPrice> priceList, int mainPriceIndex) {
        //Measure if mainPrice is close to other two values

        boolean[] ok = new boolean[priceList.size() - 1];
        double mainPrice = priceList.get(mainPriceIndex).getPrice().getQuantity();

        //Test mainPrice vs backup sources
        int f = 0;
        for (int i = 0; i < priceList.size(); i++) {
            if (i != mainPriceIndex) {
                LastPrice tempPrice = priceList.get(i);
                double temp = tempPrice.getPrice().getQuantity();
                ok[f] = closeEnough(mainPrice, temp);
                f++;
            }
        }

        int countOk = 0;
        for (int j = 0; j < ok.length; j++) {
            if (ok[j]) {
                countOk++;
            }
        }

        boolean overallOk = false; //is considered ok if the mainPrice is closeEnough to more than a half of backupPrices
        //Need to distinguish pair vs odd
        if (ok.length % 2 == 0) {
            if (countOk >= (int) ok.length / 2) {
                overallOk = true;
            }
        } else {
            if (countOk > (int) ok.length / 2) {
                overallOk = true;
            }
        }

        return overallOk;

    }
    //if temp differs from mainPrice for more than a threshold%, return false

    private boolean closeEnough(double mainPrice, double temp) {
        double distance = Math.abs(mainPrice - temp);

        double percentageDistance = Utils.round(distance * 100 / mainPrice, 4);
        if (percentageDistance > distanceTreshold) {
            return false;
        } else {
            return true;
        }
    }

    private void moveWalls() {
        LOG.warning("Moving walls process start.");
        //Should move walls here
        LOG.warning("The price moved more than" + Global.options.getCryptoPegOptions().getDistanceTreshold() + "%");

        computePricesAndLogChange();


        //Renew SELL1
        LOG.warning("Renew " + SELL1 + " . Old order id : " + sellWallOrderID1);
        renewWall(sellWallOrderID1, Constant.SELL, SELL1);

        //Renew SELL2
        LOG.warning("Renew " + SELL2 + " . Old order id : " + sellWallOrderID2);
        renewWall(sellWallOrderID2, Constant.SELL, SELL2);

        //Renew BUY1
        LOG.warning("Renew " + BUY1 + " . Old order id : " + buyWallOrderID1);
        renewWall(buyWallOrderID1, Constant.BUY, BUY1);

        //Renew BUY2
        LOG.warning("Renew " + BUY2 + " . Old order id : " + buyWallOrderID2);
        renewWall(buyWallOrderID2, Constant.BUY, BUY2);

        currentWallBtcPrice = lastPrice;
    }

    public void updateLastPrice(LastPrice lp) {
        this.lastPrice = lp;
        LOG.fine("Price Updated." + lp.getSource() + ":1 " + lp.getCurrencyMeasured().getCode() + " = "
                + "" + lp.getPrice().getQuantity() + " " + lp.getPrice().getCurrency().getCode() + "\n");

        tryMoveWalls();

    }

    private void tryMoveWalls() {

        LOG.fine("Executing tryMoveWalls");
        if (isFirstTime) {

            currentWallBtcPrice = lastPrice;

            //Compute price for walls
            computePrices();
            LOG.fine("First time wall-setup with BTCprice = " + currentWallBtcPrice.getPrice().getQuantity());

            //Cancel all existing oders

            ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders();
            if (deleteOrdersResponse.isPositive()) {
                boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
                if (deleted) {
                    LOG.warning("Clear all orders request succesfully");
                    //Wait until there are no active orders
                    boolean timedOut = false;
                    long timeout = Global.options.getCryptoPegOptions().getEmergencyTimeout() * 1000;
                    long wait = 6 * 1000;
                    long count = 0L;
                    do {
                        try {
                            Thread.sleep(wait);
                            count += wait;
                            timedOut = count > timeout;
                        } catch (InterruptedException ex) {
                            LOG.severe(ex.getMessage());
                        }
                    } while (!TradeUtils.areAllOrdersCanceled() && !timedOut);

                    if (timedOut) {
                        String message = "There was a problem cancelling all existing orders";
                        LOG.severe(message);
                        HipChatNotifications.sendMessage(message, Color.RED);
                        MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
                        //Continue anyway, maybe there is some balance to put up on order.
                    }
                    //Update the balance
                    placeInitialWalls();
                    isFirstTime = false;
                } else {
                    String message = "Could not submit request to clear orders";
                    LOG.severe(message);
                    System.exit(0);
                }

            } else {
                LOG.severe(BUY1);
                String message = "Could not submit request to clear orders";
                LOG.severe(message + " " + deleteOrdersResponse.getError() + toString());
                System.exit(0);
            }
        } else {
            //not the first time
            //check if price moved more than x% from when the wall was setup
            if (needToMoveWalls(lastPrice)) {
                moveWalls();
            } else {
                LOG.fine("No need to move walls");
            }
        }

    }

    private boolean needToMoveWalls(LastPrice last) {
        double currentWallBTCprice = currentWallBtcPrice.getPrice().getQuantity();
        double distance = Math.abs(last.getPrice().getQuantity() - currentWallBTCprice);
        double percentageDistance = Utils.round((distance * 100) / currentWallBTCprice, 4);
        LOG.fine("d=" + percentageDistance + "% (old : " + currentWallBTCprice + " new " + last.getPrice().getQuantity() + ")");

        if (percentageDistance < wallchangeThreshold) {
            return false;
        } else {
            return true;
        }
    }

    private void computePrices() {

        //Sell-side custodian sell-wall

        double btc_price = lastPrice.getPrice().getQuantity();

        //convert sell price to BTC
        sellPriceBTC = Utils.round(sellPriceUSD / btc_price, 8);
        buyPriceBTC = Utils.round(buyPriceUSD / btc_price, 8);

        if (Global.options.isDualSide()) {
            LOG.fine("Sell wall prices in BTC : \n"
                    + "Sell Price (dual-side custodians) " + sellPriceBTC + "\n"
                    + "Buy Price  " + buyPriceBTC);
        } else {
            LOG.fine("Sell wall prices in BTC : \n"
                    + "Sell Price" + sellPriceBTC);
        }

    }

    private void submitCancelOrder(Order tempOrder) {
        String order_id_delete = tempOrder.getId();
        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(order_id_delete);
        if (deleteOrderResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrderResponse.getResponseObject();

            if (deleted) {
                LOG.warning("\n\n _________________________"
                        + "Order " + order_id_delete + " deleted succesfully"
                        + "_______________________");
            } else {
                LOG.severe("Could not delete order");
            }

        } else {
            LOG.severe(deleteOrderResponse.getError().toString());
        }
    }

    private void computePricesAndLogChange() {
        if (Global.isDualSide) {
            LOG.warning("Old sell wall price = " + sellPriceBTC);
        } else {
            LOG.warning("Old sell price = " + sellPriceBTC);
            LOG.warning("Old buy price = " + buyPriceBTC);
        }

        computePrices();

        if (Global.isDualSide) {
            LOG.warning("New sell wall price = " + sellPriceBTC);
        } else {
            LOG.warning("New sell price = " + sellPriceBTC);
            LOG.warning("New buy price = " + buyPriceBTC);
        }
    }

    private void renewWall(String wallOrderID, String type, String orderIdentifier) {
        if (TradeUtils.takeDownAndWait(wallOrderID, Global.options.getCryptoPegOptions().getEmergencyTimeout() * 1000)) {
            //new_balance1 NBT is now back at disposal

            //Getavailable balance and check if there are credit available
            double availableBalance = -1;

            //Switch the currency for getbalance :
            Currency currencyBalance;
            if (type.equalsIgnoreCase(Constant.SELL)) {
                currencyBalance = Constant.NBT;
            } else {
                currencyBalance = Global.options.getPair().getPaymentCurrency();
            }
            ApiResponse availableBalancesResponse = Global.exchange.getTrade().getAvailableBalance(currencyBalance);
            if (availableBalancesResponse.isPositive()) {
                if (type.equalsIgnoreCase(Constant.SELL)) {
                    availableBalance = ((Amount) availableBalancesResponse.getResponseObject()).getQuantity();
                } else {
                    Amount cryptoBalance = (Amount) availableBalancesResponse.getResponseObject();
                    availableBalance = cryptoBalance.getConversion(lastPrice.getPrice().getQuantity()); //the available balance expressed in USD
                }

                if (availableBalance > 1) {
                    //Compute sell price
                    double orderPrice;
                    double amountOnOrder = -1;

                    if (type.equalsIgnoreCase(Constant.SELL)) {
                        amountOnOrder = availableBalance;
                        if (Global.options.isDualSide()) {
                            orderPrice = sellPriceBTC;
                        } else {
                            orderPrice = sellPriceBTC;
                        }
                    } else {
                        orderPrice = buyPriceBTC;
                        amountOnOrder = availableBalance / buyPriceBTC;
                    }



                    String orderString = type + " " + amountOnOrder + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + orderPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();

                    if (Global.options.isExecuteOrders()) {
                        //Place availableBalance on a  wall
                        LOG.warning("Strategy : " + orderIdentifier + " Submit " + type + "order : " + orderString);

                        ApiResponse orderResponse = null;
                        if (type.equalsIgnoreCase(Constant.SELL)) {
                            orderResponse = Global.exchange.getTrade().sell(Global.options.getPair(), amountOnOrder, orderPrice);
                        } else {
                            orderResponse = Global.exchange.getTrade().sell(Global.options.getPair(), amountOnOrder, orderPrice);
                        }

                        if (orderResponse.isPositive()) {
                            String responseString = (String) orderResponse.getResponseObject();
                            LOG.warning("Strategy : " + orderIdentifier + " executed correcly. = " + responseString);
                            switch (orderIdentifier) {
                                case BUY1:
                                    buyWallOrderID1 = responseString;
                                    break;
                                case BUY2:
                                    buyWallOrderID2 = responseString;
                                    break;
                                case SELL1:
                                    sellWallOrderID1 = responseString;
                                    break;
                                case SELL2:
                                    sellWallOrderID2 = responseString;
                                    break;
                            }
                        } else {
                            String errMessageTrading = "The bot encoutered an error while trying to shift walls : "
                                    + "could not place new" + orderIdentifier + " : " + orderResponse.getError().toString();
                            LOG.severe(errMessageTrading + " " + orderResponse.getError().toString());
                            HipChatNotifications.sendMessage(errMessageTrading, Color.RED);
                            MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessageTrading);
                        }
                    } else {
                        //Do not execute, just print
                        LOG.warning("Strategy : Should submit order : " + orderString);
                    }
                } else {
                    LOG.severe(currencyBalance.getCode() + " balance < 1, should be greater!");
                }
            } else {
                //Cannot update balance
                String errMessageGettingBalance = "The bot encoutered an error while trying to shift walls : "
                        + "could not get balance" + availableBalancesResponse.getError().toString();
                LOG.severe(errMessageGettingBalance);
                HipChatNotifications.sendMessage(errMessageGettingBalance, Color.RED);
                MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessageGettingBalance);
            }

        } else {
            String errMessagedeletingOrder = "could not delete order " + wallOrderID;
            LOG.severe(errMessagedeletingOrder);
            HipChatNotifications.sendMessage(errMessagedeletingOrder, Color.RED);
            MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessagedeletingOrder);
        }




    }

    public double getWallchangeThreshold() {
        return wallchangeThreshold;
    }

    public void setWallchangeThreshold(double wallchangeThreshold) {
        this.wallchangeThreshold = wallchangeThreshold;
    }

    public double getSellPriceUSD() {
        return sellPriceUSD;
    }

    public void setSellPriceUSD(double sellPriceUSD) {
        this.sellPriceUSD = sellPriceUSD;
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

    public PriceFeedManager getPfm() {
        return pfm;
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

    public LastPrice getLastPriceFromFeeds() {
        return this.lastPrice;
    }

    private boolean isDeleted(String orderId) {
        return false;
    }

    private void placeInitialWalls() {
        Amount NBTBalance = null;
        ApiResponse NBTBalancesResponse = Global.exchange.getTrade().getAvailableBalance(Constant.NBT);
        if (NBTBalancesResponse.isPositive()) {
            NBTBalance = (Amount) NBTBalancesResponse.getResponseObject();
            if (NBTBalance.getQuantity() > 1) {
                // Divide the  balance 50% 50% in balance1 and balance2
                double nbtBalance1 = Utils.round(NBTBalance.getQuantity() / 2, 4);
                double nbtBalance2 = NBTBalance.getQuantity() - nbtBalance1;

                double sellPrice;
                if (Global.options.isDualSide()) {
                    sellPrice = sellPriceBTC;
                } else {
                    sellPrice = sellPriceBTC;
                }

                String orderString1 = "sell " + nbtBalance1 + " " + Global.options.getPair().getOrderCurrency().getCode()
                        + " @ " + sellPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();
                String orderString2 = "sell " + nbtBalance2 + " " + Global.options.getPair().getOrderCurrency().getCode()
                        + " @ " + sellPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();

                if (Global.options.isExecuteOrders()) {
                    //Place nbtBalance1 on a sell wall @ 1/priceBtc + tx_fee (buy_wall_order1)


                    LOG.warning("Strategy : Submit order : " + orderString1);

                    ApiResponse sellResponse = Global.exchange.getTrade().sell(Global.options.getPair(), nbtBalance1, sellPrice);
                    if (sellResponse.isPositive()) {
                        HipChatNotifications.sendMessage("New sell wall is up on " + Global.options.getExchangeName() + " : " + orderString1, Color.YELLOW);
                        String sellResponseString = (String) sellResponse.getResponseObject();
                        LOG.warning("Strategy : Sell Response1 = " + sellResponseString);
                        sellWallOrderID1 = sellResponseString;
                    } else {
                        LOG.severe(sellResponse.getError().toString());
                    }


                    //Place nbtBalance2 on a sell wall @ 1/priceBtc + tx_fee (buy_wall_order2)

                    LOG.warning("Strategy : Submit order : " + orderString2);

                    ApiResponse sellResponse2 = Global.exchange.getTrade().sell(Global.options.getPair(), nbtBalance2, sellPrice);
                    if (sellResponse2.isPositive()) {
                        HipChatNotifications.sendMessage("New sell wall is up on " + Global.options.getExchangeName() + " : " + orderString2, Color.YELLOW);
                        String sellResponseString2 = (String) sellResponse2.getResponseObject();
                        LOG.warning("Strategy : Sell Response2 = " + sellResponseString2);
                        sellWallOrderID2 = sellResponseString2;
                    } else {
                        LOG.severe(sellResponse2.getError().toString());
                    }
                } else {
                    //Just print the order without executing it
                    LOG.warning("Should execute : " + orderString1 + "\n and " + orderString2);
                }

            } else {
                //NBT balance = 0
                LOG.fine("NBT available balance < 1, no orders to execute");
            }
        } else {
            LOG.severe(NBTBalancesResponse.getError().toString());
        }



        if (Global.options.isDualSide()) {
            //Update the crypto balance (BTC, PPC, etc)
            Amount cryptoBalance = null;
            ApiResponse CryptoBalanceResponse = Global.exchange.getTrade().getAvailableBalance(Global.options.getPair().getPaymentCurrency());
            if (CryptoBalanceResponse.isPositive()) {
                cryptoBalance = (Amount) CryptoBalanceResponse.getResponseObject();
                double cryptoBalanceinUSD = cryptoBalance.getConversion(lastPrice.getPrice().getQuantity());

                if (cryptoBalanceinUSD > 1) {
                    // Divide the  balance 50% 50% in balance1 and balance2
                    double cryptoBalance1 = Utils.round(cryptoBalance.getQuantity() / 2, 4);
                    double cryptoBalance2 = cryptoBalance.getQuantity() - cryptoBalance1;

                    double amountToBuy1 = cryptoBalance1 / buyPriceBTC;
                    double amountToBuy2 = cryptoBalance2 / buyPriceBTC;

                    String buyOrderString1 = "buy " + amountToBuy1 + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + buyPriceBTC + " " + Global.options.getPair().getPaymentCurrency().getCode();
                    String buyOrderString2 = "buy " + amountToBuy2 + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + buyPriceBTC + " " + Global.options.getPair().getPaymentCurrency().getCode();

                    if (Global.options.isExecuteOrders()) {
                        //Place cryptoBalance1 on a buy wall @ 1/priceBtc - tx_fee (buy_wall_order1)
                        LOG.warning("Strategy : Submit order : " + buyOrderString1);

                        ApiResponse buyResponse1 = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy1, buyPriceBTC);
                        if (buyResponse1.isPositive()) {
                            HipChatNotifications.sendMessage("New buy wall is up on " + Global.options.getExchangeName() + " : " + buyOrderString1, Color.YELLOW);
                            String buyResponseString1 = (String) buyResponse1.getResponseObject();
                            LOG.warning("Strategy : Buy Response1 = " + buyResponseString1);
                            buyWallOrderID1 = buyResponseString1;
                        } else {
                            LOG.severe(buyResponse1.getError().toString());
                        }


                        //Place cryptoBalance2 on a buy wall @ 1/priceBtc - tx_fee (buy_wall_order2)
                        LOG.warning("Strategy : Submit order : " + buyOrderString2);

                        ApiResponse buyResponse2 = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy2, buyPriceBTC);
                        if (buyResponse2.isPositive()) {
                            HipChatNotifications.sendMessage("New buy wall is up on " + Global.options.getExchangeName() + " : " + buyOrderString2, Color.YELLOW);
                            String buyResponseString2 = (String) buyResponse2.getResponseObject();
                            LOG.warning("Strategy : Buy Response2 = " + buyResponseString2);
                            buyWallOrderID2 = buyResponseString2;
                        } else {
                            LOG.severe(buyResponse2.getError().toString());
                        }
                    } else {
                        //Just print the order without executing it
                        LOG.warning("Should execute : " + buyOrderString1 + "\n and " + buyOrderString2);
                    }
                } else {
                    //Crypto balance insufficient
                    LOG.fine(Global.options.getPair().getPaymentCurrency().getCode() + " available balance < 1, no orders to execute");
                }
            } else {
                LOG.severe(CryptoBalanceResponse.getError().toString());
            }
        }
    }
}
