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
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.notifications.jhipchat.messages.Message;
import com.nubits.nubot.notifications.jhipchat.messages.Message.Color;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.utils.Utils;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class StrategySecondaryPegTask extends TimerTask {

    private static final Logger LOG = Logger.getLogger(StrategySecondaryPegTask.class.getName());
    private boolean mightNeedInit = true;
    private int activeSellOrders, activeBuyOrders, totalActiveOrders;
    private boolean ordersAndBalancesOK;
    private boolean needWallShift;
    private double sellPricePEG;
    private double buyPricePEG;
    private String priceDirection;  //this parameter can be either Constant.UP (when the price of the new order increased since last wall) or Constant.DOWN
    private PriceMonitorTriggerTask priceMonitorTask;
    private SendLiquidityinfoTask sendLiquidityTask;

    @Override
    public void run() {
        LOG.fine("Executing task : StrategySecondaryPegTask. DualSide :  " + Global.options.isDualSide());


        recount(); //Count number of active sells and buys

        boolean shiftSuccess = false;

        if (needWallShift) {
            String message = "Shift needed : " + Global.options.getPair().getPaymentCurrency().getCode().toUpperCase() + " "
                    + "price changed more than " + Global.options.getSecondaryPegOptions().getWallchangeTreshold() + " %";
            HipChatNotifications.sendMessage(message, Color.PURPLE);
            LOG.warning(message);

            shiftSuccess = shiftWalls();
            if (shiftSuccess) {
                mightNeedInit = false;
                needWallShift = false;
                LOG.info("Wall shift successful");
            } else {
                LOG.severe("Wall shift failed");
            }
            recount();
        }

        if (mightNeedInit) {
            boolean reset = mightNeedInit && !(ordersAndBalancesOK);
            if (reset) {
                String message = "Order reset needed";
                HipChatNotifications.sendMessage(message, Color.PURPLE);
                LOG.warning(message);
                boolean reinitiateSuccess = reInitiateOrders();
                if (reinitiateSuccess) {
                    mightNeedInit = false;
                }
            } else {
                LOG.fine("No need to init new orders since current orders seems correct");
            }
            recount();
        }

        /* this was the graceful shift. Restore after standard shifts has been properly tested
         else {
         if (needWallShift) {

         //Secondary peg price changed, need to shift walls
         boolean reinitiateSuccess = true;

         //If orders and balance are not ok, reset them
         if (!(ordersAndBalancesOK)) {
         reinitiateSuccess = reInitiateOrders(); //TODO this will cause ignoring frozen proceedings. review
         if (reinitiateSuccess) {
         mightNeedInit = false;
         }
         } else {
         //Orders and balances seems ok.

         String message = "Shift needed : " + Global.options.getPair().getPaymentCurrency().getCode().toUpperCase() + " "
         + "price changed more than " + Global.options.getSecondaryPegOptions().getWallchangeTreshold() + " %";
         HipChatNotifications.sendMessage(message, Color.PURPLE);
         LOG.warning(message);

         //First try doing it gracefully, one wall at the time.
         boolean shiftSellWallsSuccess;
         boolean shiftBuyWallsSuccess = true; //set it to true in case of sellSide custodians

         //If sell side custodian, move sell walls

         if (!Global.isDualSide) {
         shiftSellWallsSuccess = gracefullyRefreshOrders(Constant.SELL, true);
         } else {
         //If dual side :
         if (pegPriceDirection.equals(Constant.UP)) { //If peg price increased, first move buy walls
         shiftBuyWallsSuccess = gracefullyRefreshOrders(Constant.BUY, true);
         shiftSellWallsSuccess = gracefullyRefreshOrders(Constant.SELL, true);
         } else {  //If peg price decreased, first move sell walls
         shiftSellWallsSuccess = gracefullyRefreshOrders(Constant.SELL, true);
         shiftBuyWallsSuccess = gracefullyRefreshOrders(Constant.BUY, true);
         }
         }

         if (shiftSellWallsSuccess && shiftBuyWallsSuccess) {
         LOG.info("Graceful wall shift succesful");
         mightNeedInit = false;
         needWallShift = false;
         //Here I should wait until the two orders are correctly displaied. It can take some seconds
         try {
         Thread.sleep(10 * 1000); //TODO wait a dynamic interval.
         } catch (InterruptedException ex) {
         LOG.severe(ex.toString());
         }
         } else { //If doing it gracefully didn't work
         LOG.warning("Graceful wall shift failed. Trying to clear all orders");
         //Simply clear all and restart
         boolean reinitiateSuccess2 = reInitiateOrders();
         if (reinitiateSuccess2) {
         mightNeedInit = false;
         needWallShift = false;
         }
         }
         }

         }
         }
         End graceful */

        //Make sure the orders and balances are ok or try to aggregate
        if (!ordersAndBalancesOK) {
            LOG.severe("Detected a number of active orders not in line with strategy. Will try to aggregate soon");
            mightNeedInit = true;
        } else {
            if (Global.options.isAggregate()) {
                ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
                if (balancesResponse.isPositive()) {
                    Balance balance = (Balance) balancesResponse.getResponseObject();
                    Amount balanceNBT = balance.getNBTAvailable();
                    Amount balancePEG = TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());

                    LOG.fine("Updated Balance : " + balanceNBT.getQuantity() + " NBT\n "
                            + balancePEG.getQuantity() + " " + balancePEG.getCurrency());

                    //Execute sellSide strategy
                    aggregateSellSide(balanceNBT);

                    //Execute buy Side strategy
                    if (Global.isDualSide) {
                        aggregateBuySide(balancePEG);
                    }

                } else {
                    //Cannot get balance
                    LOG.severe(balancesResponse.getError().toString());
                }
            }
        }
    }

    private void placeInitialWalls() {
        boolean buysOrdersOk = true;
        boolean sellsOrdersOk = initOrders(Constant.SELL, sellPricePEG);
        if (Global.options.isDualSide()) {
            buysOrdersOk = initOrders(Constant.BUY, buyPricePEG);
        }

        if (buysOrdersOk && sellsOrdersOk) {
            mightNeedInit = false;
        } else {
            mightNeedInit = true;
        }
    }

    private void aggregateSellSide(Amount balanceNBT) {
        //----------------------NTB (Sells)----------------------------
        //Check if NBT balance > 1
        if (balanceNBT.getQuantity() > 1) {
            gracefullyRefreshOrders(Constant.SELL, false);
        } else {
            //NBT balance = 0
            LOG.fine("NBT balance < 1, no orders to execute");
        }
    }

    private void aggregateBuySide(Amount balancePEG) {
        //----------------------PEG (Buys)----------------------------
        //Check if PEG balance > 1
        double oneNBT = Utils.round(1 / Global.conversion, 8);

        if (balancePEG.getQuantity() > oneNBT) {
            //Here its time to compute the balance to put apart, if any
            TradeUtils.tryKeepProceedingsAside(balancePEG);
            gracefullyRefreshOrders(Constant.BUY, false);
        } else {
            //PEG balance = 0
            LOG.fine(balancePEG.getCurrency().getCode() + "balance < 1, no orders to execute");
        }
    }

    private boolean reInitiateOrders() {
        //They are either 0 or need to be cancelled
        if (totalActiveOrders != 0) {
            ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
            if (deleteOrdersResponse.isPositive()) {
                boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
                if (deleted) {
                    LOG.warning("Clear all orders request succesfully");
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
                            LOG.severe(ex.toString());
                        }
                    } while (!areAllOrdersCanceled && !timedOut);

                    if (timedOut) {
                        String message = "There was a problem cancelling all existing orders";
                        LOG.severe(message);
                        HipChatNotifications.sendMessage(message, Color.YELLOW);
                        MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
                        //Continue anyway, maybe there is some balance to put up on order.
                    }
                    //Update the balance
                    placeInitialWalls();
                } else {
                    String message = "Could not submit request to clear orders";
                    LOG.severe(message);
                    return false;
                }

            } else {
                LOG.severe(deleteOrdersResponse.getError().toString());
                String message = "Could not submit request to clear orders";
                LOG.severe(message);
                return false;
            }
        } else {
            placeInitialWalls();
        }
        try {
            Thread.sleep(4000); //Give the time to new orders to be placed before counting again
        } catch (InterruptedException ex) {
            LOG.severe(ex.toString());
        }
        return true;
    }

    /* Returns an array of two strings representing orders id.
     * the first element of the array is the smallest order and the second the largest */
    private String[] getSmallerWallID(String type) {
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
                LOG.severe("The number of orders on the " + type + " side is not two (" + orderListCategorized.size() + ")");
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
            LOG.severe(activeOrdersResponse.getError().toString());
            String[] err = {"-1", "-1"};
            return err;
        }
        return toRet;
    }

    private int countActiveOrders(String type) {
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
            LOG.severe(activeOrdersResponse.getError().toString());
            return -1;
        }
        return toRet;


    }

    private void recount() {
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
        if (balancesResponse.isPositive()) {
            Balance balance = (Balance) balancesResponse.getResponseObject();
            double balanceNBT = balance.getNBTAvailable().getQuantity();
            double balancePEG = (TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount())).getQuantity();

            activeSellOrders = countActiveOrders(Constant.SELL);
            activeBuyOrders = countActiveOrders(Constant.BUY);
            totalActiveOrders = activeSellOrders + activeBuyOrders;

            ordersAndBalancesOK = false;
            double oneNBT = Utils.round(1 / Global.conversion, 8);

            if (Global.options.isDualSide()) {
                if (Global.options.isAggregate()) {
                    ordersAndBalancesOK = ((activeSellOrders == 2 && activeBuyOrders == 2 && balancePEG < oneNBT && balanceNBT < 1)
                            || (activeSellOrders == 2 && activeBuyOrders == 0 && balancePEG < oneNBT)
                            || (activeSellOrders == 0 && activeBuyOrders == 2 && balanceNBT < 1));
                } else {//Ignore the balance
                    ordersAndBalancesOK = ((activeSellOrders == 2 && activeBuyOrders == 2)
                            || (activeSellOrders == 2 && activeBuyOrders == 0 && balancePEG < oneNBT)
                            || (activeSellOrders == 0 && activeBuyOrders == 2 && balanceNBT < 1));
                }


                if (balancePEG > oneNBT && Global.options.isAggregate()) {
                    LOG.warning("The " + balance.getPEGAvailableBalance().getCurrency().getCode() + " balance is not zero (" + balancePEG + " ). If this is the first executeion, ignore this message. "
                            + "If the balance represent proceedings from a sale the bot will notice. "
                            + " If you keep seying this message repeatedly over and over, you should restart the bot. ");
                }
            } else {
                if (Global.options.isAggregate()) {
                    ordersAndBalancesOK = activeSellOrders == 2 && activeBuyOrders == 0 && balanceNBT < 1;
                } else {
                    ordersAndBalancesOK = activeSellOrders == 2 && activeBuyOrders == 0; // Ignore the balance
                }
            }
        } else {
            LOG.severe(balancesResponse.getError().toString());
        }
    }

    public void notifyPriceChanged(double new_sellPricePEG, double new_buyPricePEG, double conversion, String direction) {
        LOG.warning("Strategy received a price change notification.");
        needWallShift = true;
        Global.conversion = conversion; //TODO should update this value only after its 100% that the wall has been shifted?
        sellPricePEG = new_sellPricePEG;
        buyPricePEG = new_buyPricePEG;
        this.priceDirection = direction;
    }

    //set shift to true only when a wall shift is needed. set it to false when its simply an order aggregation
    private boolean gracefullyRefreshOrders(String type, boolean shift) {
        LOG.info("executing graceful refresh. Shift = " + shift);
        boolean success = true;
        //Check if there are two orders on the side
        int numberOfOrdersActivePerSide = 0;
        if (type.equalsIgnoreCase(Constant.BUY)) {
            numberOfOrdersActivePerSide = activeBuyOrders;
        } else if (type.equalsIgnoreCase(Constant.SELL)) {
            numberOfOrdersActivePerSide = activeSellOrders;

        } else {
            LOG.severe("Wrong order type " + type + ".It can be either " + Constant.SELL + " or " + Constant.BUY);
            success = false;
        }

        if (numberOfOrdersActivePerSide == 2) {
            String[] idToDelete = getSmallerWallID(type);
            if (!idToDelete[0].equals("-1")) {
                LOG.info("Taking down first order ");
                if (TradeUtils.takeDownAndWait(idToDelete[0], Global.options.getEmergencyTimeout() * 1000, Global.options.getPair())) {
                    if (putAllBalanceOnOrder(type)) {
                        if (shift && !idToDelete[1].equals("-1")) {//if this is a wall shift and the second order has a valid id
                            //take the other order which is still up with the old price,
                            LOG.info("Taking down second order ");
                            if (TradeUtils.takeDownAndWait(idToDelete[1], Global.options.getEmergencyTimeout() * 1000, Global.options.getPair())) {
                                //try to restore at new price.
                                if (putAllBalanceOnOrder(type)) {
                                } else {
                                    success = false;
                                }
                            } else {
                                String errMessagedeletingOrder = "could not delete order " + idToDelete[1];
                                LOG.severe(errMessagedeletingOrder);
                                HipChatNotifications.sendMessage(errMessagedeletingOrder, Color.YELLOW);
                                MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessagedeletingOrder);
                                success = false;
                            }
                        }
                    } else {
                        //some error
                        success = false;
                    }

                } else {
                    String errMessagedeletingOrder = "could not delete order " + idToDelete[0];
                    LOG.severe(errMessagedeletingOrder);
                    HipChatNotifications.sendMessage(errMessagedeletingOrder, Color.YELLOW);
                    MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessagedeletingOrder);
                    success = false;
                }
            } else {
                LOG.severe("Can't get smaller wall id.");
                success = false;
            }
        } else {
            LOG.warning(" No need of graceful shift on " + type + " side since there are a number of active orders different from two ");
        }
        return success;
    }

    private boolean putAllBalanceOnOrder(String type) {
        boolean success = true;
        Amount balanceNBT;
        Amount balancePEG;

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
        if (balancesResponse.isPositive()) {
            Balance balance = (Balance) balancesResponse.getResponseObject();
            balanceNBT = balance.getNBTAvailable();
            balancePEG = TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());

            LOG.fine("Updated Balance : " + balanceNBT.getQuantity() + " " + balanceNBT.getCurrency().getCode() + "\n "
                    + balancePEG.getQuantity() + " " + balancePEG.getCurrency().getCode());

            //Update TX fee :
            //Get the current transaction fee associated with a specific CurrencyPair
            ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
            if (txFeeNTBPEGResponse.isPositive()) {
                double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
                LOG.fine("Updated Trasaction fee = " + txFeePEGNTB + "%");

                //Prepare the order
                double amount = 0;
                double price = 0;

                if (type.equalsIgnoreCase(Constant.BUY)) {
                    amount = Utils.round(balancePEG.getQuantity() / buyPricePEG, 8);
                    price = buyPricePEG;
                } else if (type.equalsIgnoreCase(Constant.SELL)) {
                    amount = balanceNBT.getQuantity();
                    price = sellPricePEG;
                } else {
                    LOG.severe("Wrong order type " + type + ".It can be either " + Constant.SELL + " or " + Constant.BUY);
                    success = false;
                }

                if (Global.executeOrders) {
                    //execute the order
                    String orderString = type + " " + amount + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
                    LOG.warning("Strategy : Submit order : " + orderString);
                    ApiResponse response;
                    if (type.equalsIgnoreCase(Constant.SELL)) {
                        response = Global.exchange.getTrade().sell(Global.options.getPair(), amount, price);
                    } else {
                        response = Global.exchange.getTrade().buy(Global.options.getPair(), amount, price);
                    }

                    if (response.isPositive()) {
                        HipChatNotifications.sendMessage("New " + type + " wall is up on " + Global.options.getExchangeName() + " : " + orderString, Color.YELLOW);
                        String responseString = (String) response.getResponseObject();
                        LOG.warning("Strategy : " + type + " Response = " + responseString);

                    } else {
                        LOG.severe(response.getError().toString());
                        success = false;
                    }
                } else {
                    //Testing only : print the order without executing it
                    LOG.warning("Strategy : (Should) Submit order : "
                            + type + " " + amount + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode());
                }
            } else {
                //Cannot update txfee
                LOG.severe(txFeeNTBPEGResponse.getError().toString());
                success = false;
            }
        } else {
            //Cannot get balance
            LOG.severe(balancesResponse.getError().toString());
            success = false;
        }
        return success;
    }

    private boolean shiftWalls() {
        boolean success = true;

        long wait_time = (1000 * (61 + 41 + 8)); // this is with priceRefresh 61, balance-interval 40  and assuming it will take 10 seconds for the other to cancel

        //Communicate to the priceMonitorTask that a wall shift is in place
        priceMonitorTask.setWallsBeingShifted(true);
        sendLiquidityTask.setWallsBeingShifted(true);

        //fix prices, so that if they change during wait time, this wall shift is not affected.
        double sellPrice = sellPricePEG;
        double buyPrice = buyPricePEG;

        String shiftImmediatelyOrderType;
        String waitAndShiftOrderType;
        double priceImmediatelyType;
        double priceWaitType;

        if (priceDirection.equals(Constant.UP)) {
            shiftImmediatelyOrderType = Constant.SELL;
            waitAndShiftOrderType = Constant.BUY;
            priceImmediatelyType = sellPrice;
            priceWaitType = buyPrice;
        } else {
            shiftImmediatelyOrderType = Constant.BUY;
            waitAndShiftOrderType = Constant.SELL;
            priceImmediatelyType = buyPrice;
            priceWaitType = sellPrice;
        }
        LOG.info("Immediately try to shift " + shiftImmediatelyOrderType + " orders");

        //immediately try to : cancel their active <shiftImmediatelyOrderType> orders
        boolean cancel1 = TradeUtils.takeDownOrders(shiftImmediatelyOrderType, Global.options.getPair());
        if (cancel1) {//re-place their <shiftImmediatelyOrderType> orders at new price
            boolean init1 = initOrders(shiftImmediatelyOrderType, priceImmediatelyType);
            if (!init1) {
                success = false;
            }
        } else {
            success = false;
        }

        if (success) { //Only move the second type of order if sure that the first have been taken down
            if (Global.options.isWaitBeforeShift()) {
                try {

                    //wait <wait_time> seconds, to avoid eating others' custodians orders (issue #11)
                    LOG.info("Wait " + Math.round(wait_time / 1000) + " seconds to make sure all the bots shif their " + shiftImmediatelyOrderType + " own orders. "
                            + "Then try to shift " + waitAndShiftOrderType + " orders.");
                    Thread.sleep(wait_time);
                } catch (InterruptedException ex) {
                    LOG.severe(ex.toString());
                    success = false;
                }
            } else {
                LOG.warning("Skipping the waiting time : wait-before-shift option have been set to false");
            }

            //Cancel active <waitAndShiftOrderType> orders
            boolean cancel2 = TradeUtils.takeDownOrders(waitAndShiftOrderType, Global.options.getPair());

            if (cancel2) {//re-place <waitAndShiftOrderType> orders at new price
                boolean init2 = initOrders(waitAndShiftOrderType, priceWaitType);
                if (!init2) {
                    success = false;
                }
            } else {
                success = false;
            }
        }

        //Here I wait until the two orders are correctly displaied. It can take some seconds
        try {
            Thread.sleep(6 * 1000); //TODO wait a dynamic interval.
        } catch (InterruptedException ex) {
            LOG.severe(ex.toString());
        }

        //Communicate to the priceMonitorTask that the wall shift is over
        priceMonitorTask.setWallsBeingShifted(false);
        sendLiquidityTask.setWallsBeingShifted(false);

        return success;
    }

    private boolean initOrders(String type, double price) {
        boolean success = true;
        Amount balance = null;
        //Update the available balance
        Currency currency;

        if (type.equals(Constant.SELL)) {
            currency = Global.options.getPair().getOrderCurrency();
        } else {
            currency = Global.options.getPair().getPaymentCurrency();
        }

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalance(currency);

        if (balancesResponse.isPositive()) {

            double oneNBT = 1;
            if (type.equals(Constant.SELL)) {
                balance = (Amount) balancesResponse.getResponseObject();
            } else {
                balance = TradeUtils.removeFrozenAmount((Amount) balancesResponse.getResponseObject(), Global.frozenBalances.getFrozenAmount());
                oneNBT = Utils.round(1 / Global.conversion, 8);
            }

            if (balance.getQuantity() > oneNBT) {
                // Divide the  balance 50% 50% in balance1 and balance2

                //Update TX fee :
                //Get the current transaction fee associated with a specific CurrencyPair
                ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                if (txFeeNTBPEGResponse.isPositive()) {
                    double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
                    LOG.fine("Updated Trasaction fee = " + txFeePEGNTB + "%");

                    double amount1 = Utils.round(balance.getQuantity() / 2, 8);
                    double amount2 = balance.getQuantity() - amount1;

                    if (type.equals(Constant.BUY)) {
                        amount1 = Utils.round(amount1 / price, 8);
                        amount2 = Utils.round(amount2 / price, 8);
                    }

                    //Prepare the orders

                    String orderString1 = type + " " + amount1 + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
                    String orderString2 = type + " " + amount2 + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();

                    if (Global.options.isExecuteOrders()) {
                        LOG.warning("Strategy - Submit order : " + orderString1);

                        ApiResponse order1Response;
                        if (type.equals(Constant.SELL)) {
                            order1Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount1, price);
                        } else {
                            order1Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount1, price);
                        }

                        if (order1Response.isPositive()) {
                            HipChatNotifications.sendMessage("New " + type + " wall is up on " + Global.options.getExchangeName() + " : " + orderString1, Message.Color.YELLOW);
                            String response1String = (String) order1Response.getResponseObject();
                            LOG.warning("Strategy - " + type + " Response1 = " + response1String);
                        } else {
                            LOG.severe(order1Response.getError().toString());
                            success = false;
                        }

                        LOG.warning("Strategy - Submit order : " + orderString2);

                        ApiResponse order2Response;
                        if (type.equals(Constant.SELL)) {
                            order2Response = Global.exchange.getTrade().sell(Global.options.getPair(), amount2, price);
                        } else {
                            order2Response = Global.exchange.getTrade().buy(Global.options.getPair(), amount2, price);
                        }


                        if (order2Response.isPositive()) {
                            HipChatNotifications.sendMessage("New " + type + " wall is up on " + Global.options.getExchangeName() + " : " + orderString2, Message.Color.YELLOW);
                            String response2String = (String) order2Response.getResponseObject();
                            LOG.warning("Strategy : " + type + " Response2 = " + response2String);
                        } else {
                            LOG.severe(order2Response.getError().toString());
                            success = false;
                        }

                    } else {
                        //Just print the order without executing it
                        LOG.warning("Should execute : " + orderString1 + "\n and " + orderString2);
                    }
                }
            } else {
                LOG.fine(type + " available balance < 1 NBT, no need to execute orders");
            }
        } else {
            LOG.severe(balancesResponse.getError().toString());
            success = false;
        }

        return success;
    }

    //Getters and setters
    public double getSellPricePEG() {
        return sellPricePEG;
    }

    public void setSellPricePEG(double sellPricePEG) {
        this.sellPricePEG = sellPricePEG;
    }

    public double getBuyPricePEG() {
        return buyPricePEG;
    }

    public void setBuyPricePEG(double buyPricePEG) {
        this.buyPricePEG = buyPricePEG;
    }

    public PriceMonitorTriggerTask getPriceMonitorTask() {
        return priceMonitorTask;
    }

    public void setPriceMonitorTask(PriceMonitorTriggerTask priceMonitorTask) {
        this.priceMonitorTask = priceMonitorTask;
    }

    public SendLiquidityinfoTask getSendLiquidityTask() {
        return sendLiquidityTask;
    }

    public void setSendLiquidityTask(SendLiquidityinfoTask sendLiquidityTask) {
        this.sendLiquidityTask = sendLiquidityTask;
    }
}
