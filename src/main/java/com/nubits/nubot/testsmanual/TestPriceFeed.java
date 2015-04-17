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
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.pricefeeds.FeedFacade;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.pricefeeds.feedservices.*;
import com.nubits.nubot.utils.InitTests;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class TestPriceFeed {
    //refer to FEEDS.md for the list of price feeds

    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestPriceFeed.class.getName());

    public static void main(String a[]) {
        InitTests.setLoggingFilename(LOG);

        TestPriceFeed test = new TestPriceFeed();
        test.init();
        //test.executeSingle(BitcoinaveragePriceFeed, Constant.BTC_USD); //Uncomment to test a single price feed

        //test.trackBTC(); //Test BTC
        //test.trackPPC(); //Test PPC
        test.trackEUR(); //Test EUR
        //test.trackCNY(); //Test CNY
        //test.trackHKD(); //Test HKD
        //test.trackPHP(); //Test PHP

    }

    private void init() {
        LOG.info("Set up SSL certificates");
        Utils.installKeystore(false);
    }

    private void executeSingle(AbstractPriceFeed feed, CurrencyPair pair) {
        LOG.info("Testing feed :  " + feed.getClass() + " , pair : " + pair.toString());
        LastPrice lastPrice = feed.getLastPrice(pair);
        if (!lastPrice.isError()) {
            LOG.info(lastPrice.toString());
        } else {
            //handle error
            LOG.error("There was a problem while updating the price");
        }
    }

    private void trackBTC() {

        String mainFeed = FeedFacade.BtcePriceFeed;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(FeedFacade.BitcoinaveragePriceFeed);
        backupFeedList.add(FeedFacade.BlockchainPriceFeed);
        backupFeedList.add(FeedFacade.CoinbasePriceFeed);
        backupFeedList.add(FeedFacade.CcedkPriceFeed);
        backupFeedList.add(FeedFacade.BterPriceFeed);
        backupFeedList.add(FeedFacade.BitfinexPriceFeed);
        backupFeedList.add(FeedFacade.BitstampPriceFeed);

        execute(mainFeed, backupFeedList, CurrencyList.BTC_USD);

    }

    private void trackPPC() {
        ArrayList<String> backupFeedList = new ArrayList<>();

        String mainFeed = FeedFacade.BtcePriceFeed;

        backupFeedList.add(FeedFacade.CoinmarketcapnorthpolePriceFeed);
        backupFeedList.add(FeedFacade.CoinmarketcapnexuistPriceFeed);

        execute(mainFeed, backupFeedList, CurrencyList.PPC_USD);
    }

    private void trackEUR() {
        String mainFeed = BitstampEURPriceFeed.name;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(OpenexchangeratesPriceFeed.name);
        backupFeedList.add(GoogleUnofficialPriceFeed.name);
        backupFeedList.add(ExchangeratelabPriceFeed.name);
        backupFeedList.add(FeedFacade.YahooPriceFeed);

        execute(mainFeed, backupFeedList, CurrencyList.EUR_USD);
    }

    private void trackHKD() {
        String mainFeed = OpenexchangeratesPriceFeed.name;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(GoogleUnofficialPriceFeed.name);
        backupFeedList.add(FeedFacade.YahooPriceFeed);

        execute(mainFeed, backupFeedList, CurrencyList.HKD_USD);
    }

    private void trackPHP() {
        String mainFeed = OpenexchangeratesPriceFeed.name;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(GoogleUnofficialPriceFeed.name);
        backupFeedList.add(FeedFacade.YahooPriceFeed);

        execute(mainFeed, backupFeedList, CurrencyList.PHP_USD);
    }

    private void trackCNY() {
        String mainFeed = OpenexchangeratesPriceFeed.name;

        ArrayList<String> backupFeedList = new ArrayList<>();

        backupFeedList.add(GoogleUnofficialPriceFeed.name);
        backupFeedList.add(FeedFacade.YahooPriceFeed);
        backupFeedList.add(ExchangeratelabPriceFeed.name);

        execute(mainFeed, backupFeedList, CurrencyList.CNY_USD);
    }

    private void execute(String mainFeed, ArrayList<String> backupFeedList, CurrencyPair pair) {

        PriceFeedManager pfm = null;
        try {
            pfm = new PriceFeedManager(mainFeed, backupFeedList, pair);
        } catch (NuBotConfigException e) {
            LOG.error(e.toString());
        }

        pfm.fetchLastPrices();
        ArrayList<LastPrice> priceList = pfm.getLastPrices();

        LOG.info("\n\n\n ---------------------- Testing results for: " + pair.toStringSepSpecial("/"));
        LOG.info("Positive response from " + priceList.size() + "/" + pfm.getFeedList().size() + " feeds\n");
        for (int i = 0; i < priceList.size(); i++) {
            LastPrice tempPrice = priceList.get(i);
            LOG.info(tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
                    + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode());
        }
    }
}
