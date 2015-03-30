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
package com.nubits.nubot.strategy.Secondary;

/**
 *
 * @author desrever <desrever at nubits.com>
 */

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.*;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.utils.Utils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class StrategySecondaryPegUtils {


    final static Logger LOG = LoggerFactory.getLogger(StrategySecondaryPegUtils.class);

    private StrategySecondaryPegTask strategy;
    private final int MAX_RANDOM_WAIT_SECONDS = 5;
    private final int SHORT_WAIT_SECONDS = 6;

    public StrategySecondaryPegUtils(StrategySecondaryPegTask strategy) {
        this.strategy = strategy;
    }

    public boolean reInitiateOrders(boolean firstTime) {

        LOG.debug("reInitiateOrders . firstTime=" + firstTime);

        //They are either 0 or need to be cancelled
        if (strategy.getTotalActiveOrders() != 0) {
            ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
            if (deleteOrdersResponse.isPositive()) {
                boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
                if (deleted) {
                    LOG.warn("Clear all orders request succesfully");
                    if (firstTime) //update the initial balance of the secondary peg
                    {
                        Global.frozenBalances.setBalanceAlreadyThere(Global.options.getPair().getPaymentCurrency());
                    }
                    //Wait until there are no active orders
                    boolean timedOut = false;
                    long timeout = Global.options.getEmergencyTimeout() * 1000;
                    long wait = SHORT_WAIT_SECONDS * 1000;
                    long count = 0L;

                    boolean areAllOrdersCanceled = false;
                    do {
                        try {

                            Thread.sleep(wait);
                            areAllOrdersCanceled = TradeUtils.tryCancelAllOrders(Global.options.getPair());
                            LOG.info("Are all orders canceled? " + areAllOrdersCanceled);
                            count += wait;
                            timedOut = count > timeout;

                        } catch (InterruptedException ex) {
                            LOG.error(ex.toString());
                        }
                    } while (!areAllOrdersCanceled && !timedOut);

                    if (timedOut) {
                        String message = "There was a problem cancelling all existing orders";
                        LOG.error(message);
                        HipChatNotifications.sendMessage(message, MessageColor.YELLOW);
                        MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
                        //Continue anyway, maybe there is some balance to put up on order.
                    }
                    //Update the balance
                    placeInitialWalls();
                } else {
                    String message = "Could not submit request to clear orders";
                    LOG.error(message);
                    return false;
                }

            } else {
                LOG.error(deleteOrdersResponse.getError().toString());
                String message = "Could not submit request to clear orders";
                LOG.error(message);
                return false;
            }
        } else {
            if (firstTime) //update the initial balance of the secondary peg
            {
                Global.frozenBalances.setBalanceAlreadyThere(Global.options.getPair().getPaymentCurrency());
            }
            placeInitialWalls();
        }
        try {
            Thread.sleep(SHORT_WAIT_SECONDS); //Give the time to new orders to be placed before counting again
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }
        return true;
    }

    public void placeInitialWalls() {

        boolean buysOrdersOk = true;
        double sellPrice = strategy.getSellPricePEG();
        LOG.debug("init sell orders. price: " + sellPrice);
        boolean sellsOrdersOk = initOrders(Constant.SELL, sellPrice);

        if (Global.options.isDualSide()) {
            double buyPrice = strategy.getBuyPricePEG();
            LOG.debug("init buy orders. price: " + buyPrice);
            buysOrdersOk = initOrders(Constant.BUY, buyPrice);
        }

        if (buysOrdersOk && sellsOrdersOk) {
            strategy.setMightNeedInit(false);
            LOG.info("Initial walls placed");
        } else {
            strategy.setMightNeedInit(true);
        }
    }

    public boolean initOrders(String type, double price) {

        LOG.debug("initOrders " + type + ", price " + price);

        boolean success = true;
        Amount balance = null;
        //Update the available balance
        Currency currency;

        if (!Global.swappedPair) {
            if (type.equals(Constant.SELL)) {
                currency = Global.options.getPair().getOrderCurrency();
            } else {
                currency = Global.options.getPair().getPaymentCurrency();
            }
        } else {
            if (type.equals(Constant.SELL)) {
                currency = Global.options.getPair().getPaymentCurrency();
            } else {
                currency = Global.options.getPair().getOrderCurrency();
            }
        }
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalance(currency);
        if (!balancesResponse.isPositive()) {
            LOG.error(balancesResponse.getError().toString());
            return false;
        }
        double oneNBT = 1;
        if (type.equals(Constant.SELL)) {
            balance = (Amount) balancesResponse.getResponseObject();
        } else {
            //Here its time to compute the balance to put apart, if any
            balance = (Amount) balancesResponse.getResponseObject();
            balance = Global.frozenBalances.removeFrozenAmount(balance, Global.frozenBalances.getFrozenAmount());
            oneNBT = Utils.round(1 / Global.conversion, Settings.DEFAULT_PRECISION);
        }

        if (balance.getQuantity() < oneNBT * 2) {
            LOG.info("no need to execute " +type + "orders : available balance < 1 NBT");
            return false;
        }

        //Update TX fee :
        //Get the current transaction fee associated with a specific CurrencyPair
        ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());

        //short hand variables
        double maxSell = Global.options.getMaxSellVolume();
        double maxBuy = Global.options.getMaxBuyVolume();

        if (txFeeNTBPEGResponse.isPositive()) {
            double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
            LOG.trace("Updated Transaction fee = " + txFeePEGNTB + "%");

            double amount1 = Utils.round(balance.getQuantity() / 2, Settings.DEFAULT_PRECISION);
            //check the calculated amount against the set maximum sell amount set in the options.json file


            if (maxSell > 0 && type.equals(Constant.SELL)) {
                if (amount1 > (maxSell / 2))
                    amount1 = (maxSell / 2);
            }


            if (type.equals(Constant.BUY) && !Global.swappedPair) {
                amount1 = Utils.round(amount1 / price, Settings.DEFAULT_PRECISION);
                //check the calculated amount against the max buy amount option, if any.
                if (maxBuy > 0) {
                    if (amount1 > (maxBuy / 2))
                        amount1 = (maxBuy / 2);
                }

            }
            //Prepare the orders

            String orderString1;
            String sideStr = type + " side order : ";

            if (!Global.swappedPair) {
                orderString1 = sideStr + " " + type + " " + Utils.round(amount1, 4) + " " + Global.options.getPair().getOrderCurrency().getCode()
                        + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();

            } else { //Swapped
                String typeStr;
                if (type.equals(Constant.SELL)) {
                    typeStr = Constant.BUY;
                    amount1 = Utils.round(amount1 / Global.conversion, Settings.DEFAULT_PRECISION);
                    if (Global.options.getMaxSellVolume() > 0) {
                        if (amount1 > maxSell)
                            amount1 = maxSell;
                    }
                } else {
                    typeStr = Constant.SELL;
                }
                orderString1 = sideStr + " " + typeStr + " " + Utils.round(amount1, 4) + " " + Global.options.getPair().getOrderCurrency().getCode()
                        + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
            }

            if (Global.options.isExecuteOrders()) {
                LOG.warn("Strategy - Submit order : " + orderString1);

                ApiResponse order1Response;
                if (type.equals(Constant.SELL)) { //Place sellSide order 1
                    if (Global.swappedPair) {
                        order1Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount1, price);
                    } else {
                        order1Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount1, price);
                    }
                } else { //Place buySide order 1
                    if (Global.swappedPair) {
                        order1Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount1, price);
                    } else {
                        order1Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount1, price);
                    }
                }
                if (order1Response.isPositive()) {
                    HipChatNotifications.sendMessage("New " + type + " wall is up on " + Global.options.getExchangeName() + " : " + orderString1, MessageColor.YELLOW);
                    String response1String = (String) order1Response.getResponseObject();
                    LOG.warn("Strategy - " + type + " Response1 = " + response1String);
                } else {
                    LOG.error(order1Response.getError().toString());
                    success = false;
                }
                //wait a while to give the time to the new amount to update

                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException ex) {
                    LOG.error(ex.toString());
                }
                //read balance again
                ApiResponse balancesResponse2 = Global.exchange.getTrade().getAvailableBalance(currency);
                if (balancesResponse2.isPositive()) {

                    balance = (Amount) balancesResponse2.getResponseObject();

                    if (type.equals(Constant.BUY)) {
                        balance = Global.frozenBalances.removeFrozenAmount(balance, Global.frozenBalances.getFrozenAmount());
                    }


                    double amount2 = balance.getQuantity();

                    //check the calculated amount against the set maximum sell amount set in the options.json file

                    if (Global.options.getMaxSellVolume() > 0 && type.equals(Constant.SELL)) {
                        amount2 = amount2 > (maxSell / 2) ? (maxSell / 2) : amount2;
                    }

                    if ((type.equals(Constant.BUY) && !Global.swappedPair)
                            || (type.equals(Constant.SELL) && Global.swappedPair)) {
                        //hotfix
                        amount2 = Utils.round(amount2 - (oneNBT * 0.9), Settings.DEFAULT_PRECISION); //multiply by .9 to keep it below one NBT

                        amount2 = Utils.round(amount2 / price, Settings.DEFAULT_PRECISION);

                        //check the calculated amount against the max buy amount option, if any.
                        if (maxBuy > 0) {
                            if (amount2 > (maxBuy / 2))
                                amount2 = (maxBuy / 2);
                        }

                    }

                    String orderString2;
                    if (!Global.swappedPair) {
                        orderString2 = sideStr + " " + type + " " + Utils.round(amount2, 4) + " " + Global.options.getPair().getOrderCurrency().getCode()
                                + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();

                    } else { //Swapped
                        String typeStr;
                        if (type.equals(Constant.SELL)) {
                            typeStr = Constant.BUY;
                        } else {
                            typeStr = Constant.SELL;
                        }
                        orderString2 = sideStr + " " + typeStr + " " + Utils.round(amount2, 4) + " " + Global.options.getPair().getOrderCurrency().getCode()
                                + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
                    }
                    //put it on order

                    LOG.warn("Strategy - Submit order : " + orderString2);
                    ApiResponse order2Response;
                    if (type.equals(Constant.SELL)) { //Place sellSide order 2
                        if (Global.swappedPair) {
                            order2Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount2, price);
                        } else {
                            order2Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount2, price);
                        }
                    } else {//Place buySide order 2
                        if (Global.swappedPair) {
                            order2Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount2, price);
                        } else {
                            order2Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount2, price);
                        }
                    }
                    if (order2Response.isPositive()) {
                        HipChatNotifications.sendMessage("New " + type + " wall is up on " + Global.options.getExchangeName() + " : " + orderString2, MessageColor.YELLOW);
                        String response2String = (String) order2Response.getResponseObject();
                        LOG.warn("Strategy - " + type + " Response2 = " + response2String);
                    } else {
                        LOG.error(order2Response.getError().toString());
                        success = false;
                    }
                } else {
                    LOG.error("Error while reading the balance the second time " + balancesResponse2.getError().toString());
                }
            } else {
                //Just print the order without executing it
                LOG.warn("Should execute orders");
            }
        }


        return success;
    }

    public int countActiveOrders(String type) {

        LOG.trace("countActiveOrders " + type);
        //Get active orders
        int numOrders = 0;
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                if (tempOrder.getType().equalsIgnoreCase(type)) {
                    numOrders++;
                }
            }

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
            return -1;
        }
        LOG.debug("activeorders " + type + " " + numOrders);
        return numOrders;
    }

    public void recount() {
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
        if (balancesResponse.isPositive()) {
            PairBalance balance = (PairBalance) balancesResponse.getResponseObject();
            double balanceNBT = balance.getNBTAvailable().getQuantity();
            double balancePEG = (Global.frozenBalances.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount())).getQuantity();

            strategy.setActiveSellOrders(countActiveOrders(Constant.SELL));
            strategy.setActiveBuyOrders(countActiveOrders(Constant.BUY));
            strategy.setTotalActiveOrders(strategy.getActiveBuyOrders() + strategy.getActiveSellOrders());

            strategy.setOrdersAndBalancesOK(false);

            double oneNBT = Utils.round(1 / Global.conversion, Settings.DEFAULT_PRECISION);


            int activeSellOrders = strategy.getActiveSellOrders();
            int activeBuyOrders = strategy.getActiveBuyOrders();
            if (Global.options.isDualSide()) {

                strategy.setOrdersAndBalancesOK((activeSellOrders == 2 && activeBuyOrders == 2)
                        || (activeSellOrders == 2 && activeBuyOrders == 0 && balancePEG < oneNBT)
                        || (activeSellOrders == 0 && activeBuyOrders == 2 && balanceNBT < 1));


                if (balancePEG > oneNBT
                        && Global.options.getPair().getPaymentCurrency().isFiat()
                        && !strategy.isFirstTime()
                        && Global.options.getMaxBuyVolume() != 0) { //Only for EUR...CNY etc
                    LOG.warn("The " + balance.getPEGAvailableBalance().getCurrency().getCode() + " balance is not zero (" + balancePEG + " ). If the balance represent proceedings "
                            + "from a sale the bot will notice.  On the other hand, If you keep seying this message repeatedly over and over, you should restart the bot. ");
                    strategy.setProceedsInBalance(true);
                } else {
                    strategy.setProceedsInBalance(false);
                }
            } else {
                if (Global.options.isAggregate()) {
                    strategy.setOrdersAndBalancesOK(activeSellOrders == 2 && activeBuyOrders == 0 && balanceNBT < 1);
                } else {
                    strategy.setOrdersAndBalancesOK(activeSellOrders == 2 && activeBuyOrders == 0); // Ignore the balance
                }
            }
        } else {
            LOG.error(balancesResponse.getError().toString());
        }
    }

    public void aggregateAndKeepProceeds() {

        LOG.info("aggregateAndKeepProceeds");

        boolean cancel = TradeUtils.takeDownOrders(Constant.BUY, Global.options.getPair());
        if (cancel) {

            //get the balance and see if it does still require an aggregation

            Global.frozenBalances.freezeNewFunds();

            //Introuce an aleatory sleep time to desync bots at the time of placing orders.
            //This will favour competition in markets with multiple custodians
            try {
                Thread.sleep(Utils.randInt(0, MAX_RANDOM_WAIT_SECONDS) * 1000);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }

            double buyPrice = strategy.getBuyPricePEG();
            LOG.info("init buy orders. price " + buyPrice);
            initOrders(Constant.BUY, buyPrice);

        } else {
            LOG.error("An error occurred while attempting to cancel buy orders.");
        }
    }

    /* Returns an array of two strings representing orders id.
     * the first element of the array is the smallest order and the second the largest */
    public String[] getSmallerWallID(String type) {
        String[] toRet = new String[2];
        Order smallerOrder = new Order();
        Order biggerOrder = new Order();
        smallerOrder.setId("-1");
        biggerOrder.setId("-1");
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();
            ArrayList<Order> orderListCategorized = TradeUtils.filterOrders(orderList, type);

            if (orderListCategorized.size() != 2) {
                LOG.error("The number of orders on the " + type + " side is not two (" + orderListCategorized.size() + ")");
                String[] err = {"-1", "-1"};
                return err;
            } else {
                Order tempOrder1 = orderListCategorized.get(0);
                Order tempOrder2 = orderListCategorized.get(1);
                smallerOrder = tempOrder1;
                biggerOrder = tempOrder2;
                if (tempOrder1.getAmount().getQuantity() > tempOrder2.getAmount().getQuantity()) {
                    smallerOrder = tempOrder2;
                    biggerOrder = tempOrder1;
                }
                toRet[0] = smallerOrder.getId();
                toRet[1] = biggerOrder.getId();

            }

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
            String[] err = {"-1", "-1"};
            return err;
        }
        return toRet;
    }

    public boolean shiftWalls() {

        LOG.debug("Executing shiftWalls()");

        boolean success = true;

        //Communicate to the priceMonitorTask that a wall shift is in place
        strategy.getPriceMonitorTask().setWallsBeingShifted(true);
        strategy.getSendLiquidityTask().setWallsBeingShifted(true);

        //fix prices, so that if they change during wait time, this wall shift is not affected.
        double sellPrice = strategy.getSellPricePEG();
        double buyPrice = strategy.getBuyPricePEG();

        //Swap prices
        if (Global.swappedPair) {
            sellPrice = strategy.getBuyPricePEG();
            buyPrice = strategy.getSellPricePEG();
        }


        LOG.info("Immediately try to cancel all orders");

        //immediately try to : cancel all active orders
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());

        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

            if (deleted) {

                if (Global.options.isMultipleCustodians()) {
                    //Introuce an aleatory sleep time to desync bots at the time of placing orders.
                    //This will favour competition in markets with multiple custodians
                    try {
                        Thread.sleep(SHORT_WAIT_SECONDS + Utils.randInt(0, MAX_RANDOM_WAIT_SECONDS) * 1000); //SHORT_WAIT_SECONDS gives the time to other bots to take down their order
                    } catch (InterruptedException ex) {
                        LOG.error(ex.toString());
                    }
                }

                //Update frozen balances
                if (!Global.options.isDualSide() //Do not do this for sell side custodians
                        && !Global.options.getPair().getPaymentCurrency().isFiat()) //Do not do this for stable secondary pegs (e.g EUR)
                {
                    // update the initial balance of the secondary peg
                    Global.frozenBalances.freezeNewFunds();
                }

                //Reset sell side orders


                boolean initSells = initOrders(Constant.SELL, sellPrice); //Force init sell orders

                if (!initSells) {
                    success = false;
                }

                if (initSells) { //Only move the buy orders if sure that the sell have been taken down
                    if(Global.options.isDualSide()) {
                        boolean initBuys;
                        initBuys = initOrders(Constant.BUY, buyPrice);
                        if (!initBuys) {
                            success = false;
                            LOG.error("NuBot has not been able to shift buy orders");
                        }
                    }
                } else { //success false with the first part of the shift
                        LOG.error("NuBot has not been able to shift sell orders");
                    }
                }

                //Here I wait until the two orders are correctly displaied. It can take some seconds
                try {
                    Thread.sleep(SHORT_WAIT_SECONDS * 1000);
                } catch (InterruptedException ex) {
                    LOG.error(ex.toString());
                }

                //Communicate to the priceMonitorTask that the wall shift is over
                strategy.getPriceMonitorTask().setWallsBeingShifted(false);
                strategy.getSendLiquidityTask().setWallsBeingShifted(false);

            } else {
                LOG.info("Could not submit request to clear orders");
                success = false;
                //Communicate to the priceMonitorTask that the wall shift is over
                strategy.getPriceMonitorTask().setWallsBeingShifted(false);
                strategy.getSendLiquidityTask().setWallsBeingShifted(false);
                LOG.error(deleteOrdersResponse.getError().toString());
            }

        return success;
    }
}