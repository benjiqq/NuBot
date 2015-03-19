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

            LOG.info("order fetch loop");

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
