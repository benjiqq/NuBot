package com.nubits.nubot.tasks;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class OrderFetchTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(OrderFetchTask.class.getName());

    /**
     * get orders every [x] milliseconds
     */
    private final int interval_time = 1000;

    private ArrayList<Order> currentOpenOrders;

    private final Object lock = new Object();

    @Override
    public void run(){

        boolean run = true;

        //continously update
        while(run) {

            ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
            if (activeOrdersResponse.isPositive()) {
                synchronized (lock) {
                    currentOpenOrders= (ArrayList<Order>) activeOrdersResponse.getResponseObject();
                    LOG.info("got current orders " + currentOpenOrders.size());
                }
            } else{
                LOG.error("could not get orders");
            }

            try{
                Thread.currentThread().sleep(interval_time);
            }catch(Exception e){
                LOG.error("" + e);
            }

        }
    }

    public synchronized ArrayList<Order> getCurrentOpenOrders(){
        synchronized (lock) {
            return this.currentOpenOrders;
        }
    }
}
