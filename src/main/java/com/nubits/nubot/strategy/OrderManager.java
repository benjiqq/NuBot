package com.nubits.nubot.strategy;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 *
 */
public class OrderManager {

    private int numActiveSellOrders, numActiveBuyOrders, numTotalActiveOrders;

    private static final Logger LOG = LoggerFactory.getLogger(OrderManager.class.getName());

    private static ArrayList<Order> orderList;

    public static void fetch(){
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static int countActiveOrders(String type) {

        LOG.trace("countActiveOrders " + type);
        //Get active orders
        int numOrders = -1;

        fetch();

        for (Order tempOrder: orderList){
            if (tempOrder.getType().equalsIgnoreCase(type)) {
                numOrders++;
            }
        }

        LOG.debug("activeorders " + type + " " + numOrders);
        return numOrders;
    }

    public static ArrayList<Order> filterOrders(ArrayList<Order> originalList, String type) {
        ArrayList<Order> toRet = new ArrayList<>();
        for(Order temp: originalList) {
            if (temp.getType().equalsIgnoreCase(type)) {
                toRet.add(temp);
            }
        }

        return toRet;
    }

    public void logActiveOrders(){
        LOG.info("buy orders: " + this.getNumActiveBuyOrders());
        LOG.info("sell orders: " + this.getNumActiveSellOrders());
    }

    public ArrayList<Order> getOrderList(){
        return orderList;
    }

    public int getNumActiveSellOrders() {
        return this.countActiveOrders(Constant.SELL);
    }

    public int getNumActiveBuyOrders() {
        return this.countActiveOrders(Constant.BUY);
    }

    public int getNumTotalActiveOrders() {
        return orderList.size();
    }

}
