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

package com.nubits.nubot.trading;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.models.OrderToPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.TreeMap;

public class TradeUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TradeUtils.class.getName());

    /**
     * cancel all outstanding orders
     *
     * @param pair
     * @return
     */
    public static boolean tryCancelAllOrders(CurrencyPair pair) {
        boolean toRet = false;
        //get all orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (!activeOrdersResponse.isPositive()) {
            LOG.error(activeOrdersResponse.getError().toString());
            return false;
        }

        ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

        if (orderList.size() == 0) {
            toRet = true;
        } else {
            LOG.info("There are still : " + orderList.size() + " active orders");
            //Retry to cancel them to fix issue #14
            ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(pair);
            if (deleteOrdersResponse.isPositive()) {
                boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

                if (deleted) {
                    LOG.info("Order clear request succesful");
                } else {
                    toRet = false;
                    LOG.info("Could not submit request to clear orders");
                }
            } else {
                toRet = false;
                LOG.error(deleteOrdersResponse.getError().toString());
            }
        }


        return toRet;
    }

    public static boolean takeDownOrders(String type, CurrencyPair pair) {
        boolean completed = true;
        //Get active orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (!activeOrdersResponse.isPositive()) {
            LOG.error(activeOrdersResponse.getError().toString());
            return false;
        }

        ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

        for (int i = 0; i < orderList.size(); i++) {
            Order tempOrder = orderList.get(i);
            if (tempOrder.getType().equalsIgnoreCase(type)) {
                boolean tempDeleted = takeDownAndWait(tempOrder.getId(), 120 * 1000, pair);
                if (!tempDeleted) {
                    completed = false;
                }
            }
        }

        return completed;
    }

    public static boolean takeDownAndWait(String orderID, long timeoutMS, CurrencyPair pair) {

        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(orderID, pair);
        if (deleteOrderResponse.isPositive()) {
            boolean delRequested = (boolean) deleteOrderResponse.getResponseObject();

            if (delRequested) {
                LOG.warn("Order " + orderID + " delete request submitted");
            } else {
                LOG.error("Could not submit request to delete order" + orderID);
            }

        } else {
            LOG.error(deleteOrderResponse.getError().toString());
        }


        //Wait until the order is deleted or timeout
        boolean timedout = false;
        boolean deleted = false;
        long wait = 6 * 1000;
        long count = 0L;
        do {
            try {
                Thread.sleep(wait);
                count += wait;
                timedout = count > timeoutMS;

                ApiResponse orderDetailResponse = Global.exchange.getTrade().isOrderActive(orderID);
                if (orderDetailResponse.isPositive()) {
                    deleted = !((boolean) orderDetailResponse.getResponseObject());
                    LOG.info("Does order " + orderID + "  still exist?" + !deleted);
                } else {
                    LOG.error(orderDetailResponse.getError().toString());
                    return false;
                }
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
                return false;
            }
        } while (!deleted && !timedout);

        if (timedout) {
            return false;
        }
        return true;
    }

    public static double getSellPrice(double txFee) {
        if (Global.options.isDualSide()) {
            return 1 + (0.01 * txFee);
        } else {
            return 1 + (0.01 * txFee) + Global.options.getPriceIncrement();
        }

    }

    public static double getBuyPrice(double txFeeUSDNTB) {
        return 1 - (0.01 * txFeeUSDNTB);
    }

    public static ArrayList<Order> filterOrders(ArrayList<Order> originalList, String type) {
        ArrayList<Order> toRet = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i++) {
            Order temp = originalList.get(i);
            if (temp.getType().equalsIgnoreCase(type)) {
                toRet.add(temp);
            }
        }

        return toRet;
    }

    /**
     * Build the query string given a set of query parameters
     *
     * @param args
     * @param encoding
     * @return
     */
    public static String buildQueryString(AbstractMap<String, String> args, String encoding) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) {
                result += '&';
            }
            try {
                result += URLEncoder.encode(hashkey, encoding) + "="
                        + URLEncoder.encode(args.get(hashkey), encoding);
            } catch (Exception ex) {
                LOG.error(ex.toString());
            }
        }
        return result;
    }

    public static String buildQueryString(TreeMap<String, String> args, String encoding) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) {
                result += '&';
            }
            try {
                result += URLEncoder.encode(hashkey, encoding) + "="
                        + URLEncoder.encode(args.get(hashkey), encoding);
            } catch (Exception ex) {
                LOG.error(ex.toString());
            }
        }
        return result;
    }

    //The two methods below have been amalgamated into the CCDEK wrapper
    //TODO - get rid of these ccedk =methods once testing has taken place
    public static int offset = 0;


    //Init the order
    public static boolean placeMultipleOrders(ArrayList<OrderToPlace> orders) {
        //Observation : it can take between 15 and 20 seconds to place 10 orders
        boolean success = true;

        LOG.info(orders.size() + "need to be placed ");

        int countSuccess = 0;
        String failureString = "";
        for (int i = 0; i < orders.size(); i++) {
            ApiResponse tempResponse = placeOrder(orders.get(i));

            if (tempResponse.isPositive()) {
                String buyResponseString = (String) tempResponse.getResponseObject();
                LOG.info("Order " + i + "/" + orders.size() + " response = " + buyResponseString);
                countSuccess++;
            } else {
                success = false;
                failureString += "Order " + i + " failed : " + tempResponse.getError().toString() + "\n";
            }

            try {
                Thread.sleep(300); //sleep to avoid getting banned
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage());
            }

        }
        if (success) {
            LOG.info(orders.size() + " orders placed succesfully");
        } else {
            LOG.warn(orders.size() - countSuccess + "/" + orders.size() + " orders failed."
                    + "\nDetails : \n" + failureString);
        }

        return success;
    }


    private static ApiResponse placeOrder(OrderToPlace order) {
        //TODO move into the trade interface when tested and ready

        LOG.info(": Submit order : "
                + order.getType() + " " + order.getSize() + " " + order.getPair().getOrderCurrency().getCode()
                + " @ " + order.getPrice() + " " + order.getPair().getPaymentCurrency().getCode());

        ApiResponse toReturn;
        if (order.getType().equalsIgnoreCase(Constant.BUY)) {
            toReturn = Global.exchange.getTrade().buy(order.getPair(), order.getSize(), order.getPrice());
        } else {
            toReturn = Global.exchange.getTrade().sell(order.getPair(), order.getSize(), order.getPrice());
        }

        return toReturn;
    }
}
