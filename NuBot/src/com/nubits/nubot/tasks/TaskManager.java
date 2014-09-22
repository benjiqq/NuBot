/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
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
package com.nubits.nubot.tasks;

import com.nubits.nubot.global.Global;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.jhipchat.messages.Message.Color;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author desrever < desrever@nubits.com >
 */
public class TaskManager {

    private static final Logger LOG = Logger.getLogger(TaskManager.class.getName());
    private static final String STRATEGY_FIAT = "Strategy Fiat Task";
    private static final String STRATEGY_CRYPTO = "Strategy Crypto Task";
//Class Variables
    protected int interval;
    private BotTask checkConnectionTask;
    private BotTask strategyFiatTask;
    private BotTask strategyCryptoTask;
    private BotTask checkOrdersTask;
    private BotTask checkNudTask;
    private BotTask priceMonitorTask; //use only with NuPriceMonitor
    private ArrayList<BotTask> taskList;
    private boolean running;
    private boolean initialized = false;
//Constructor

    public TaskManager() {
        this.running = false;
        taskList = new ArrayList<BotTask>();
        //assign default values in case are not defined in options.json
        int checkOrdersInterval = 30,
                checkbalanceInterval = 30,
                checkPriceInterval = 61;

        boolean verbose = false;

        if (Global.options != null) {
            //If global option have been loaded
            checkOrdersInterval = Global.options.getCheckOrdersInteval();
            checkbalanceInterval = Global.options.getCheckBalanceInterval();
            checkPriceInterval = Global.options.getCryptoPegOptions().getRefreshTime();
            verbose = Global.options.isVerbose();
        }

        checkConnectionTask = new BotTask(
                new CheckConnectionTask(), 127, "checkConnection");
        taskList.add(checkConnectionTask);

        checkOrdersTask = new BotTask(
                new CheckOrdersTask(verbose), checkOrdersInterval, "checkOrders"); //true for verbosity
        taskList.add(checkOrdersTask);

        checkNudTask = new BotTask(
                new CheckNudTask(), 30, "checkNud");
        taskList.add(checkNudTask);

        strategyFiatTask = new BotTask(
                new StrategyFiatTask(), checkbalanceInterval, STRATEGY_FIAT);
        taskList.add(strategyFiatTask);

        strategyCryptoTask = new BotTask(
                new StrategyCryptoTask(), checkPriceInterval, STRATEGY_CRYPTO);
        taskList.add(strategyCryptoTask);

        priceMonitorTask = new BotTask(
                new PriceMonitorTask(), checkbalanceInterval, STRATEGY_CRYPTO);
        taskList.add(priceMonitorTask);

        initialized = true;
    }

    //Methods
    public void startAll() {
        for (int i = 0; i < taskList.size(); i++) {
            BotTask task = taskList.get(i);
            task.start();
        }

    }

    public void stopAll() {
        LOG.fine("\nStopping all tasks : -- ");
        boolean sentNotification = false;
        for (int i = 0; i < taskList.size(); i++) {

            BotTask bt = taskList.get(i);
            if (bt.getName().equals(STRATEGY_FIAT) || bt.getName().equals(STRATEGY_CRYPTO)) {
                if (!sentNotification) {
                    HipChatNotifications.sendMessage("Bot shut-down", Color.RED);
                    sentNotification = true;
                }
            }
            LOG.fine("Shutting down " + bt.getName());
            try {
                bt.getTimer().cancel();
                bt.getTimer().purge();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                System.exit(0);
            }

        }
    }

    public void printTasksStatus() {
        for (int i = 0; i < taskList.size(); i++) {
            BotTask task = taskList.get(i);
            LOG.fine("Task name : " + task.getName() + ""
                    + " running : " + task.isRunning());
        }
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * @return the isRunning
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param isRunning the isRunning to set
     */
    public void setRunning(boolean isRunning) {
        this.running = isRunning;
    }

    public BotTask getCheckConnectionTask() {
        return checkConnectionTask;
    }

    public void setCheckConnectionTask(BotTask checkConnectionTask) {
        this.checkConnectionTask = checkConnectionTask;
    }

    public BotTask getStrategyFiatTask() {
        return strategyFiatTask;
    }

    public void setStrategyFiatTask(BotTask strategyFiatTask) {
        this.strategyFiatTask = strategyFiatTask;
    }

    public BotTask getStrategyCryptoTask() {
        return strategyCryptoTask;
    }

    public void setStrategyCryptoTask(BotTask strategyCryptoTask) {
        this.strategyCryptoTask = strategyCryptoTask;
    }

    public BotTask getCheckOrdersTask() {
        return checkOrdersTask;
    }

    public void setCheckOrdersTask(BotTask checkOrdersTask) {
        this.checkOrdersTask = checkOrdersTask;
    }

    public ArrayList<BotTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(ArrayList<BotTask> taskList) {
        this.taskList = taskList;
    }

    public BotTask getCheckNudTask() {
        return checkNudTask;
    }

    public void setCheckNudTask(BotTask checkNudTask) {
        this.checkNudTask = checkNudTask;
    }

    public BotTask getPriceMonitorTask() {
        return priceMonitorTask;
    }

    public void setPriceMonitorTask(BotTask priceMonitorTask) {
        this.priceMonitorTask = priceMonitorTask;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
