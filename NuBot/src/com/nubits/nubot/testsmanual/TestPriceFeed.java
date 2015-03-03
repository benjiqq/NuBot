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
package com.nubits.nubot.testsmanual;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.AbstractPriceFeed;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.pricefeeds.PriceFeedManager.LastPriceResponse;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class TestPriceFeed {
    //refer to FEEDS.md for the list of price feeds

    private static final Logger LOG = LoggerFactory.getLogger(TestPriceFeed.class.getName());

    public static void main(String a[]) {
        TestPriceFeed test = new TestPriceFeed();
        test.init();
        //test.executeSingle(BitcoinaveragePriceFeed, Constant.BTC_USD); //Uncomment to test a single price feed

        test.trackBTC(); //Test BTC
        test.trackPPC(); //Test PPC
        test.trackEUR(); //Test EUR
        test.trackCNY(); //Test CNY
        test.trackHKD(); //Test HKD
        test.trackPHP(); //Test PHP

    }

    private void init() {
        Utils.loadProperties("settings.properties");
        //feed = new BitcoinaveragePriceFeed();
        String folderName = "tests_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;

        LOG.info("Set up SSL certificates");
        Utils.installKeystore(false);
    }

    private void executeSingle(AbstractPriceFeed feed, CurrencyPair pair) {
        LOG.info("Testing feed :  " + feed.getName() + " , pair : " + pair.toString());
        LastPrice lastPrice = feed.getLastPrice(pair);
        if (!lastPrice.isError()) {
            LOG.info(lastPrice.toString());
        } else {
            //handle error
            LOG.error("There was a problem while updating the price");
        }
    }

    private void trackBTC() {

        String mainFeed = PriceFeedManager.BTCE;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(PriceFeedManager.BITCOINAVERAGE);
        backupFeedList.add(PriceFeedManager.BLOCKCHAIN);
        backupFeedList.add(PriceFeedManager.COINBASE);
        backupFeedList.add(PriceFeedManager.CCEDK);
        backupFeedList.add(PriceFeedManager.BTER);
        //TODO add bitfinex and  bitstamp after merging this branch with develop

        execute(mainFeed, backupFeedList, Constant.BTC_USD);

    }

    private void trackPPC() {
        ArrayList<String> backupFeedList = new ArrayList<>();

        String mainFeed = PriceFeedManager.BTCE;

        backupFeedList.add(PriceFeedManager.COINMARKETCAP_NO);
        backupFeedList.add(PriceFeedManager.COINMARKETCAP_NE);

        execute(mainFeed, backupFeedList, Constant.PPC_USD);
    }

    private void trackEUR() {
        String mainFeed = PriceFeedManager.BITSTAMP_EURUSD;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(PriceFeedManager.OPENEXCHANGERATES);
        backupFeedList.add(PriceFeedManager.GOOGLE_UNOFFICIAL);
        backupFeedList.add(PriceFeedManager.EXCHANGERATELAB);
        backupFeedList.add(PriceFeedManager.YAHOO);

        execute(mainFeed, backupFeedList, Constant.EUR_USD);
    }

    private void trackHKD() {
        String mainFeed = PriceFeedManager.OPENEXCHANGERATES;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(PriceFeedManager.GOOGLE_UNOFFICIAL);
        backupFeedList.add(PriceFeedManager.YAHOO);

        execute(mainFeed, backupFeedList, Constant.HKD_USD);
    }

    private void trackPHP() {
        String mainFeed = PriceFeedManager.OPENEXCHANGERATES;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(PriceFeedManager.GOOGLE_UNOFFICIAL);
        backupFeedList.add(PriceFeedManager.YAHOO);

        execute(mainFeed, backupFeedList, Constant.PHP_USD);
    }

    private void trackCNY() {
        String mainFeed = PriceFeedManager.OPENEXCHANGERATES;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(PriceFeedManager.GOOGLE_UNOFFICIAL);
        backupFeedList.add(PriceFeedManager.EXCHANGERATELAB);
        backupFeedList.add(PriceFeedManager.YAHOO);

        execute(mainFeed, backupFeedList, Constant.CNY_USD);
    }

    private void execute(String mainFeed, ArrayList<String> backupFeedList, CurrencyPair pair) {

        PriceFeedManager pfm = new PriceFeedManager(mainFeed, backupFeedList, pair);

        LastPriceResponse lpr = pfm.getLastPrices();


        ArrayList<LastPrice> priceList = pfm.getLastPrices().getPrices();

        LOG.info("\n\n\n ---------------------- Testing " + pair.toString("/"));
        LOG.info("Positive response from " + priceList.size() + "/" + pfm.getFeedList().size() + " feeds\n");
        for (int i = 0; i < priceList.size(); i++) {
            LastPrice tempPrice = priceList.get(i);
            LOG.info(tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
                    + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode());
        }
    }
}
