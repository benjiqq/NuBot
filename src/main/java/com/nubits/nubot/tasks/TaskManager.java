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

package com.nubits.nubot.tasks;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.strategy.Primary.StrategyPrimaryPegTask;
import com.nubits.nubot.strategy.Secondary.StrategySecondaryPegTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class TaskManager {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManager.class.getName());
    private static final String STRATEGY_FIAT = "Strategy Fiat Task";
    private static final String STRATEGY_CRYPTO = "Strategy Crypto Task";

    protected int interval;
    private BotTask checkConnectionTask;
    private BotTask strategyFiatTask;
    private BotTask sendLiquidityTask;
    private BotTask checkNudTask;
    private BotTask priceMonitorTask; //use only with NuPriceMonitor

    //these are used for secondary peg strategy
    private BotTask secondaryPegTask;
    private BotTask priceTriggerTask;

    private ArrayList<BotTask> taskList;
    private boolean running;
    private boolean initialized = false;

    public TaskManager() {
        this.running = false;
        taskList = new ArrayList<BotTask>();

        //TODO! options can't be null
        //assign default values just for testing without Global.options loaded
        //TODO naming mixed

        setTasks();
    }

    public void setNudTask() {
        this.checkNudTask = new BotTask(
                new CheckNudTask(), Settings.CHECK_NUD_INTERVAL, "checkNud");
        taskList.add(checkNudTask);

    }

    private void setTasks() {
        //connectivity tasks

        LOG.info("setting up tasks");

        checkConnectionTask = new BotTask(
                new CheckConnectionTask(), Settings.CHECK_CONNECTION_INTERVAL, "checkConnection");
        taskList.add(checkConnectionTask);
        LOG.info("checkConnectionTask : " + checkConnectionTask);

        sendLiquidityTask = new BotTask(
                new SubmitLiquidityinfoTask(Global.options.verbose), Settings.SUBMIT_LIQUIDITY_SECONDS, "sendLiquidity");
        taskList.add(sendLiquidityTask);
        LOG.info("sendLiquidityTask : " + sendLiquidityTask);


        strategyFiatTask = new BotTask(
                new StrategyPrimaryPegTask(), Settings.EXECUTE_STRATEGY_INTERVAL, STRATEGY_FIAT);
        taskList.add(strategyFiatTask);
        LOG.info("strategyFiatTask : " + strategyFiatTask);

        secondaryPegTask = new BotTask(
                new StrategySecondaryPegTask(), Settings.EXECUTE_STRATEGY_INTERVAL, STRATEGY_CRYPTO);
        taskList.add(secondaryPegTask);
        LOG.info("secondaryPegTask : " + secondaryPegTask);

        //Select the correct interval
        int checkPriceInterval = Settings.CHECK_PRICE_INTERVAL;
        if (Global.options.pair.getPaymentCurrency().isFiat() && !Global.swappedPair
                || Global.options.pair.getOrderCurrency().isFiat() && Global.swappedPair) {
            checkPriceInterval = Settings.CHECK_PRICE_INTERVAL_FIAT;
        }
        priceTriggerTask = new BotTask(
                new PriceMonitorTriggerTask(), checkPriceInterval, "priceTriggerTask");
        taskList.add(priceTriggerTask);
        LOG.info("priceTriggerTask : " + priceTriggerTask);

        /*priceMonitorTask = new BotTask(
                new NuPriceMonitorTask(), Settings.CHECK_PRICE_INTERVAL, STRATEGY_CRYPTO);
        taskList.add(priceMonitorTask);*/

        initialized = true;
    }

    //Methods
    public void startAll() {
        for (int i = 0; i < taskList.size(); i++) {
            BotTask task = taskList.get(i);
            task.start();
        }

    }

    public void stopAll() throws IllegalStateException {
        LOG.info("\nStopping all tasks : -- ");
        boolean sentNotification = false;
        for (int i = 0; i < taskList.size(); i++) {

            BotTask bt = taskList.get(i);
            if (bt.getName().equals(STRATEGY_FIAT) || bt.getName().equals(STRATEGY_CRYPTO)) {
                if (!sentNotification) {
                    String additionalInfo = "";

                    additionalInfo = Global.options.getExchangeName() + " " + Global.options.getPair().toStringSep();

                    //dpn't send mail here for now
                    HipChatNotifications.sendMessageCritical("Bot shut-down ( " + additionalInfo + " )");
                    sentNotification = true;
                }
            }
            LOG.info("Shutting down " + bt.getName());
            try {
                bt.getTimer().cancel();
                bt.getTimer().purge();
            } catch (IllegalStateException e) {
                throw e;
            }

        }
    }

    public void printTasksStatus() {
        for (int i = 0; i < taskList.size(); i++) {
            BotTask task = taskList.get(i);
            LOG.info("Task name : " + task.getName() + ""
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
        return this.checkConnectionTask;
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

    public BotTask getSendLiquidityTask() {
        return sendLiquidityTask;
    }

    public BotTask getSecondaryPegTask() {
        return secondaryPegTask;
    }

    public void setSecondaryPegTask(BotTask secondaryPegTask) {
        this.secondaryPegTask = secondaryPegTask;
    }

    public BotTask getPriceTriggerTask() {
        return priceTriggerTask;
    }

    public void setPriceTriggerTask(BotTask priceTriggerTask) {
        this.priceTriggerTask = priceTriggerTask;
    }

    public void setSendLiquidityTask(BotTask slt) {
        this.sendLiquidityTask = slt;
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
