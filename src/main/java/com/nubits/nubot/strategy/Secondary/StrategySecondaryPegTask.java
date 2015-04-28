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

package com.nubits.nubot.strategy.Secondary;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.tasks.PriceMonitorTriggerTask;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;


public class StrategySecondaryPegTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(StrategySecondaryPegTask.class.getName());

    private StrategySecondaryPegUtils strategyUtils = new StrategySecondaryPegUtils(this);

    private boolean mightNeedInit = true;

    private boolean ordersAndBalancesOK;

    private boolean needWallShift;

    private double sellPricePEG;

    private double buyPricePEG;

    private boolean shiftingWalls = false;

    private String priceDirection;  //this parameter can be either Constant.UP (when the price of the new order increased since last wall) or Constant.DOWN
    private PriceMonitorTriggerTask priceMonitorTask;
    private SubmitLiquidityinfoTask sendLiquidityTask;
    private boolean isFirstTime = true;
    private boolean proceedsInBalance = false; // Only used on secondary peg to fiat (EUR , CNY etc)
    private boolean resettingOrders = false; //Flag turned true by resetorders

    @Override
    public void run() {
        if (SessionManager.sessionInterrupted()) return; //external interruption
        LOG.debug("Executing task on " + Global.exchange.getName() + ": StrategySecondaryPegTask. DualSide :  " + Global.options.isDualSide());


        if (isFirstTime) {
            initStrategy();
        } else {
            adaptOrders();
        }
    }

    public void adaptOrders() {
        if (SessionManager.sessionInterrupted()) return; //external interruption

        LOG.debug("adapt orders");

        if (Global.options.isMultipleCustodians()) {
            LOG.trace("multiple custodians do not need strategy exec");
            return;
        }

        if (shiftingWalls) {
            LOG.info("Already shifting walls.");
            return;
        }

        strategyUtils.recount(); //Count number of active sells and buys
        if (SessionManager.sessionInterrupted()) return; //external interruption

        if (mightNeedInit) {
            LOG.info("might need init");
            boolean reset = mightNeedInit && !(ordersAndBalancesOK);
            if (reset) {
                String message = "Order reset needed on " + Global.exchange.getName();
                HipChatNotifications.sendMessage(message, MessageColor.PURPLE);
                if (SessionManager.sessionInterrupted()) return; //external interruption

                LOG.warn(message);
                LOG.debug("mightNeedInit: " + mightNeedInit + " ordersAndBalancesOK: " + ordersAndBalancesOK);
                boolean reinitiateSuccess = strategyUtils.reInitiateOrders(false);
                if (reinitiateSuccess) {
                    mightNeedInit = false;
                }
            } else {
                LOG.debug("No need to init new orders since current orders are correct");
            }

            strategyUtils.recount();
        }

        //Make sure the orders and balances are ok or try to aggregate
        if (!ordersAndBalancesOK) {
            LOG.warn("Detected a number of active orders not in line with strategy. Will try to aggregate soon");
            mightNeedInit = true;
        } else {
            if (Global.options.getKeepProceeds() > 0 && Global.options.getPair().getPaymentCurrency().isFiat()) {
                //Execute buy Side strategy
                if (Global.options.isDualSide() && proceedsInBalance && !needWallShift) {
                    strategyUtils.aggregateAndKeepProceeds();
                }
            }
        }

    }

    public void initStrategy() {
        if (SessionManager.sessionInterrupted()) return; //external interruption

        //First execution : reset orders and init strategy
        LOG.info("Initializing strategy");
        LOG.info("setting up ordermanager");


        isFirstTime = false;
        strategyUtils.recount();
        boolean reinitiateSuccess = strategyUtils.reInitiateOrders(true);
        if (!reinitiateSuccess) {
            LOG.error("There was a problem while trying to reinitiating orders on first execution. Trying again on next execution");
            isFirstTime = true;
        }
        getSendLiquidityTask().setFirstOrdersPlaced(true);
    }

    public void notifyPriceChanged(double new_sellPricePEG, double new_buyPricePEG, double conversion, String direction) {
        if (SessionManager.sessionInterrupted()) return; //external interruption

        if (shiftingWalls) {
            LOG.warn("Shift request failed, shift in progress.");
            return;
        }

        shiftingWalls = true;

        LOG.info("Strategy received a price change notification.");
        needWallShift = true;

        if (!Global.swappedPair) {
            sellPricePEG = new_sellPricePEG;
            buyPricePEG = new_buyPricePEG;
        } else {
            sellPricePEG = new_buyPricePEG;
            buyPricePEG = new_sellPricePEG;
        }
        this.priceDirection = direction;

        //execute immediately
        boolean shiftSuccess = false;

        String currencyTracked = "";
        if (Global.swappedPair) {
            currencyTracked = Global.options.getPair().getOrderCurrency().getCode().toUpperCase();
        } else {
            currencyTracked = Global.options.getPair().getPaymentCurrency().getCode().toUpperCase();
        }

        String message = "Shift needed on " + Global.exchange.getName() + " Reason : ";
        if (!Global.options.isMultipleCustodians()) {
            message += currencyTracked + " price went " + getPriceDirection() + " more than " + Global.options.getWallchangeThreshold() + " %";
        } else {
            message += Settings.RESET_EVERY_MINUTES + " minutes elapsed since last shift";
        }
        HipChatNotifications.sendMessage(message, MessageColor.PURPLE);
        LOG.warn(message);
        if (SessionManager.sessionInterrupted()) return; //external interruption

        shiftSuccess = strategyUtils.shiftWalls();
        if (shiftSuccess) {
            mightNeedInit = false;
            needWallShift = false;
            LOG.info("Wall shift successful");
        } else {
            LOG.error("Wall shift failed");
        }
        shiftingWalls = false;

    }

    public double getSellPricePEG() {
        return sellPricePEG;
    }

    public void setSellPricePEG(double sellPricePEG) {
        LOG.info("set setSellPricePEG : " + sellPricePEG);
        this.sellPricePEG = sellPricePEG;
    }

    public double getBuyPricePEG() {
        return buyPricePEG;
    }

    public void setBuyPricePEG(double buyPricePEG) {
        LOG.info("setBuyPricePEG : " + buyPricePEG);
        this.buyPricePEG = buyPricePEG;
    }

    public PriceMonitorTriggerTask getPriceMonitorTask() {
        return priceMonitorTask;
    }

    public void setPriceMonitorTask(PriceMonitorTriggerTask priceMonitorTask) {
        this.priceMonitorTask = priceMonitorTask;
    }

    public SubmitLiquidityinfoTask getSendLiquidityTask() {
        return sendLiquidityTask;
    }

    public void setSendLiquidityTask(SubmitLiquidityinfoTask sendLiquidityTask) {
        this.sendLiquidityTask = sendLiquidityTask;
    }


    public boolean isMightNeedInit() {
        return mightNeedInit;
    }

    public void setMightNeedInit(boolean mightNeedInit) {
        this.mightNeedInit = mightNeedInit;
    }

    public boolean isOrdersAndBalancesOK() {
        return ordersAndBalancesOK;
    }

    public void setOrdersAndBalancesOK(boolean ordersAndBalancesOK) {
        this.ordersAndBalancesOK = ordersAndBalancesOK;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setIsFirstTime(boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public boolean isProceedsInBalance() {
        return proceedsInBalance;
    }

    public void setProceedsInBalance(boolean proceedsInBalance) {
        this.proceedsInBalance = proceedsInBalance;
    }

    public String getPriceDirection() {
        return priceDirection;
    }

    public void setPriceDirection(String priceDirection) {
        this.priceDirection = priceDirection;
    }

    public boolean isResettingOrders() {
        return resettingOrders;
    }

    public void setResettingOrders(boolean resettingOrders) {
        this.resettingOrders = resettingOrders;
    }
}
