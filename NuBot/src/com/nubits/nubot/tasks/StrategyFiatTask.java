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
public class StrategyFiatTask extends TimerTask {

    private static final Logger LOG = Logger.getLogger(StrategyFiatTask.class.getName());
    private boolean mightNeedInit = true;
    private int activeSellOrders, activeBuyOrders, totalActiveOrders;
    private boolean ordersAndBalancesOk;

    @Override
    public void run() {
        LOG.fine("Executing task : StrategyTask. DualSide :  " + Global.options.isDualSide());

        recount(); //Count number of active sells and buys

        if (mightNeedInit) {

            // if there are 2 active orders, do nothing
            // if there are 0 orders, place initial walls
            // if there are a number of orders different than 2, cancel all and place initial walls


            if (!(ordersAndBalancesOk)) {
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
                                HipChatNotifications.sendMessage(message, Color.YELLOW);
                                MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
                                //Continue anyway, maybe there is some balance to put up on order.
                            }
                            //Update the balance
                            placeInitialWalls();
                        } else {
                            String message = "Could not submit request to clear orders";
                            LOG.severe(message);
                            System.exit(0);
                        }

                    } else {
                        LOG.severe(deleteOrdersResponse.getError().toString());
                        String message = "Could not submit request to clear orders";
                        LOG.severe(message);
                        System.exit(0);
                    }
                } else {
                    placeInitialWalls();
                }
            } else {
                LOG.warning("No need to init new orders since current orders seems correct");
            }
            mightNeedInit = false;
            recount();
        }



        //Make sure there are 2 orders per side
        if (!ordersAndBalancesOk) {
            LOG.severe("Detected a number of active orders not in line with strategy. Will try to aggregate soon");
            mightNeedInit = true; //if not, set firstime = true so nextTime will try to cancel and reset.
        } else {

            ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
            if (balancesResponse.isPositive()) {
                Balance balance = (Balance) balancesResponse.getResponseObject();
                Amount balanceNBT = balance.getNBTAvailable();

                Amount balanceFIAT = TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());
                LOG.fine("Updated Balance : " + balanceNBT.getQuantity() + " NBT\n "
                        + balanceFIAT.getQuantity() + " USD");

                //Execute sellSide strategy
                sellSide(balanceNBT);

                //Execute buy Side strategy
                if (Global.isDualSide) {
                    buySide(balanceFIAT);
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
                ApiResponse txFeeNTBFIATResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                if (txFeeNTBFIATResponse.isPositive()) {
                    double txFeeFIATNTB = (Double) txFeeNTBFIATResponse.getResponseObject();
                    LOG.fine("Updated Trasaction fee = " + txFeeFIATNTB + "%");


                    //Prepare the sell order

                    double sellPrice = TradeUtils.getSellPrice(txFeeFIATNTB);


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
                            //sellWallOrderID1 = sellResponseString;
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
            Amount fiatBalance = null;
            ApiResponse fiatBalanceResponse = Global.exchange.getTrade().getAvailableBalance(Global.options.getPair().getPaymentCurrency());
            if (fiatBalanceResponse.isPositive()) {
                fiatBalance = TradeUtils.removeFrozenAmount((Amount) fiatBalanceResponse.getResponseObject(), Global.frozenBalances.getFrozenAmount());

                if (fiatBalance.getQuantity() > 1) {
                    // Divide the  balance 50% 50% in balance1 and balance2
                    double fiatBalance1 = Utils.round(fiatBalance.getQuantity() / 2, 4);
                    double fiatBalance2 = fiatBalance.getQuantity() - fiatBalance1;

                    //Update TX fee :
                    //Get the current transaction fee associated with a specific CurrencyPair
                    ApiResponse txFeeNTBFIATResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                    if (txFeeNTBFIATResponse.isPositive()) {
                        double txFeeFIATNTB = (Double) txFeeNTBFIATResponse.getResponseObject();
                        LOG.fine("Updated Trasaction fee = " + txFeeFIATNTB + "%");

                        //Prepare the buy order
                        double buyPrice = TradeUtils.getBuyPrice(txFeeFIATNTB);

                        double amountToBuy1 = fiatBalance1 / buyPrice;
                        double amountToBuy2 = fiatBalance2 / buyPrice;

                        String buyOrderString1 = "buy " + amountToBuy1 + " " + Global.options.getPair().getOrderCurrency().getCode()
                                + " @ " + buyPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();
                        String buyOrderString2 = "buy " + amountToBuy2 + " " + Global.options.getPair().getOrderCurrency().getCode()
                                + " @ " + buyPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();

                        if (Global.options.isExecuteOrders()) {
                            //Place cryptoBalance1 on a buy wall @ 1 - tx_fee (buy_wall_order1)
                            LOG.warning("Strategy : Submit order : " + buyOrderString1);

                            ApiResponse buyResponse1 = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy1, buyPrice);
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

                            ApiResponse buyResponse2 = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy2, buyPrice);
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
                LOG.severe(fiatBalanceResponse.getError().toString());
            }
        }
    }

    private void sellSide(Amount balanceNBT) {
        //----------------------NTB (Sells)----------------------------
        //Check if NBT balance > 1
        if (balanceNBT.getQuantity() > 1) {
            String idToDelete = getSmallerWallID(Constant.SELL);
            if (!idToDelete.equals("-1")) {
                LOG.warning("Sellside : Taking down smaller order to aggregate it with new balance");

                if (TradeUtils.takeDownAndWait(idToDelete, Global.options.getEmergencyTimeout() * 1000)) {

                    //Update balanceNBT to aggregate new amount made available
                    ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
                    if (balancesResponse.isPositive()) {
                        Balance balance = (Balance) balancesResponse.getResponseObject();
                        balanceNBT = balance.getNBTAvailable();
                        Amount balanceFIAT = TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());

                        LOG.fine("Updated Balance : " + balanceNBT.getQuantity() + " " + balanceNBT.getCurrency().getCode() + "\n "
                                + balanceFIAT.getQuantity() + " " + balanceFIAT.getCurrency().getCode());

                        //Update TX fee :
                        //Get the current transaction fee associated with a specific CurrencyPair
                        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                        if (txFeeNTBUSDResponse.isPositive()) {
                            double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
                            LOG.fine("Updated Trasaction fee = " + txFeeUSDNTB + "%");

                            //Prepare the sell order
                            double sellPrice = TradeUtils.getSellPrice(txFeeUSDNTB);

                            double amountToSell = balanceNBT.getQuantity();
                            if (Global.executeOrders) {
                                //execute the order
                                String orderString = "sell " + amountToSell + " " + Global.options.getPair().getOrderCurrency().getCode()
                                        + " @ " + sellPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();
                                LOG.warning("Strategy : Submit order : " + orderString);

                                ApiResponse sellResponse = Global.exchange.getTrade().sell(Global.options.getPair(), amountToSell, sellPrice);
                                if (sellResponse.isPositive()) {
                                    HipChatNotifications.sendMessage("New sell wall is up on " + Global.options.getExchangeName() + " : " + orderString, Color.YELLOW);
                                    String sellResponseString = (String) sellResponse.getResponseObject();
                                    LOG.warning("Strategy : Sell Response = " + sellResponseString);
                                } else {
                                    LOG.severe(sellResponse.getError().toString());
                                }
                            } else {
                                //Testing only : print the order without executing it
                                LOG.warning("Strategy : (Should) Submit order : "
                                        + "sell" + amountToSell + " " + Global.options.getPair().getOrderCurrency().getCode()
                                        + " @ " + sellPrice + " " + Global.options.getPair().getPaymentCurrency().getCode());
                            }
                        } else {
                            //Cannot update txfee
                            LOG.severe(txFeeNTBUSDResponse.getError().toString());
                        }


                    } else {
                        //Cannot get balance
                        LOG.severe(balancesResponse.getError().toString());
                    }
                } else {
                    String errMessagedeletingOrder = "could not delete order " + idToDelete;
                    LOG.severe(errMessagedeletingOrder);
                    HipChatNotifications.sendMessage(errMessagedeletingOrder, Color.YELLOW);
                    MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem shifting walls", errMessagedeletingOrder);
                }
            } else {
                LOG.severe("Can't get smaller wall id.");
            }

        } else {
            //NBT balance = 0
            LOG.fine("NBT balance < 1, no orders to execute");
        }
    }

    private void buySide(Amount balanceFIAT) {
        //----------------------USD (Buys)----------------------------
        //Check if USD balance > 1
        if (balanceFIAT.getQuantity() > 1) {

            TradeUtils.tryKeepProceedingsAside(balanceFIAT);


            String idToDelete = getSmallerWallID(Constant.BUY);
            if (!idToDelete.equals("-1")) {
                LOG.warning("Buyside : Taking down smaller order to aggregate it with new balance");
                if (TradeUtils.takeDownAndWait(idToDelete, Global.options.getEmergencyTimeout() * 1000)) {

                    //Update balanceNBT to aggregate new amount made available
                    ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
                    if (balancesResponse.isPositive()) {
                        Balance balance = (Balance) balancesResponse.getResponseObject();
                        Amount balanceNBT = balance.getNBTAvailable();
                        balanceFIAT = TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount());

                        LOG.fine("Updated Balance : " + balanceNBT.getQuantity() + " NBT\n "
                                + balanceFIAT.getQuantity() + " USD");

                        //Update TX fee :
                        //Get the current transaction fee associated with a specific CurrencyPair
                        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
                        if (txFeeNTBUSDResponse.isPositive()) {
                            double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
                            LOG.fine("Updated Trasaction fee = " + txFeeUSDNTB + "%");

                            //Prepare the buy order
                            double buyPrice = TradeUtils.getBuyPrice(txFeeUSDNTB);

                            double amountToBuy = balanceFIAT.getQuantity() / buyPrice;
                            if (Global.executeOrders) {
                                //execute the order
                                String orderString = "buy " + amountToBuy + " " + Global.options.getPair().getOrderCurrency().getCode()
                                        + " @ " + buyPrice + " " + Global.options.getPair().getPaymentCurrency().getCode();
                                LOG.warning("Strategy : Submit order : " + orderString);

                                ApiResponse buyResponse = Global.exchange.getTrade().buy(Global.options.getPair(), amountToBuy, buyPrice);
                                if (buyResponse.isPositive()) {
                                    String buyResponseString = (String) buyResponse.getResponseObject();
                                    HipChatNotifications.sendMessage("New buy wall is up on " + Global.options.getExchangeName() + " : " + orderString, Color.YELLOW);
                                    LOG.warning("Strategy : Buy Response = " + buyResponseString);
                                } else {
                                    LOG.severe(buyResponse.getError().toString());
                                }
                            } else {
                                //Testing only : print the order without executing it
                                LOG.warning("Strategy : (Should) Submit order : "
                                        + "buy" + amountToBuy + " " + Global.options.getPair().getOrderCurrency().getCode()
                                        + " @ " + buyPrice + " " + Global.options.getPair().getPaymentCurrency().getCode());
                            }
                        } else {
                            //Cannot update txfee
                            LOG.severe(txFeeNTBUSDResponse.getError().toString());
                        }
                    } else {
                        //Cannot get balance
                        LOG.severe(balancesResponse.getError().toString());
                    }
                } else {
                    String errMessagedeletingOrder = "could not delete order " + idToDelete;
                    LOG.severe(errMessagedeletingOrder);
                    HipChatNotifications.sendMessage(errMessagedeletingOrder, Color.YELLOW);
                    MailNotifications.send(Global.options.getMailRecipient(), "NuBot : problem cancelling orders walls", errMessagedeletingOrder);
                }

            } else {
                LOG.severe("Can't get smaller wall id.");
            }

        } else {
            //USD balance = 0
            LOG.fine("USD balance < 1, no orders to execute");
        }
    }

    private String getSmallerWallID(String type) {
        Order smallerOrder = new Order();
        smallerOrder.setId("-1");
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();
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
        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
            return "-1";
        }

        return smallerOrder.getId();
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
            double balanceFIAT = (TradeUtils.removeFrozenAmount(balance.getPEGAvailableBalance(), Global.frozenBalances.getFrozenAmount())).getQuantity();
            activeSellOrders = countActiveOrders(Constant.SELL);
            activeBuyOrders = countActiveOrders(Constant.BUY);
            totalActiveOrders = activeSellOrders + activeBuyOrders;

            ordersAndBalancesOk = false;

            if (Global.options.isDualSide()) {
                ordersAndBalancesOk = (activeSellOrders == 2 && activeBuyOrders == 2)
                        || (activeSellOrders == 2 && activeBuyOrders == 0 && balanceFIAT < 1)
                        || (activeSellOrders == 0 && activeBuyOrders == 2 && balanceNBT < 1);

                if (balanceFIAT > 1) {
                    LOG.warning("The " + balance.getPEGAvailableBalance().getCurrency().getCode() + " balance is not zero (" + balanceFIAT + " ). If you just started the bot or if the balance represent proceedings "
                            + "from a sale the bot will notice.  On the other hand, If you keep seying this message repeatedly over and over, you should restart the bot. ");
                }
            } else {
                ordersAndBalancesOk = activeSellOrders == 2 && activeBuyOrders == 0 && balanceNBT < 0.01;
            }
        } else {
            LOG.severe(balancesResponse.getError().toString());
        }
    }
}
