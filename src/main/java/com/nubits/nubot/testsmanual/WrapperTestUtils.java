/*
 * Copyright (C) 2015 Nu Development Team
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


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class WrapperTestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperTestUtils.class.getName());

    public static void testGetAvailableBalances(CurrencyPair pair) {
        //Get all the balances  associated with the account
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(pair);
        if (balancesResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance() ");
            PairBalance balance = (PairBalance) balancesResponse.getResponseObject();

            LOG.info(balance.toString());

        } else {
            LOG.error(balancesResponse.getError().toString());
        }
    }

    public static void testGetAvailableBalance(Currency cur) {
        //Get the USD balance associated with the account
        ApiResponse balanceResponse = Global.exchange.getTrade().getAvailableBalance(cur);
        if (balanceResponse.isPositive()) {
            LOG.info("Positive response from TradeInterface.getBalance(Currency cur) ");
            Amount balance = (Amount) balanceResponse.getResponseObject();

            LOG.info(balance.toString());
        } else {
            LOG.error(balanceResponse.getError().toString());
        }
    }

    public static void testGetLastPrice(CurrencyPair pair) {
        //Get lastPrice for a given CurrencyPair
        ApiResponse lastPriceResponse = Global.exchange.getTrade().getLastPrice(pair);
        if (lastPriceResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getLastPrice(CurrencyPair pair) ");
            Ticker ticker = (Ticker) lastPriceResponse.getResponseObject();
            LOG.info("Last price : 1 " + pair.getOrderCurrency().getCode() + " = "
                    + ticker.getLast() + " " + pair.getPaymentCurrency().getCode());
            LOG.info("ask  : 1 " + pair.getOrderCurrency().getCode() + " = "
                    + ticker.getAsk() + " " + pair.getPaymentCurrency().getCode());
            LOG.info("bid  : 1 " + pair.getOrderCurrency().getCode() + " = "
                    + ticker.getBid() + " " + pair.getPaymentCurrency().getCode());

        } else {
            LOG.error(lastPriceResponse.getError().toString());
        }

    }

    public static void testSell(double amountSell, double priceSell, CurrencyPair pair) {
        //Place a sell order


        ApiResponse sellResponse = Global.exchange.getTrade().sell(pair, amountSell, priceSell);
        if (sellResponse.isPositive()) {

            LOG.info("\nPositive response  from TradeInterface.sell(...) ");
            LOG.warn("Strategy : Submit order : "
                    + "sell" + amountSell + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceSell + " " + pair.getPaymentCurrency().getCode());

            String sellResponseString = (String) sellResponse.getResponseObject();
            LOG.info("Response = " + sellResponseString);
        } else {
            LOG.error(sellResponse.getError().toString());
        }
    }

    public static void testBuy(double amountBuy, double priceBuy, CurrencyPair pair) {
        //Place a buy order

        ApiResponse buyResponse = Global.exchange.getTrade().buy(pair, amountBuy, priceBuy);
        if (buyResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.buy(...) ");
            LOG.info(": Submit order : "
                    + "buy" + amountBuy + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceBuy + " " + pair.getPaymentCurrency().getCode());
            String buyResponseString = (String) buyResponse.getResponseObject();
            LOG.info("Response = " + buyResponseString);

        } else {
            LOG.error(buyResponse.getError().toString());
        }
    }

    public static void testGetActiveOrders() {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders();
        if (activeOrdersResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getActiveOrders() ");
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

            LOG.info("Active orders : " + orderList.size());
            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                LOG.info(tempOrder.toString());
            }

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static void testGetActiveOrders(CurrencyPair pair, boolean printThem) {
        //Get active orders associated with a specific CurrencyPair
        ApiResponse activeOrdersUSDNTBResponse = Global.exchange.getTrade().getActiveOrders(pair);
        if (activeOrdersUSDNTBResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getActiveOrders(CurrencyPair pair) ");
            ArrayList<Order> orderListUSDNBT = (ArrayList<Order>) activeOrdersUSDNTBResponse.getResponseObject();

            LOG.info("Active orders : " + orderListUSDNBT.size());
            if (printThem) {
                for (int i = 0; i < orderListUSDNBT.size(); i++) {
                    Order tempOrder = orderListUSDNBT.get(i);
                    LOG.info(tempOrder.toString());
                }
            }
        } else {
            LOG.error(activeOrdersUSDNTBResponse.getError().toString());
        }
    }

    public static void testGetOrderDetail(String order_id_detail) {
        //Get the order details for a specific order_id
        ApiResponse orderDetailResponse = Global.exchange.getTrade().getOrderDetail(order_id_detail);
        if (orderDetailResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getOrderDetail(id) ");
            Order order = (Order) orderDetailResponse.getResponseObject();
            LOG.info(order.toString());
        } else {
            LOG.info(orderDetailResponse.getError().toString());
        }
    }

    public static void testCancelOrder(String order_id_delete, CurrencyPair pair) {
        //Cancel an order
        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(order_id_delete, pair);
        if (deleteOrderResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrderResponse.getResponseObject();

            if (deleted) {
                LOG.info("Order deleted succesfully");
            } else {
                LOG.info("Could not delete order");
            }

        } else {
            LOG.error(deleteOrderResponse.getError().toString());
        }
    }

    public static void testGetTxFee() {
        //Get current trascation fee
        ApiResponse txFeeResponse = Global.exchange.getTrade().getTxFee();
        if (txFeeResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getTxFee()");
            double txFee = (Double) txFeeResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFee + "%");
        } else {
            LOG.error(txFeeResponse.getError().toString());
        }
    }

    public static void testGetTxFeeWithArgs(CurrencyPair pair) {
        //Get the current transaction fee associated with a specific CurrencyPair
        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(pair);
        if (txFeeNTBUSDResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getTxFee(CurrencyPair pair)");
            double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFeeUSDNTB + "%");
        } else {
            LOG.error(txFeeNTBUSDResponse.getError().toString());
        }
    }

    public static void testIsOrderActive(String orderId) {
        //Check if orderId is active
        ApiResponse orderDetailResponse = Global.exchange.getTrade().isOrderActive(orderId);
        if (orderDetailResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.isOrderActive(id) ");
            boolean exist = (boolean) orderDetailResponse.getResponseObject();
            LOG.info("Order " + orderId + "  active? " + exist);
        } else {
            LOG.error(orderDetailResponse.getError().toString());
        }
    }

    public static void testClearAllOrders(CurrencyPair pair) {
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(pair);
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

            if (deleted) {
                LOG.info("Order clear request succesfully");
            } else {
                LOG.info("Could not submit request to clear orders");
            }

        } else {
            LOG.error(deleteOrdersResponse.getError().toString());
        }
    }

    public static void testGetLastTrades(CurrencyPair pair) {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair);
        if (activeOrdersResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getLastTrades(pair) ");
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            LOG.info("Last 24h trades : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);
                LOG.info(tempTrade.toString());
            }
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static void testGetLastTrades(CurrencyPair pair, long startTime) {
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair, startTime);
        if (activeOrdersResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getLastTrades(pair,startTime) ");
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            LOG.info("Last trades from " + startTime + " : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);
                LOG.info(tempTrade.toString());
            }
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static void testMultipleOrders(ArrayList<OrderToPlace> orders, CurrencyPair pair) {

        boolean success = TradeUtils.placeMultipleOrders(orders);
        LOG.info("Multiple orders (" + orders + ") placed. success = " + success);

    }

    public static void testPlaceAndClearAllOrders(CurrencyPair pair) {

        //clear old orders if any
        testClearAllOrders(pair);


        // place a few orders
        for (int i = 0; i <= 5; i++) {
            testSell(0.1, 0.004, pair);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }
        }

        for (int i = 0; i <= 5; i++) {
            testBuy(0.1, 0.001, pair);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }
        }


        //Wait 4 secs
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

        //try to clear orders
        testClearAllOrders(pair);
    }


    public static void configureExchange(String exchangeName) throws NuBotConfigException {

        //Create exchange object
        Global.exchange = new Exchange(exchangeName);

        //Create e ExchangeLiveData object to accomodate liveData from the Global.exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        //Create the ApiKeys object reading from option files
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());

        //Create a new TradeInterface object using the custom implementation
        TradeInterface ti = ExchangeFacade.getInterfaceByName(exchangeName, keys, Global.exchange);

        //TODO:  remove when tested
        //Assign the keys to the TradeInterface
        ti.setKeys(keys);

        //Assign the TradeInterface to the exchange
        Global.exchange.setTrade(ti);

        //Set the connection check url
        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());
    }
}
