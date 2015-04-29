package com.nubits.nubot.strategy;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.models.OrderToPlace;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.trading.OrderException;
import com.nubits.nubot.utils.Utils;
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

    private long lastFetch;

    private int totalOrdersSubmitted = 0;

    /**
     * clear all orders
     *
     * @throws com.nubits.nubot.trading.OrderException
     */
    public void clearOrders() throws OrderException {
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
        } while (!tryCancelAllOrders(Global.options.getPair()) && !timedOut);

        if (timedOut) {
            String message = "There was a problem cancelling all existing orders";
            LOG.error(message);
            HipChatNotifications.sendMessage(message, MessageColor.YELLOW);
            MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
            //Continue anyway, maybe there is some balance to put up on order.
        }

    }

    /**
     * cancel all outstanding orders
     *
     * @param pair
     * @return
     */
    public boolean tryCancelAllOrders(CurrencyPair pair) {
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
                    LOG.info("Order clear request successful");
                    toRet = true;
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

    public boolean takeDownOrders(String type, CurrencyPair pair) {
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

    public boolean takeDownAndWait(String orderID, long timeoutMS, CurrencyPair pair) {

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


    private ApiResponse executeBuysideOrder(CurrencyPair pair, double amount, double rate) {
        LOG.info("executeBuysideOrder : " + pair + " " + amount + " " + rate);
        ApiResponse orderResponse;
        if (!Global.swappedPair) {
            orderResponse = Global.exchange.getTrade().buy(pair, amount, rate);
        } else {
            orderResponse = Global.exchange.getTrade().sell(pair, amount, rate);
        }
        return orderResponse;
    }

    private ApiResponse executeSellsideOrder(CurrencyPair pair, double amount, double rate) {
        LOG.info("executeSellsideOrder : " + pair + " " + amount + " " + rate);
        ApiResponse orderResponse;
        if (!Global.swappedPair) {
            orderResponse = Global.exchange.getTrade().sell(pair, amount, rate);
        } else {
            orderResponse = Global.exchange.getTrade().buy(pair, amount, rate);
        }
        return orderResponse;
    }

    private String orderString(String type, double amount1, double price) {

        String orderString;
        String sideStr = type + " side order : ";

        if (!Global.swappedPair) {
            orderString = sideStr + " " + type + " " + Utils.round(amount1, 4) + " " + Global.options.getPair().getOrderCurrency().getCode()
                    + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
        } else {
            String typeStr;
            if (type.equals(Constant.SELL)) {
                typeStr = Constant.BUY;
            } else {
                typeStr = Constant.SELL;
            }
            orderString = sideStr + " " + typeStr + " " + Utils.round(amount1, 4) + " " + Global.options.getPair().getOrderCurrency().getCode()
                    + " @ " + price + " " + Global.options.getPair().getPaymentCurrency().getCode();
        }

        return orderString;
    }


    /**
     * execute an order with specified type
     *
     * @param type
     * @param pair
     * @param amount
     * @param rate
     * @return
     */
    public boolean executeOrder(String type, CurrencyPair pair, double amount, double rate) {
        if (SessionManager.sessionInterrupted()) return false;

        String orderString = orderString(type, amount, rate);
        LOG.warn("Submitting limit order : " + orderString);

        if (Global.options.isExecuteOrders()) {

            if (SessionManager.startupTime == -1)
                SessionManager.startupTime = System.currentTimeMillis() - SessionManager.sessionStarted;

            ApiResponse orderResponse;
            if (type.equals(Constant.BUY)) {
                orderResponse = executeBuysideOrder(pair, amount, rate);
            } else {
                orderResponse = executeSellsideOrder(pair, amount, rate);
            }
            if (SessionManager.sessionInterrupted()) return false;

            if (orderResponse.isPositive()) {
                String msg = "New " + type + " wall is up on <strong>" + Global.options.getExchangeName() + "</strong> : " + orderString;
                HipChatNotifications.sendMessage(msg, MessageColor.YELLOW);
                LOG.info("Strategy - Order success: " + type + " Response = " + orderResponse.getResponseObject());
                totalOrdersSubmitted++;
                LOG.debug("order #" + totalOrdersSubmitted);
                return true;
            } else {
                LOG.error(orderResponse.getError().toString());
                return false;
            }
        } else {
            LOG.warn("Demo mode[executeorders:false] . Not executing orders");
            return true;
        }

    }


    //TODO: never used in live trading
    public ApiResponse placeOrder(OrderToPlace order) {
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

    //Init the order
    //TODO: never used in live trading
    public boolean placeMultipleOrders(ArrayList<OrderToPlace> orders) {
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

    /**
     * fetch orders without delay
     */
    public void fetchOrders() {
        if (SessionManager.sessionInterrupted()) return; //external interruption

        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            lastFetch = System.currentTimeMillis();
            this.orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }


    /**
     * fetch bound with time
     *
     * @param tresh
     */
    public void fetchTimeBound(double tresh) {
        long cur = System.currentTimeMillis();
        long diff = cur - lastFetch;
        LOG.trace("OrderManager. diff: " + diff);
        if (diff > tresh) {
            LOG.debug("Fetching active orders ...");
            fetchOrders();
        }
    }

    //public int getumOrders

    public int countOrder(String type) {
        int numOrders = 0;

        for (Order tempOrder : orderList) {
            if (tempOrder.getType().equalsIgnoreCase(type)) {
                numOrders++;
            }
        }

        LOG.trace("activeorders " + type + " " + numOrders);
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
        LOG.trace("buy orders: " + this.getNumActiveBuyOrders());
        LOG.trace("sell orders: " + this.getNumActiveSellOrders());
    }

    public ArrayList<Order> getOrderList() {
        return orderList;
    }

    public int getNumActiveSellOrders() {
        return countOrder(Constant.SELL);
    }

    public int getNumActiveBuyOrders() {
        return countOrder(Constant.BUY);
    }

    public int fetchSellOrdersTimeBound(double timetresh) {
        this.fetchTimeBound(timetresh);
        return this.getNumActiveSellOrders();
    }

    public int fetchBuyOrdersTimeBound(double timetresh) {
        this.fetchTimeBound(timetresh);
        return this.getNumActiveBuyOrders();
    }

    public int getNumTotalActiveOrders() {
        return orderList.size();
    }

    public int getTotalOrdersSubmitted() {
        return this.totalOrdersSubmitted;
    }

}
