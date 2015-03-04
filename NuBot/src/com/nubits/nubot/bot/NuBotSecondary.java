/*
 * Copyright (C) 2014-2015 Nu Development Team
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
package com.nubits.nubot.bot;

import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.*;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import com.nubits.nubot.tasks.strategy.PriceMonitorTriggerTask;
import com.nubits.nubot.tasks.strategy.StrategySecondaryPegTask;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * a NuBot implementing dual strategy
 */
public class NuBotSecondary extends NuBotBase {

    final static Logger LOG = LoggerFactory.getLogger(NuBotSecondary.class);

    public NuBotSecondary() {

    }

    @Override
    public void configureStrategy() throws NuBotConfigException {
        if (Global.options.isDualSide()) {
            LOG.info("Configuring NuBot for Dual-Side strategy");
        } else {
            LOG.info("Configuring NuBot for Sell-Side strategy");
        }


        //Peg to a USD price via crypto pair
        Currency toTrackCurrency;

        if (Global.swappedPair) { //NBT as paymentCurrency
            toTrackCurrency = Global.options.getPair().getOrderCurrency();
        } else {
            toTrackCurrency = Global.options.getPair().getPaymentCurrency();
        }

        CurrencyPair toTrackCurrencyPair = new CurrencyPair(toTrackCurrency, CurrencyList.USD);

        //TODO strategy tasks be in bots package

        PriceMonitorTriggerTask pmtask = (PriceMonitorTriggerTask) Global.taskManager.getPriceTriggerTask().getTask();
        StrategySecondaryPegTask strattask = (StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask());

        // set trading strategy to the price monitor task
        pmtask.setStrategy(strattask);

        //TODO circular

        // set price monitor task to the strategy
        strattask.setPriceMonitorTask(pmtask);

        PriceMonitorTriggerTask monitorTask = (PriceMonitorTriggerTask) Global.taskManager.getPriceTriggerTask().getTask();
        strattask.setPriceMonitorTask(monitorTask);

        // set liquidityinfo task to the strategy
        SubmitLiquidityinfoTask liqtask = (SubmitLiquidityinfoTask) Global.taskManager.getSendLiquidityTask().getTask();
        strattask.setSendLiquidityTask(liqtask);

        PriceFeedManager pfm = null;
        try {
            pfm = new PriceFeedManager(opt.getMainFeed(), opt.getBackupFeedNames(), toTrackCurrencyPair);
        } catch (NuBotConfigException e) {
            exitWithNotice("can't configure price feeds");
        }

        //Then set the pfm
        pmtask.setPriceFeedManager(pfm);

        //Set the priceDistance threshold
        pmtask.setDistanceTreshold(opt.getDistanceThreshold());

        //Set the wallet shift threshold
        pmtask.setWallchangeThreshold(opt.getWallchangeThreshold());

        //Set the outputpath for wallshifts

        String outputPath = logsFolder + "wall_shifts.csv";
        ((PriceMonitorTriggerTask) (Global.taskManager.getPriceTriggerTask().getTask())).setOutputPath(outputPath);
        FileSystem.writeToFile("timestamp,source,crypto,price,currency,sellprice,buyprice,otherfeeds\n", outputPath, false);

        //read the delay to sync with remote clock
        //issue 136 - multi custodians on a pair.
        //walls are removed and re-added every three minutes.
        //Bot needs to wait for next 3 min window before placing walls
        //set the interval from settings

        int reset_every = NuBotAdminSettings.reset_every_minutes;

        int interval = 1;
        if (Global.options.isMultipleCustodians()) {
            interval = 60 * reset_every;
        } else {
            interval = NuBotAdminSettings.refresh_time_seconds;
        }
        Global.taskManager.getPriceTriggerTask().setInterval(interval);

        if (Global.options.isMultipleCustodians()) {
            //Force the a spread to avoid collisions
            double forcedSpread = 0.9;
            LOG.info("Forcing a " + forcedSpread + "% minimum spread to protect from collisions");
            if (Global.options.getSpread() < forcedSpread) {
                Global.options.setSpread(forcedSpread);
            }
        }

        int delaySeconds = 0;

        if (Global.options.isMultipleCustodians()) {
            delaySeconds = Utils.getSecondsToNextwindow(reset_every);
            LOG.info("NuBot will be start running in " + delaySeconds + " seconds, to sync with remote NTP and place walls during next wall shift window.");
        } else {
            LOG.warn("NuBot will not try to sync with other bots via remote NTP : 'multiple-custodians' is set to false");
        }
        //then start the thread
        Global.taskManager.getPriceTriggerTask().start(delaySeconds);
    }


}
