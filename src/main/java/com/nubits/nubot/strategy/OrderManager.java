package com.nubits.nubot.strategy;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.trading.OrderException;
import com.nubits.nubot.trading.TradeUtils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * OrderManager channels order queries to exchanges and stores the results
 * gets triggered from tasks
 */
public class OrderManager {

    private static final Logger LOG = LoggerFactory.getLogger(OrderManager.class.getName());

    private static ArrayList<Order> orderList;

    /**
     * clear all orders
     *
     * @throws com.nubits.nubot.trading.OrderException
     */
    public static void clearOrders() throws OrderException {
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
        if (!deleteOrdersResponse.isPositive()) {
            LOG.error(deleteOrdersResponse.getError().toString());
            String message = "Could not submit request to clear orders";
            LOG.error(message);
            throw new OrderException(message);
        }

        boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
        if (!deleted) {
            String message = "Could not submit request to clear orders";
            LOG.error(message);
            throw new OrderException(message);
        }

        LOG.warn("Clear all orders request successful");
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
                LOG.error(ex.toString());
            }
        } while (!TradeUtils.tryCancelAllOrders(Global.options.getPair()) && !timedOut);

        if (timedOut) {
            String message = "There was a problem cancelling all existing orders";
            LOG.error(message);
            HipChatNotifications.sendMessage(message, MessageColor.YELLOW);
            MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
            //Continue anyway, maybe there is some balance to put up on order.
        }

    }

    public void fetch() {
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            this.orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public int countActiveOrders(String type) {

        this.fetch();

        int numOrders = 0;

        for (Order tempOrder : orderList) {
            if (tempOrder.getType().equalsIgnoreCase(type)) {
                numOrders++;
            }
        }

        LOG.debug("activeorders " + type + " " + numOrders);
        return numOrders;
    }

    public ArrayList<Order> filterOrders(ArrayList<Order> originalList, String type) {
        ArrayList<Order> toRet = new ArrayList<>();
        for (Order temp : originalList) {
            if (temp.getType().equalsIgnoreCase(type)) {
                toRet.add(temp);
            }
        }

        return toRet;
    }

    public void logActiveOrders() {
        LOG.debug("buy orders: " + this.getNumActiveBuyOrders());
        LOG.debug("sell orders: " + this.getNumActiveSellOrders());
    }

    public ArrayList<Order> getOrderList() {
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
