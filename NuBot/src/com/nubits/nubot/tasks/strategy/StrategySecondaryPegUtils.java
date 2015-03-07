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

/**
 *
 *
 */

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.*;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.trading.LiquidityDistribution.LiquidityDistributionModel;
import com.nubits.nubot.trading.LiquidityDistribution.ModelParameters;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.utils.Utils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;

import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class StrategySecondaryPegUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StrategySecondaryPegUtils.class.getName());
    private StrategySecondaryPegTask strategy;
    private final int MAX_RANDOM_WAIT_SECONDS = 5;
    private final int SHORT_WAIT_SECONDS = 6;

    public StrategySecondaryPegUtils(StrategySecondaryPegTask strategy) {
        this.strategy = strategy;
    }

    public boolean reInitiateOrders(boolean firstTime) {
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
        boolean sellsOrdersOk = initOrders(Constant.SELL, strategy.getSellPricePEG());

        if (Global.options.isDualSide()) {
            buysOrdersOk = initOrders(Constant.BUY, strategy.getBuyPricePEG());
        }

        if (buysOrdersOk && sellsOrdersOk) {
            strategy.setMightNeedInit(false);
            LOG.info("Initial walls placed");
        } else {
            strategy.setMightNeedInit(true);
        }
    }

    public boolean initOrders(String type, double price) {
        if (Global.options.isDistributeLiquidity()) {
            LOG.info("Initializing orders [distributed liquidity mode]");
            return initDistributedOrders(type, price);
        } else {
            LOG.info("Initializing orders [classic mode]");
            return initWallsOnly(type, price);
        }
    }

    private boolean initDistributedOrders(String type, double price) {
        boolean success;

        //Cancel all orders
        ApiResponse clearOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
        if (clearOrdersResponse.isPositive()) {
            boolean deleted = (boolean) clearOrdersResponse.getResponseObject();
            if (deleted) {
                LOG.info("Order clear request succesfully");
            } else {
                LOG.info("Could not submit request to clear orders");
            }

        } else {
            LOG.error(clearOrdersResponse.getError().toString());
            return false;
        }

        Amount balance;
        if (type.equals(Constant.SELL)) {
            ApiResponse balanceNBTResponse = Global.exchange.getTrade().getAvailableBalance(Global.options.getPair().getOrderCurrency());
            if (balanceNBTResponse.isPositive()) {
                Amount amount = (Amount) balanceNBTResponse.getResponseObject();
                balance = amount;
            } else {
                LOG.error(balanceNBTResponse.getError().toString());
                return false;
            }
        } else { //BUY orders
            ApiResponse balancePEGResponse = Global.exchange.getTrade().getAvailableBalance(Global.options.getPair().getPaymentCurrency());
            if (balancePEGResponse.isPositive()) {
                Amount amount = (Amount) balancePEGResponse.getResponseObject();
                balance = amount;
            } else {
                LOG.error(balancePEGResponse.getError().toString());
                return false;
            }
        }

        double wallHeight = 1000; //TODO later
        ModelParameters params = LiquidityDistributionModel.getStandardParams(type, wallHeight);
        LiquidityDistributionModel ldm = new LiquidityDistributionModel(params, type);
        ArrayList<OrderToPlace> orders = ldm.getOrdersToPlace(type, balance, price, Global.options.getPair(), Global.options.getTxFee());

        success = TradeUtils.placeMultipleOrders(orders);
        LOG.info("Multiple orders (" + orders + ") placed. success = " + success);


        return success;
    }

    private boolean initWallsOnly(String type, double price) {
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
        if (balancesResponse.isPositive()) {
            double oneNBT = 1;
            if (type.equals(Constant.SELL)) {
                balance = (Amount) balancesResponse.getResponseObject();
            } else {
                //Here its time to compute the balance to put apart, if any
                balance = (Amount) balancesResponse.getResponseObject();
                balance = Global.frozenBalances.removeFrozenAmount(balance, Global.frozenBalances.getFrozenAmount());
                oneNBT = Utils.round(1 / Global.conversion, 8);
            }
            if (balance.getQuantity() > oneNBT * 2) {
                //Update TX fee :
                //Get the current transaction fee associated with a specific CurrencyPair
                ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                if (txFeeNTBPEGResponse.isPositive()) {
                    double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
                    LOG.info("Updated Trasaction fee = " + txFeePEGNTB + "%");

                    double amount1 = Utils.round(balance.getQuantity() / 2, 8);

                    if (type.equals(Constant.BUY) && !Global.swappedPair) {
                        amount1 = Utils.round(amount1 / price, 8);
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
                            amount1 = Utils.round(amount1 / Global.conversion, 8);
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
                            if (type.equals(Constant.SELL)) {
                                balance = (Amount) balancesResponse2.getResponseObject();
                            } else {
                                //Here its time to compute the balance to put apart, if any
                                balance = (Amount) balancesResponse2.getResponseObject();
                                balance = Global.frozenBalances.removeFrozenAmount(balance, Global.frozenBalances.getFrozenAmount());
                            }
                            double amount2 = balance.getQuantity();

                            if ((type.equals(Constant.BUY) && !Global.swappedPair)
                                    || (type.equals(Constant.SELL) && Global.swappedPair)) {
                                //hotfix
                                amount2 = Utils.round(amount2 - (oneNBT * 0.9), 8); //multiply by .9 to keep it below one NBT
                                amount2 = Utils.round(amount2 / price, 8);
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
                                LOG.warn("Strategy : " + type + " Response2 = " + response2String);
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
            } else {
                LOG.info(type + " available balance < 1 NBT, no need to execute orders");
            }
        } else {
            LOG.error(balancesResponse.getError().toString());
            success = false;
        }
        return success;
    }

    public int countActiveOrders(String type) {
        //Get active orders
        int toRet = 0;
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                if (tempOrder.getType().equalsIgnoreCase(type)) {
                    toRet++;
                }
            }

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
            return -1;
        }
        return toRet;
    }

    public void recount() {
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
        if (balancesResponse.isPositive()) {
            Balance balance = (Balance) balancesResponse.getResponseObject();
            double balanceNBT = balance.getNBTAvailable().getQuantity();
            double balancePEG = (Global.frozenBalances.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount())).getQuantity();

            strategy.setActiveSellOrders(countActiveOrders(Constant.SELL));
            strategy.setActiveBuyOrders(countActiveOrders(Constant.BUY));
            strategy.setTotalActiveOrders(strategy.getActiveBuyOrders() + strategy.getActiveSellOrders());

            strategy.setOrdersAndBalancesOK(false);

            double oneNBT = Utils.round(1 / Global.conversion, 8);


            int activeSellOrders = strategy.getActiveSellOrders();
            int activeBuyOrders = strategy.getActiveBuyOrders();
            if (Global.options.isDualSide()) {

                strategy.setOrdersAndBalancesOK((activeSellOrders == 2 && activeBuyOrders == 2)
                        || (activeSellOrders == 2 && activeBuyOrders == 0 && balancePEG < oneNBT)
                        || (activeSellOrders == 0 && activeBuyOrders == 2 && balanceNBT < 1));


                if (balancePEG > oneNBT && Global.options.getPair().getPaymentCurrency().isFiat() && !strategy.isFirstTime()) { //Only for EUR...CNY etc
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

            initOrders(Constant.BUY, strategy.getBuyPricePEG());

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
            /* the commented code works with more than two orders, but is not needed now
             for (int i = 0; i < orderListCategorized.size(); i++) {
             Order tempOrder = orderListCategorized.get(i);
             if (tempOrder.getType().equalsIgnoreCase(type)) {
             if (i == 0) {
             smallerOrder = tempOrder;
             } else {
             if (smallerOrder.getAmount().getQuantity() > tempOrder.getAmount().getQuantity()) {
             smallerOrder = tempOrder;
             }
             }
             }
             }
             */
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
            String[] err = {"-1", "-1"};
            return err;
        }
        return toRet;
    }

    public boolean shiftWalls() {
        boolean success = true;

        // Fix 156 -- reduce this value as excoin API get more responsive
        int WAIT_TIME_FIX_156_EXCOIN = 3000; //ms

        //Communicate to the priceMonitorTask that a wall shift is in place
        strategy.getPriceMonitorTask().setWallsBeingShifted(true);
        strategy.getSendLiquidityTask().setWallsBeingShifted(true);

        //fix prices, so that if they change during wait time, this wall shift is not affected.
        double sellPrice = strategy.getSellPricePEG();
        double buyPrice = strategy.getBuyPricePEG();

        String shiftImmediatelyOrderType;
        String waitAndShiftOrderType;
        double priceImmediatelyType;
        double priceWaitType;

        if (strategy.getPriceDirection().equals(Constant.UP)) {
            shiftImmediatelyOrderType = Constant.SELL;
            waitAndShiftOrderType = Constant.BUY;
            if (!Global.swappedPair) {
                priceImmediatelyType = sellPrice;
                priceWaitType = buyPrice;
            } else {
                priceImmediatelyType = buyPrice;
                priceWaitType = sellPrice;
            }
        } else {
            shiftImmediatelyOrderType = Constant.BUY;
            waitAndShiftOrderType = Constant.SELL;
            if (!Global.swappedPair) {
                priceImmediatelyType = buyPrice;
                priceWaitType = sellPrice;
            } else {
                priceImmediatelyType = sellPrice;
                priceWaitType = buyPrice;
            }
        }

        if ((!Global.options.isDualSide() && shiftImmediatelyOrderType.equals(Constant.SELL))
                || Global.options.isDualSide()) {
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

                    if (shiftImmediatelyOrderType.equals(Constant.BUY)
                            && !Global.options.getPair().getPaymentCurrency().isFiat()) //Do not do this for stable secondary pegs (e.g EUR)
                    {
                        // update the initial balance of the secondary peg
                        Global.frozenBalances.freezeNewFunds();
                    }

                    boolean init1;
                    if (!Global.swappedPair) {
                        init1 = initOrders(shiftImmediatelyOrderType, priceImmediatelyType);
                    } else {
                        //fix 256
                        try {
                            Thread.sleep(WAIT_TIME_FIX_156_EXCOIN);
                        } catch (InterruptedException ex) {
                            LOG.error(ex.toString());
                        }
                        init1 = initOrders(waitAndShiftOrderType, priceImmediatelyType);
                    }
                    if (!init1) {
                        success = false;
                    }

                    if (success) { //Only move the second type of order if sure that the first have been taken down
                        if ((!Global.options.isDualSide() && shiftImmediatelyOrderType.equals(Constant.BUY))
                                || Global.options.isDualSide()) {
                            if (waitAndShiftOrderType.equals(Constant.BUY)
                                    && !Global.options.getPair().getPaymentCurrency().isFiat()) //Do not do this for stable secondary pegs (e.g EUR)) // update the initial balance of the secondary peg
                            {
                                Global.frozenBalances.freezeNewFunds();
                            }

                            boolean init2;



                            if (!Global.swappedPair) {
                                init2 = initOrders(waitAndShiftOrderType, priceWaitType);
                            } else {
                                //fix 256
                                try {
                                    Thread.sleep(WAIT_TIME_FIX_156_EXCOIN);
                                } catch (InterruptedException ex) {
                                    LOG.error(ex.toString());
                                }
                                init2 = initOrders(shiftImmediatelyOrderType, priceWaitType);
                            }
                            if (!init2) {
                                success = false;
                            }
                        }
                    } else { //success false with the first part of the shift
                        if ((!Global.options.isDualSide() && shiftImmediatelyOrderType.equals(Constant.SELL)) //sellside
                                || Global.options.isDualSide()) { //dualside
                            LOG.error("NuBot has not been able to shift " + shiftImmediatelyOrderType + " orders");
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
                }
            } else {
                success = false;
                //Communicate to the priceMonitorTask that the wall shift is over
                strategy.getPriceMonitorTask().setWallsBeingShifted(false);
                strategy.getSendLiquidityTask().setWallsBeingShifted(false);
                LOG.error(deleteOrdersResponse.getError().toString());
            }
        }
        return success;
    }
}
