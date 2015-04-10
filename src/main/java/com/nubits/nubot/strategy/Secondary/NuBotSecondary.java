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
import com.nubits.nubot.bot.NuBotBase;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.tasks.PriceMonitorTriggerTask;
import com.nubits.nubot.tasks.SubmitLiquidityinfoTask;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a NuBot implementing dual strategy
 */
public class NuBotSecondary extends NuBotBase {

    final static Logger LOG = LoggerFactory.getLogger(NuBotSecondary.class);

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

        //TODO

        PriceMonitorTriggerTask pmTask = (PriceMonitorTriggerTask) Global.taskManager.getPriceTriggerTask().getTask();
        StrategySecondaryPegTask straTask = (StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask());

        // set trading strategy to the price monitor task
        pmTask.setStrategy(straTask);

        //TODO circular

        // set price monitor task to the strategy
        straTask.setPriceMonitorTask(pmTask);

        // set liquidityinfo task to the strategy
        SubmitLiquidityinfoTask liqTask = (SubmitLiquidityinfoTask) Global.taskManager.getSendLiquidityTask().getTask();
        straTask.setSendLiquidityTask(liqTask);

        PriceFeedManager pfm = null;
        try {
            pfm = new PriceFeedManager(Global.options.getMainFeed(), Global.options.getBackupFeedNames(), toTrackCurrencyPair);
        } catch (NuBotConfigException e) {
            throw new NuBotConfigException("can't configure price feeds");
        } catch (Exception e) {
            LOG.error("" + Global.options);
            throw new NuBotConfigException("something wrong with options");
        }

        pmTask.setPriceFeedManager(pfm);

        //Set the wallet shift threshold
        pmTask.setWallchangeThreshold(Global.options.getWallchangeThreshold());


        //read the delay to sync with remote clock
        //issue 136 - multi custodians on a pair.
        //walls are removed and re-added every three minutes.
        //Bot needs to wait for next 3 min window before placing walls
        //set the interval from settings

        int reset_every = Settings.RESET_EVERY_MINUTES;

        int interval = 1;
        if (Global.options.isMultipleCustodians()) {
            interval = 60 * reset_every;
        } else {
            interval = Settings.CHECK_PRICE_INTERVAL;
        }
        Global.taskManager.getPriceTriggerTask().setInterval(interval);

        if (Global.options.isMultipleCustodians()) {
            //Force the a spread to avoid collisions
            double forcedSpread = Settings.FORCED_SPREAD;
            if (Global.options.getSpread() < forcedSpread) {
                Global.options.setSpread(forcedSpread);
                LOG.info("Forcing a " + forcedSpread + "% minimum spread to protect from collisions");
            }
        }

        int delaySeconds = 0;

        LOG.info("multiple custodians: " + Global.options.isMultipleCustodians());

        if (Global.options.isMultipleCustodians()) {
            delaySeconds = Utils.getSecondsToNextwindow(reset_every);
            LOG.info("NuBot will start running in " + delaySeconds + " seconds, to sync with remote NTP and place walls during next wall shift window.");
        } else {

            LOG.info("NuBot will not try to sync with other bots via remote NTP : 'multiple-custodians' is set to false");
        }
        //then start the thread
        Global.taskManager.getPriceTriggerTask().start(delaySeconds);
    }


}
