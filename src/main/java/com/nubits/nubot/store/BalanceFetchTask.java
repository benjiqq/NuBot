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

package com.nubits.nubot.store;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;

public class BalanceFetchTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(BalanceFetchTask.class.getName());

    /**
     * get balance every [x] milliseconds
     */
    private final int INTERVAL_TIME = 1000* 60 ;

    private HashMap<Currency,Amount> balances;

    private final Object lock = new Object();

    private CurrencyPair pair;

    public BalanceFetchTask(CurrencyPair pair){
        this.pair = pair;
    }

    @Override
    public void run(){

        boolean run = true;

        balances = new HashMap<>();
        balances.put(this.pair.getOrderCurrency(), new Amount(0.0, this.pair.getOrderCurrency()));
        balances.put(this.pair.getPaymentCurrency(), new Amount(0.0, this.pair.getPaymentCurrency()));

        //continously update
        while(run) {

            Iterator<Currency> it = balances.keySet().iterator();

            LOG.trace("balance fetch loop");

            while (it.hasNext()){

                Currency c = it.next();

                ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalance(c);

                if (balancesResponse.isPositive()) {
                    synchronized (lock) {
                        Amount a = (Amount)balancesResponse.getResponseObject();
                        LOG.debug("for " + c + " got amount " + a);
                        balances.put(c,a);
                    }
                } else{
                    LOG.error("balance fetch failed");
                }


            }



            try{
                Thread.currentThread().sleep(INTERVAL_TIME);
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
