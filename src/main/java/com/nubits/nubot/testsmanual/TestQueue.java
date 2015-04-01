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

import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Query;
import com.nubits.nubot.utils.InitTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

/**
 * Demonstration of a simple FIFO queue to execute orders
 */
public class TestQueue {
    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestQueue.class.getName());

    final static long SPACING = 250; //500 ms
    private final Random random = new Random();

    private BlockingQueue<Query> ordersQueue = new DelayQueue();
    private boolean busy = false;

    public static void main(String[] args) {
        InitTests.setLoggingFilename(LOG);

        TestQueue test = new TestQueue();

        // Starting DelayQueue Producer to push some delayed objects to the queue
        test.startTradingSimulation();

        // Starting DelayQueue Consumer to take the expired delayed objects from the queue
        test.startConsumer();

    }


    private Thread tradingSimulation = new Thread(new Runnable() {
        @Override
        public void run() {
            int i = 1;
            while (i < 30) { //Simulate 30 orders
                try {
                    // Simulate a trading engine calling getQuery
                    getQuery("url", "method" + i, new TreeMap<String, String>(), false);
                    Thread.sleep(50 + (int) (Math.random() * 1 * 1000));
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }, "Trading simulation Thread");

    public void startTradingSimulation() {
        this.tradingSimulation.start();
    }

    private Thread consumerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    // Take elements out from the DelayQueue object.
                    Query query = ordersQueue.take();
                    getQueryImpl(query);
                    Thread.sleep(30); //change this and tune it properly
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }, "Query execution Thread");

    public void startConsumer() {
        this.consumerThread.start();
    }


    private void getQuery(String url, String method, TreeMap<String, String> query_args, boolean isGet) {

        //Compute the delay
        long delay = SPACING;

        try {
            //Wrap the request into a Query object
            Query query = new Query(url, method, query_args, isGet, delay);
            ordersQueue.put(query);
            LOG.info(query.getMethod() + " added to queue ");
        } catch (InterruptedException e) {
            LOG.error(e.toString());
        }

    }

    private void getQueryImpl(Query query) {
        busy = true;
        LOG.info("executing query " + query.getMethod());

        //Simulate query execution time random between 0.5 and 2 seconds

        sleep(330 + (int) (Math.random() * 700));

        busy = false;
    }


    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            LOG.error(e.toString());
        }
    }
}



