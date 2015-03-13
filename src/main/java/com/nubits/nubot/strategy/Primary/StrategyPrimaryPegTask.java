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
package com.nubits.nubot.strategy.Primary;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.*;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import com.nubits.nubot.strategy.BotUtil;
import com.nubits.nubot.trading.OrderException;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.utils.Utils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;

import java.util.ArrayList;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class StrategyPrimaryPegTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(StrategyPrimaryPegTask.class.getName());

    /**
     * minimum balance required
     */
    double treshhold_minimum_balance = 0.0001; //1

    private final int precision = 8;

    private boolean mightNeedInit = true;
    private int activeSellOrders, activeBuyOrders, totalActiveOrders;
    private boolean ordersAndBalancesOk;
    private boolean isFirstTime = true;
    private SubmitLiquidityinfoTask sendLiquidityTask;
    private boolean proceedsInBalance = false;
    private final int RESET_AFTER_CYCLES = 50;
    private final int MAX_RANDOM_WAIT_SECONDS = 5;
    private final int SHORT_WAIT_SECONDS = 5;
    private int cycles = 0;


    @Override
    public void run() {

        LOG.info("Executing task : StrategyTask. DualSide :  " + Global.options.isDualSide());

        cycles++;

        if (isFirstTime)
            init();

        if (cycles == RESET_AFTER_CYCLES) {
            reset();
        }

        try {
            adjust();
        } catch (OrderException e) {
            //handle error
        }


    }

    private void adjust() throws OrderException {

        checkBalancesAndOrders(); //Count number of active sells and buys

        if (mightNeedInit) {
            // if there are 2 active orders, do nothing
            // if there are 0 orders, place initial walls
            // if there are a number of orders different than 2, cancel all and place initial walls
            if (!(ordersAndBalancesOk)) {
                //if there orders need to be cleared
                if (totalActiveOrders > 0) {
                    try {
                        BotUtil.clearOrders();
                    } catch (OrderException e) {
                        throw e;
                    }
                }

                //Update the balance
                placeInitialWalls();

            } else {
                LOG.warn("No need to init new orders since current orders are correct");
            }
            mightNeedInit = false;
            checkBalancesAndOrders();
        }

        //Make sure there are 2 orders per side
        if (!ordersAndBalancesOk) {
            LOG.error("Detected a number of active orders not in line with strategy. Will try to aggregate soon");
            mightNeedInit = true; //if not, set firstime = true so nextTime will try to cancel and reset.
        } else {

            CurrencyPair pair = Global.options.getPair();

            ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(pair);

            if (!balancesResponse.isPositive()) {
                LOG.error(balancesResponse.getError().toString());
            }

            Balance balance = (Balance) balancesResponse.getResponseObject();

            Amount balanceNBT = balance.getNBTAvailable();

            Amount balanceFIAT = Global.frozenBalances.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());
            LOG.info("Current Balance : " + balanceNBT.getQuantity() + " " + pair.getOrderCurrency() + " "
                    + balanceFIAT.getQuantity() + " " + pair.getPaymentCurrency());

            //Execute sellSide strategy
            sellSide(balanceNBT);

            //Execute buy Side strategy
            if (Global.options.isDualSide() && proceedsInBalance) {
                buySide();
            }


        }
    }

    private void init() {
        LOG.info("Initializing strategy");
        checkBalancesAndOrders();
        isFirstTime = false;

        boolean reinitiateSuccess = reInitiateOrders(true);
        if (!reinitiateSuccess) {
            LOG.error("There was a problem while trying to reinitiating orders on first execution. Trying again on next execution");
            isFirstTime = true;
        } else {
            LOG.info("Initial walls placed");
        }
        getSendLiquidityTask().setFirstOrdersPlaced(true);
    }

    /**
     * Execute this block every RESET_AFTER_CYCLES cycles to ensure fairness with competing custodians
     */
    private void reset() {

        //Reset cycle number
        cycles = 0;

        //add a random number of cycles to avoid unlikely situation of synced custodians
        cycles += Utils.randInt(0, 5);

        //Cancel sell side orders
        boolean cancelSells = TradeUtils.takeDownOrders(Constant.SELL, Global.options.getPair());

        if (cancelSells) {
            //Update balances

            ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
            if (!balancesResponse.isPositive()) {
                //Cannot get balance
                LOG.error(balancesResponse.getError().toString());
            }

            Balance balance = (Balance) balancesResponse.getResponseObject();
            Amount balanceNBT = balance.getNBTAvailable();


            Amount balanceFIAT = Global.frozenBalances.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());
            LOG.info("Updated Balance : " + balanceNBT.getQuantity() + " NBT\n "
                    + balanceFIAT.getQuantity() + " USD");

            //Execute sellSide strategy
            //Introuce an aleatory sleep time to desync bots at the time of placing orders.
            //This will favour competition in markets with multiple custodians
            try {
                Thread.sleep(Utils.randInt(0, MAX_RANDOM_WAIT_SECONDS) * 1000);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }

            sellSide(balanceNBT);
        }

        //Execute buy Side strategy
        if (Global.options.isDualSide()) {
            //Introduce an aleatory sleep time to desync bots at the time of placing orders.
            //This will favour competition in markets with multiple custodians
            try {
                Thread.sleep(Utils.randInt(0, MAX_RANDOM_WAIT_SECONDS) * 1000);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }

            buySide();
        }
    }

    private void placeInitialWalls() {

        LOG.info("place initial walls");

        ApiResponse txFeeNTBFIATResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
        if (txFeeNTBFIATResponse.isPositive()) {

            double txFeeFIATNTB = (Double) txFeeNTBFIATResponse.getResponseObject();
            boolean buysOrdersOk = true;
            boolean sellsOrdersOk = initOrders(Constant.SELL, TradeUtils.getSellPrice(txFeeFIATNTB));

            LOG.info("txFeeFIATNTB " + txFeeFIATNTB);
            LOG.info("sellsOrdersOk " + sellsOrdersOk);

            if (Global.options.isDualSide()) {
                buysOrdersOk = initOrders(Constant.BUY, TradeUtils.getBuyPrice(txFeeFIATNTB));
            }

            LOG.info("buysOrdersOk " + buysOrdersOk);

            if (buysOrdersOk && sellsOrdersOk) {
                mightNeedInit = false;
            } else {
                mightNeedInit = true;
            }

            LOG.info("mightNeedInit " + mightNeedInit);

        } else {
            LOG.error("An error occurred while attempting to update tx fee.");
            mightNeedInit = true;
        }


    }

    private void orderLog(String orderString) {
        LOG.warn("Strategy : Submit order : " + orderString);
    }

    /**
     * NTB (Sells)
     */
    private void sellSide(Amount balanceNBT) {

        //Don't order if NBT balance < treshhold
        double nbttreshhold = 1;
        if (balanceNBT.getQuantity() < nbttreshhold) {
            //NBT balance = 0
            LOG.info("NBT balance < 1, no orders to execute");
            return;
        }

        String idToDelete = getSmallerWallID(Constant.SELL);
        if (idToDelete.equals("-1")) {
            LOG.error("Can't get smaller wall id.");
            return;
        }

        LOG.warn("Sellside : Taking down smaller order to aggregate it with new balance");

        boolean orderdelete = TradeUtils.takeDownAndWait(idToDelete, Global.options.getEmergencyTimeout() * 1000, Global.options.getPair());

        if (!orderdelete) {
            String errMessagedeletingOrder = "could not delete order " + idToDelete;
            LOG.error(errMessagedeletingOrder);
            HipChatNotifications.sendMessage(errMessagedeletingOrder, MessageColor.YELLOW);
            MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessagedeletingOrder);
            return;
        }


        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
        if (balancesResponse.isPositive()) {
            //Cannot get balance
            LOG.error(balancesResponse.getError().toString());
            return;
        }

        Balance balance = (Balance) balancesResponse.getResponseObject();

        balanceNBT = balance.getNBTAvailable();

        Amount balanceFIAT = Global.frozenBalances.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());

        LOG.info("Updated Balance : " + balanceNBT.getQuantity() + " " + balanceNBT.getCurrency().getCode() + "\n "
                + balanceFIAT.getQuantity() + " " + balanceFIAT.getCurrency().getCode());

        //Update TX fee :
        //Get the current transaction fee associated with a specific CurrencyPair
        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
        if (!txFeeNTBUSDResponse.isPositive()) {
            //Cannot update txfee
            LOG.error(txFeeNTBUSDResponse.getError().toString());
            return;
        }

        double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
        LOG.info("Updated Trasaction fee = " + txFeeUSDNTB + "%");

        //Prepare the sell order
        double sellPrice = TradeUtils.getSellPrice(txFeeUSDNTB);

        //There is a cap on the order size
        if (Global.options.getMaxSellVolume() > 0) {
            if (balanceNBT.getQuantity() > Global.options.getMaxSellVolume()) {
                //put the cap
                balanceNBT.setQuantity(Global.options.getMaxSellVolume());
            }
        }

        double amountToSell = balanceNBT.getQuantity();
        LOG.info("amount to sell " + amountToSell);
        if (Global.options.isExecuteOrders()) {
            //execute the order
            String orderString = "sell " + Utils.round(amountToSell, 2) + " " + Global.options.getPair().getOrderCurrency().getCode()
                    + " @ " + sellPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();
            orderLog(orderString);

            ApiResponse sellResponse = Global.exchange.getTrade().sell(Global.options.getPair(), amountToSell, sellPrice);
            if (sellResponse.isPositive()) {
                HipChatNotifications.sendMessage("New sell wall is up on " + Global.options.getExchangeName() + " : " + orderString, MessageColor.YELLOW);
                String sellResponseString = (String) sellResponse.getResponseObject();
                LOG.warn("Strategy : Sell Response = " + sellResponseString);
            } else {
                LOG.error(sellResponse.getError().toString());
            }
        } else {
            //Testing only : print the order without executing it
            LOG.warn("Strategy : (Should) Submit order : "
                    + "sell" + amountToSell + " " + Global.options.getPair().getOrderCurrency().getCode()
                    + " @ " + sellPrice + " " + Global.options.getPair().getPaymentCurrency().getCode());
        }
    }


    /**
     * USD (Buys)
     */
    private void buySide() {

        boolean cancel = TradeUtils.takeDownOrders(Constant.BUY, Global.options.getPair());
        if (cancel) {
            Global.frozenBalances.freezeNewFunds();
            ApiResponse txFeeNTBFIATResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
            if (txFeeNTBFIATResponse.isPositive()) {
                double txFeeFIATNTB = (Double) txFeeNTBFIATResponse.getResponseObject();
                {
                    initOrders(Constant.BUY, TradeUtils.getBuyPrice(txFeeFIATNTB));
                }
            } else {
                LOG.error("An error occurred while attempting to update tx fee.");
            }

        } else {
            LOG.error("An error occurred while attempting to cancel buy orders.");
        }

    }

    private String getSmallerWallID(String type) {
        Order smallerOrder = new Order();
        smallerOrder.setId("-1");

        /*ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());

        if (!activeOrdersResponse.isPositive()) {
            LOG.error(activeOrdersResponse.getError().toString());
            return "-1";
        }*/

        //ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

        ArrayList<Order> orderList = Global.taskManager.orderFetchTask.getCurrentOpenOrders();

        ArrayList<Order> orderListCategorized = TradeUtils.filterOrders(orderList, type);

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

        return smallerOrder.getId();
    }

    /**
     * check whether outstanding orders are according to the strategy
     */
    private void checkBalancesAndOrders() {

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());

        if (!balancesResponse.isPositive()) {
            LOG.error(balancesResponse.getError().toString());
            return;
        }

        Balance balance = (Balance) balancesResponse.getResponseObject();
        double balanceNBT = balance.getNBTAvailable().getQuantity();
        double balanceFIAT = (Global.frozenBalances.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount())).getQuantity();

        LOG.info("balance NBT " + balanceNBT);
        LOG.info("balance FIAT " + balanceFIAT);

        activeSellOrders = BotUtil.countActiveOrders(Constant.SELL);
        activeBuyOrders = BotUtil.countActiveOrders(Constant.BUY);
        totalActiveOrders = activeSellOrders + activeBuyOrders;

        LOG.info("activeSellOrders " + activeSellOrders);
        LOG.info("activeBuyOrders " + activeBuyOrders);
        LOG.info("totalActiveOrders" + totalActiveOrders);

        ordersAndBalancesOk = false;

        double treshholdNBT = 1;
        double treshholdFIAT = 1;
        int numOrdersBoth = 2;
        int numOrdersSell = 2;
        int numOrdersBuy = 2;

        if (Global.options.isDualSide()) {
            LOG.info("checking balance and orders for dualside");

            boolean bothSides = activeSellOrders == numOrdersBoth && activeBuyOrders == numOrdersBoth;
            boolean sellinplace = activeSellOrders == numOrdersSell && activeBuyOrders == 0 && balanceFIAT < treshholdFIAT;
            boolean buyinplace = activeSellOrders == 0 && activeBuyOrders == numOrdersBuy && balanceNBT < treshholdNBT;

            LOG.info("bothSides " + bothSides);
            LOG.info("sellneeded " + sellinplace);
            LOG.info("buyneeded " + buyinplace);

            ordersAndBalancesOk = bothSides || sellinplace || buyinplace;

            if (balanceFIAT > 1 && !isFirstTime) {
                LOG.warn("The " + balance.getPEGAvailableBalance().getCurrency().getCode() + " balance is not zero (" + balanceFIAT + " ). If the balance represent proceedings "
                        + "from a sale the bot will notice.  On the other hand, If you keep seying this message repeatedly over and over, you should restart the bot. ");
                proceedsInBalance = true;
            } else {
                proceedsInBalance = false;
            }

        } else {
            LOG.info("checking balance and orders for one side");

            boolean nobuyorders = activeBuyOrders == 0;
            boolean twosellorders = activeSellOrders == 2;
            boolean nonbt = balanceNBT < 0.01;
            LOG.info("nobuyorders " + nobuyorders);
            LOG.info("twosellorders " + twosellorders);
            LOG.info("nonbt " + nonbt);

            ordersAndBalancesOk = twosellorders && nobuyorders && nonbt;
        }

    }

    private boolean reInitiateOrders(boolean firstTime) {

        LOG.info("reInitiateOrders");

        if (totalActiveOrders != 0) {

            LOG.info("totalActiveOrders " + totalActiveOrders);

            ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
            if (deleteOrdersResponse.isPositive()) {
                boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
                if (deleted) {
                    LOG.warn("Clear all orders request successful");
                    if (firstTime) //update the initial balance of the secondary peg
                    {
                        Global.frozenBalances.setBalanceAlreadyThere(Global.options.getPair().getPaymentCurrency());
                    }
                    //Wait until there are no active orders
                    boolean timedOut = false;
                    long timeout = Global.options.getEmergencyTimeout() * 1000;
                    long wait = 5 * 1000;
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

    /**
     * cap a double at max value
     *
     * @param a
     * @param max
     * @return
     */
    private double cap(double a, double max) {
        if (a > max)
            return max;
        else
            return a;
    }

    private boolean initOrders(String type, double price) {
        boolean success = true;
        Amount amount = null;
        //Update the available balance
        Currency currency;

        if (type.equals(Constant.SELL)) {
            currency = Global.options.getPair().getOrderCurrency();
        } else {
            currency = Global.options.getPair().getPaymentCurrency();
        }

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalance(currency);

        if (!balancesResponse.isPositive()) {
            LOG.error(balancesResponse.getError().toString());
            return false;
        }

        double oneNBT = 1;
        if (type.equals(Constant.SELL)) {
            amount = (Amount) balancesResponse.getResponseObject();
        } else {
            //Here its time to compute the balance to put apart, if any
            amount = (Amount) balancesResponse.getResponseObject();
            amount = Global.frozenBalances.removeFrozenAmount(amount, Global.frozenBalances.getFrozenAmount());
            oneNBT = Utils.round(1 / Global.conversion, precision);
        }


        if (amount.getQuantity() < oneNBT) {
            LOG.info(type + " available balance (" + amount.getQuantity() + "," + currency + ") < " + treshhold_minimum_balance + " " + currency + ", no need to execute orders");
        } else {
            // Divide the  balance 50% 50% in balance1 and balance2

            //Update TX fee :
            //Get the current transaction fee associated with a specific CurrencyPair
            ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
            if (txFeeNTBPEGResponse.isPositive()) {
                double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
                LOG.info("Updated Trasaction fee = " + txFeePEGNTB + "%");

                double amount1 = Utils.round(amount.getQuantity() / 2, precision);

                double maxbuy = Global.options.getMaxBuyVolume();
                double maxsell = Global.options.getMaxSellVolume();

                //check the calculated amount against the set maximum sell amount set in the options.json file
                if (Global.options.getMaxSellVolume() > 0 && type.equals(Constant.SELL)) {
                    amount1 = amount1 > (Global.options.getMaxSellVolume() / 2) ? (Global.options.getMaxSellVolume() / 2) : amount1;
                }

                if (type.equals(Constant.BUY) && !Global.swappedPair) {
                    amount1 = Utils.round(amount1 / price, precision);
                    //check the calculated amount against the max buy amount option, if any.
                    if (Global.options.getMaxBuyVolume() > 0) {
                        double most = maxbuy / 2;
                        amount1 = cap(amount1, most);
                    }
                }

                double amount2 = amount.getQuantity() - amount1;

                if (Global.options.getMaxSellVolume() > 0 && type.equals(Constant.SELL)) {
                    double most = maxsell / 2;
                    amount2 = cap(amount2, most);
                }

                if ((type.equals(Constant.BUY) && !Global.swappedPair)
                        || (type.equals(Constant.SELL) && Global.swappedPair)) {
                    //hotfix
                    amount2 = Utils.round(amount2 - (oneNBT * 0.9), precision); //multiply by .9 to keep it below one NBT

                    amount2 = Utils.round(amount2 / price, precision);

                    //check the calculated amount against the max buy amount option, if any.
                    if (Global.options.getMaxBuyVolume() > 0) {
                        double most = maxbuy / 2;
                        amount2 = cap(amount2, most);
                    }

                }

                if (type.equals(Constant.BUY)) {
                    amount2 = Utils.round(amount2 / price, precision);
                }

                //Prepare the orders

                String orderString1 = type + " " + Utils.round(amount1, 2) + " " + Global.options.getPair().getOrderCurrency().getCode()
                        + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
                String orderString2 = type + " " + Utils.round(amount2, 2) + " " + Global.options.getPair().getOrderCurrency().getCode()
                        + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();

                if (Global.options.isExecuteOrders()) {
                    LOG.warn("Strategy - Submit order : " + orderString1);

                    ApiResponse order1Response;
                    if (type.equals(Constant.SELL)) {
                        order1Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount1, price);
                    } else {
                        order1Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount1, price);
                    }

                    if (order1Response.isPositive()) {
                        HipChatNotifications.sendMessage("New " + type + " wall is up on " + Global.options.getExchangeName() + " : " + orderString1, MessageColor.YELLOW);
                        String response1String = (String) order1Response.getResponseObject();
                        LOG.warn("Strategy - " + type + " Response1 = " + response1String);
                    } else {
                        LOG.error(order1Response.getError().toString());
                        success = false;
                    }

                    LOG.warn("Strategy - Submit order : " + orderString2);

                    ApiResponse order2Response;
                    if (type.equals(Constant.SELL)) {
                        order2Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount2, price);
                    } else {
                        order2Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount2, price);
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
                    //Just print the order without executing it
                    LOG.warn("Should execute : " + orderString1 + "\n and " + orderString2);
                }
            }
        }


        return true;
    }

    public SubmitLiquidityinfoTask getSendLiquidityTask() {
        return sendLiquidityTask;
    }

    public void setSendLiquidityTask(SubmitLiquidityinfoTask sendLiquidityTask) {
        this.sendLiquidityTask = sendLiquidityTask;
    }
}
