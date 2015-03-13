package com.nubits.nubot.tasks;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class BalanceFetchTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BalanceFetchTask.class.getName());

    /**
     * get balance every [x] milliseconds
     */
    private final int interval_time = 1000;

    private Balance balance;

    private final Object lock = new Object();

    private CurrencyPair pair;

    public BalanceFetchTask(CurrencyPair pair){
        this.pair = pair;
    }

    @Override
    public void run(){

        boolean run = true;

        //continously update
        while(run) {

            ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(this.pair);

            if (balancesResponse.isPositive()) {
                synchronized (lock) {
                    balance = (Balance) balancesResponse.getResponseObject();
                    LOG.info("got balance " + balance);
                }
                //LOG.info("orders: " + currentOpenOrders.size());
            }

            try{
                Thread.currentThread().sleep(interval_time);
            }catch(Exception e){
                LOG.error("" + e);
            }

        }
    }

    public synchronized Balance getCurrentBalances(){
        synchronized (lock) {
            return this.balance;
        }
    }
}
