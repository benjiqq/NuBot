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

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.strategy.Primary.StrategyPrimaryPegTask;
import com.nubits.nubot.strategy.Secondary.StrategySecondaryPegTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class TaskManager {

    private static final Logger LOG = LoggerFactory.getLogger(TaskManager.class.getName());
    private static final String STRATEGY_FIAT = "Strategy Fiat Task";
    private static final String STRATEGY_CRYPTO = "Strategy Crypto Task";

    //protected int interval;
    private BotTask checkConnectionTask;
    private BotTask strategyFiatTask;
    private BotTask sendLiquidityTask;
    private BotTask checkNudTask;

    //these are used for secondary peg strategy
    private BotTask secondaryPegTask;
    private BotTask priceTriggerTask;

    private ArrayList<BotTask> taskList;
    private boolean running;
    private boolean initialized = false;

    public TaskManager() {
        this.running = false;
        taskList = new ArrayList<BotTask>();

        //assign default values just for testing without Global.options loaded

    }

    /**
     * setup the task for checking Nu RPC
     */
    public void setupNuRPCTask() {
        LOG.info("Setting up RPC client on " + Global.options.getNudIp() + ":" + Global.options.getNudPort());

        Global.rpcClient = new NuRPCClient(Global.options.getNudIp(), Global.options.getNudPort(),
                Global.options.getRpcUser(), Global.options.getRpcPass(), true,
                Global.options.getNubitsAddress(), Global.options.getPair(), Global.options.getExchangeName());

        this.setNudTask();

    }

    public void startTaskNu() {
        LOG.info("Starting task : Check connection with Nud");
        this.getCheckNudTask().start();
    }


    public void setNudTask() {
        this.checkNudTask = new BotTask(
                new CheckNudTask(), Settings.CHECK_NUD_INTERVAL, "checkNud");
        taskList.add(checkNudTask);

    }

    public void setTasks() {
        //connectivity tasks

        LOG.info("setting up tasks");

        checkConnectionTask = new BotTask(
                new CheckConnectionTask(), Settings.CHECK_CONNECTION_INTERVAL, "checkConnection");
        taskList.add(checkConnectionTask);
        LOG.debug("checkConnectionTask : " + checkConnectionTask);

        sendLiquidityTask = new BotTask(
                new SubmitLiquidityinfoTask(Global.options.verbose), Settings.SUBMIT_LIQUIDITY_SECONDS, "sendLiquidity");
        taskList.add(sendLiquidityTask);
        LOG.debug("sendLiquidityTask : " + sendLiquidityTask);


        strategyFiatTask = new BotTask(
                new StrategyPrimaryPegTask(), Settings.EXECUTE_STRATEGY_INTERVAL, STRATEGY_FIAT);
        taskList.add(strategyFiatTask);
        LOG.debug("strategyFiatTask : " + strategyFiatTask);

        secondaryPegTask = new BotTask(
                new StrategySecondaryPegTask(), Settings.EXECUTE_STRATEGY_INTERVAL, STRATEGY_CRYPTO);
        taskList.add(secondaryPegTask);
        LOG.debug("secondaryPegTask : " + secondaryPegTask);

        //Select the correct interval
        int checkPriceInterval = Settings.CHECK_PRICE_INTERVAL;
        CurrencyPair pair = Global.options.getPair();
        boolean checkFiat = pair.getPaymentCurrency().isFiat() && !Global.swappedPair
                || pair.getOrderCurrency().isFiat() && Global.swappedPair;

        if (checkFiat) {
            checkPriceInterval = Settings.CHECK_PRICE_INTERVAL_FIAT;
        }

        priceTriggerTask = new BotTask(
                new PriceMonitorTriggerTask(), checkPriceInterval, "priceTriggerTask");
        taskList.add(priceTriggerTask);
        LOG.debug("priceTriggerTask : " + priceTriggerTask);

        initialized = true;
    }

    public void startAll() {
        for (int i = 0; i < taskList.size(); i++) {
            BotTask task = taskList.get(i);
            task.start();
        }

    }

    public boolean stopAll() throws IllegalStateException {
        LOG.info("Stopping all BotTasks. ");
        boolean sentNotification = false;
        for (int i = 0; i < taskList.size(); i++) {

            BotTask bt = taskList.get(i);

            LOG.debug("Shutting down " + bt.getName());
            try {
                bt.getTimer().cancel();
                bt.getTimer().purge();
            } catch (IllegalStateException e) {
                LOG.error("" + e);
                throw e;
            }
        }
        LOG.info("BotTasks stopped. ");
        return true;
    }

    public void printTasksStatus() {
        for (int i = 0; i < taskList.size(); i++) {
            BotTask task = taskList.get(i);
            LOG.info("Task name : " + task.getName() + ""
                    + " running : " + task.isRunning());
        }
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

    public BotTask getSendLiquidityTask() {
        return sendLiquidityTask;
    }

    public BotTask getSecondaryPegTask() {
        return secondaryPegTask;
    }

    public BotTask getPriceTriggerTask() {
        return priceTriggerTask;
    }

    public BotTask getCheckNudTask() {
        return checkNudTask;
    }

    public boolean isInitialized() {
        return initialized;
    }

}
