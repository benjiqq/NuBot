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
import com.nubits.nubot.models.Order;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.notifications.jhipchat.messages.Message.Color;
import com.nubits.nubot.utils.TradeUtils;
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
    private String pegPriceDirection;  //this parameter can be either Constant.UP (when the price of the peg increased since last wall) or Constant.DOWN  (peg decreased)

    @Override
    public void run() {
        LOG.fine("Executing task : StrategySecondaryPegTask. DualSide :  " + Global.options.isDualSide());

        recount(); //Count number of active sells and buys

        if(needWallShift){
                String message = "Shift needed : " + Global.options.getPair().getPaymentCurrency().getCode().toUpperCase() + " "
                            + "price changed more than " + Global.options.getSecondaryPegOptions().getWallchangeTreshold() + " %";
                HipChatNotifications.sendMessage(message, Color.PURPLE);
                LOG.warning(message);
            }
        
        if (mightNeedInit || needWallShift) {
            
            boolean reinitiateSuccess ;
            
            boolean reset = ( needWallShift )
                    || mightNeedInit && !(ordersAndBalancesOK)  ;
            if (reset) {
                reinitiateSuccess = reInitiateOrders();
                if (reinitiateSuccess) {
                    mightNeedInit = false;
                    needWallShift = false;
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
                            LOG.severe(ex.getMessage());
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

        //Make sure the orders and balances are ok
        if (!ordersAndBalancesOK) {
            LOG.severe("Detected a number of active orders not in line with strategy. Will try to aggregate soon");
            mightNeedInit = true; 
        } else {
            ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
            if (balancesResponse.isPositive()) {
                Balance balance = (Balance) balancesResponse.getResponseObject();
                Amount balanceNBT = balance.getNBTAvailable();
                Amount balancePEG = TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());

                LOG.fine("Updated Balance : " + balanceNBT.getQuantity() + " NBT\n "
                        + balancePEG.getQuantity() + " " + balancePEG.getCurrency());

                //Execute sellSide strategy
                sellSide(balanceNBT);

                //Execute buy Side strategy
                if (Global.isDualSide) {
                    buySide(balancePEG);
                }

            } else {
                //Cannot get balance
                LOG.severe(balancesResponse.getError().toString());
            }
        }

    }

    private void placeInitialWalls() {
        Amount NBTBalance = null;
        ApiResponse NBTBalancesResponse = Global.exchange.getTrade().getAvailableBalance(Global.options.getPair().getOrderCurrency());
        if (NBTBalancesResponse.isPositive()) {
            NBTBalance = (Amount) NBTBalancesResponse.getResponseObject();
            if (NBTBalance.getQuantity() > 1) {
                // Divide the  balance 50% 50% in balance1 and balance2
                double nbtBalance1 = Utils.round(NBTBalance.getQuantity() / 2, 4);
                double nbtBalance2 = NBTBalance.getQuantity() - nbtBalance1;

                //Update TX fee :
                //Get the current transaction fee associated with a specific CurrencyPair
                ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                if (txFeeNTBPEGResponse.isPositive()) {
                    double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
                    LOG.fine("Updated Trasaction fee = " + txFeePEGNTB + "%");

                    //Prepare the sell order

                    String orderString1 = "sell " + nbtBalance1 + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + sellPricePEG + " " + Global.options.getPair().getPaymentCurrency().getCode();
                    String orderString2 = "sell " + nbtBalance2 + " " + Global.options.getPair().getOrderCurrency().getCode()
                            + " @ " + sellPricePEG + " " + Global.options.getPair().getPaymentCurrency().getCode();

                    if (Global.options.isExecuteOrders()) {
                        //Place nbtBalance1 on a sell wall @ 1/priceBtc + tx_fee (buy_wall_order1)

                        LOG.warning("Strategy : Submit order : " + orderString1);

                        ApiResponse sellResponse = Global.exchange.getTrade().sell(Global.options.getPair(), nbtBalance1, sellPricePEG);
                        if (sellResponse.isPositive()) {
                            HipChatNotifications.sendMessage("New sell wall is up on " + Global.options.getExchangeName() + " : " + orderString1, Color.YELLOW);
                            String sellResponseString = (String) sellResponse.getResponseObject();
                            LOG.warning("Strategy : Sell Response1 = " + sellResponseString);
                            //sellWallOrderID1 = sellResponseString;
                        } else {
                            LOG.severe(sellResponse.getError().toString());
                        }

                        //Place nbtBalance2 on a sell wall @ 1/priceBtc + tx_fee (buy_wall_order2)
                        LOG.warning("Strategy : Submit order : " + orderString2);

                        ApiResponse sellResponse2 = Global.exchange.getTrade().sell(Global.options.getPair(), nbtBalance2, sellPricePEG);
                        if (sellResponse2.isPositive()) {
                            HipChatNotifications.sendMessage("New sell wall is up on " + Global.options.getExchangeName() + " : " + orderString2, Color.YELLOW);
                            String sellResponseString2 = (String) sellResponse2.getResponseObject();
                            LOG.warning("Strategy : Sell Response2 = " + sellResponseString2);
                            //sellWallOrderID2 = sellResponseString2;
                        } else {
                            LOG.severe(sellResponse2.getError().toString());
                        }

                    } else {
                        //Just print the order without executing it
                        LOG.warning("Should execute : " + orderString1 + "\n and " + orderString2);
                    }
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
            Amount pegBalance = null;
            ApiResponse pegBalanceResponse = Global.exchange.getTrade().getAvailableBalance(Global.options.getPair().getPaymentCurrency());
            if (pegBalanceResponse.isPositive()) {
                pegBalance = TradeUtils.removeFrozenAmount((Amount) pegBalanceResponse.getResponseObject(), Global.frozenBalances.getFrozenAmount());


                double oneNBT = Utils.round(1 / Global.conversion, 6);
                if (pegBalance.getQuantity() > oneNBT) {
                    // Divide the  balance 50% 50% in balance1 and balance2
                    double pegBalance1 = Utils.round(pegBalance.getQuantity() / 2, 4);
                    double pegBalance2 = pegBalance.getQuantity() - pegBalance1;

                    //Update TX fee :
                    //Get the current transaction fee associated with a specific CurrencyPair
                    ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                    if (txFeeNTBPEGResponse.isPositive()) {
                        double txFeePEGNTB = (Double) txFeeNTBPEGResponse.getResponseObject();
                        LOG.fine("Updated Trasaction fee = " + txFeePEGNTB + "%");

                        //Prepare the buy order

                        double amountToBuy1 = pegBalance1 / buyPricePEG;
                        double amountToBuy2 = pegBalance2 / buyPricePEG;

                        String buyOrderString1 = "buy " + amountToBuy1 + " " + Global.options.getPair().getOrderCurrency().getCode()
                                + " @ " + buyPricePEG + " " + Global.options.getPair().getPaymentCurrency().getCode();
                        String buyOrderString2 = "buy " + amountToBuy2 + " " + Global.options.getPair().getOrderCurrency().getCode()
                                + " @ " + buyPricePEG + " " + Global.options.getPair().getPaymentCurrency().getCode();

                        if (Global.options.isExecuteOrders()) {
                            //Place cryptoBalance1 on a buy wall @ 1 - tx_fee (buy_wall_order1)
                            LOG.warning("Strategy : Submit order : " + buyOrderString1);

                            ApiResponse buyResponse1 = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy1, buyPricePEG);
                            if (buyResponse1.isPositive()) {
                                HipChatNotifications.sendMessage("New buy wall is up on " + Global.options.getExchangeName() + " : " + buyOrderString1, Color.YELLOW);
                                String buyResponseString1 = (String) buyResponse1.getResponseObject();
                                LOG.warning("Strategy : Buy Response1 = " + buyResponseString1);
                                //buyWallOrderID1=buyResponseString1;
                            } else {
                                LOG.severe(buyResponse1.getError().toString());
                            }

                            //Place cryptoBalance2 on a buy wall @ 1 - tx_fee (buy_wall_order2)
                            LOG.warning("Strategy : Submit order : " + buyOrderString2);

                            ApiResponse buyResponse2 = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy2, buyPricePEG);
                            if (buyResponse2.isPositive()) {
                                HipChatNotifications.sendMessage("New buy wall is up on " + Global.options.getExchangeName() + " : " + buyOrderString2, Color.YELLOW);
                                String buyResponseString2 = (String) buyResponse2.getResponseObject();
                                LOG.warning("Strategy : Buy Response2 = " + buyResponseString2);
                                //buyWallOrderID2=buyResponseString2;
                            } else {
                                LOG.severe(buyResponse2.getError().toString());
                            }
                        } else {
                            //Just print the order without executing it
                            LOG.warning("Should execute : " + buyOrderString1 + "\n and " + buyOrderString2);
                        }
                    }
                } else {
                    //Crypto balance insufficient
                    LOG.fine(Global.options.getPair().getPaymentCurrency().getCode() + " available balance < 1, no orders to execute");
                }
            } else {
                LOG.severe(pegBalanceResponse.getError().toString());
            }
        }
    }

    private void sellSide(Amount balanceNBT) {
        //----------------------NTB (Sells)----------------------------
        //Check if NBT balance > 1
        if (balanceNBT.getQuantity() > 1) {
            if(Global.options.isAggregate())
            {
                gracefullyRefreshOrders(Constant.SELL, false);
            }
        } else {
            //NBT balance = 0
            LOG.fine("NBT balance < 1, no orders to execute");
        }
    }

    private void buySide(Amount balancePEG) {
        //----------------------PEG (Buys)----------------------------
        //Check if PEG balance > 1
        double oneNBT = Utils.round(1 / Global.conversion, 6);

        if (balancePEG.getQuantity() > oneNBT) {
            //Here its time to compute the balance to put apart, if any
            if(Global.options.isAggregate())
            {
                TradeUtils.tryKeepProceedingsAside(balancePEG); 
                gracefullyRefreshOrders(Constant.BUY, false);
            }
        } else {
            //PEG balance = 0
            LOG.fine(balancePEG.getCurrency().getCode() + "balance < 1, no orders to execute");
        }
    }

    private boolean reInitiateOrders() {
        //They are either 0 or need to be cancelled
        if (totalActiveOrders != 0) {
            ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders();
            if (deleteOrdersResponse.isPositive()) {
                boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
                if (deleted) {
                    LOG.warning("Clear all orders request succesfully");
                    //Wait until there are no active orders
                    boolean timedOut = false;
                    long timeout = Global.options.getEmergencyTimeout() * 1000;
                    long wait = 5 * 1000;
                    long count = 0L;
                    
                    boolean areAllOrdersCanceled=false;
                    do {
                        try {
                            
                            Thread.sleep(wait);
                            areAllOrdersCanceled = TradeUtils.areAllOrdersCanceled();
                            LOG.info("Are all orders canceled? " + areAllOrdersCanceled);
                            count += wait;
                            timedOut = count > timeout;

                        } catch (InterruptedException ex) {
                            LOG.severe(ex.getMessage());
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
            LOG.severe(ex.getMessage());
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
            double oneNBT = Utils.round(1 / Global.conversion, 6);

            if (Global.options.isDualSide()) {
                if (Global.options.isAggregate())
                {
                        ordersAndBalancesOK = ((activeSellOrders == 2 && activeBuyOrders == 2 && balancePEG < oneNBT  && balanceNBT < 1 )
                                || (activeSellOrders == 2 && activeBuyOrders == 0 && balancePEG < oneNBT)
                                || (activeSellOrders == 0 && activeBuyOrders == 2 && balanceNBT < 1));
                }
                else{//Ignore the balance
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
                if (Global.options.isAggregate())
                {
                     ordersAndBalancesOK = activeSellOrders == 2  && activeBuyOrders == 0  && balanceNBT < 1;
                }
                else{
                     ordersAndBalancesOK = activeSellOrders == 2  && activeBuyOrders == 0 ; // Ignore the balance
                }
            }
        } else {
            LOG.severe(balancesResponse.getError().toString());
        }

        mightNeedInit = !ordersAndBalancesOK;
    }

    public void notifyPriceChanged(double new_sellPricePEG, double new_buyPricePEG, double conversion, String direction) {
        LOG.warning("Strategy received a price change notification.");
        needWallShift = true;
        Global.conversion = conversion; //TODO should update this value only after its 100% that the wall has been shifted? 
        sellPricePEG = new_sellPricePEG;
        buyPricePEG = new_buyPricePEG;
        this.pegPriceDirection = direction;
    }

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
                if (TradeUtils.takeDownAndWait(idToDelete[0], Global.options.getEmergencyTimeout() * 1000)) {
                    if (putAllBalanceOnOrder(type)) {
                        if (shift && !idToDelete[1].equals("-1")) {//if this is a wall shift and the second order has a valid id
                            //take the other order which is still up with the old price,
                            LOG.info("Taking down second order ");
                            if (TradeUtils.takeDownAndWait(idToDelete[1], Global.options.getEmergencyTimeout() * 1000)) {
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
                    amount = Utils.round(balancePEG.getQuantity() / buyPricePEG, 6);
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
}
