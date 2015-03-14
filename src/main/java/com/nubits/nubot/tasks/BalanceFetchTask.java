package com.nubits.nubot.tasks;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class BalanceFetchTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BalanceFetchTask.class.getName());

    /**
     * get balance every [x] milliseconds
     */
    private final int interval_time = 1000;

    private HashMap<Currency,Amount> balances;

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

            Iterator<Currency> it = balances.keySet().iterator();

            while (it.hasNext()){
                Currency c = it.next();

                ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalance(c);

                if (balancesResponse.isPositive()) {
                    synchronized (lock) {
                        Amount a = (Amount)balancesResponse.getResponseObject();
                        LOG.info("for " + c + " got amount " + a);
                        balances.put(c,a);
                    }
                    //LOG.info("orders: " + currentOpenOrders.size());
                }
            }



            try{
                Thread.currentThread().sleep(interval_time);
            }catch(Exception e){
                LOG.error("" + e);
            }

        }
    }

    public synchronized Amount getCurrentAmount(Currency c){
        synchronized (lock) {
            return this.balances.get(c);
        }
    }

}