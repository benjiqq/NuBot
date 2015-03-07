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
package com.nubits.nubot.pricefeeds;

import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import com.nubits.nubot.options.NuBotConfigException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Manager for a list of price feeds
 */
public class PriceFeedManager {

    private static final Logger LOG = LoggerFactory.getLogger(PriceFeedManager.class.getName());
    private AbstractPriceFeed mainfeed;
    private ArrayList<AbstractPriceFeed> feedList = new ArrayList<>();
    private CurrencyPair pair;
    public static HashMap<String, AbstractPriceFeed> FEED_NAMES_MAP;

    /*public final static String BLOCKCHAIN = "blockchain"; //BTC
    public final static String BITCOINAVERAGE = "bitcoinaverage"; //BTC
    public final static String COINBASE = "coinbase"; //BTC
    public final static String BITSTAMP = "bitstamp"; // BTC
    public final static String BITFINEX = "bitfinex"; // BTC
    public final static String BTER = "bter"; //BTC and PPC
    public final static String CCEDK = "ccedk"; //BTC and PPC
    public final static String BTCE = ExchangeFacade.BTCE;
    public final static String COINMARKETCAP_NO = "coinmarketcap_no"; //PPC
    public final static String COINMARKETCAP_NE = "coinmarketcap_ne"; //PPC
    public final static String BITSTAMP_EURUSD = "bitstampeurusd"; // EUR
    public final static String GOOGLE_UNOFFICIAL = "google-unofficial"; // EUR CNY
    public final static String YAHOO = "yahoo"; //EUR CNY
    public final static String OPENEXCHANGERATES = "openexchangerates"; //EUR CNY
    public final static String EXCHANGERATELAB = "exchangeratelab"; // EUR CNY*/

    private final static String basepackage = "com.nubits.nubot.pricefeeds.";
    public final static String[] feedlclasses = {"BitcoinaveragePriceFeed", "BitcoinaveragePriceFeed",
            "CoinbasePriceFeed", "BterPriceFeed", "CcedkPriceFeed", "BtcePriceFeed",
            "CoinmarketcapnorthpolePriceFeed", "CoinmarketcapnexuistPriceFeed",
            "BitstampPriceFeed", "BitstampEURPriceFeed", "GoogleUnofficialPriceFeed",
            "YahooPriceFeed", "OpenexchangeratesPriceFeed", "BitfinexPriceFeed",
            "ExchangeratelabPriceFeed"};
    
    public PriceFeedManager(String mainFeed, ArrayList<String> backupFeedList, CurrencyPair pair) throws NuBotConfigException {
        initValidFeeds();
        this.pair = pair;

        feedList.add(getFeed(mainFeed)); //add the main feed at index 0
        //this.mainfeed = getFeed(mainFeed);

        for (int i = 0; i < backupFeedList.size(); i++) {
            feedList.add(getFeed(backupFeedList.get(i)));
        }
    }

    /**
     * init feeds based on the classes. classes contains the name
     */
    private void initValidFeeds() {

        FEED_NAMES_MAP = new HashMap<>();
        for (int i = 0; i < feedlclasses.length; i++ ) {
            try {
                Class<?> feedclass = Class.forName(basepackage + feedlclasses[i]);
                Constructor<?> feed = feedclass.getConstructor(String.class);
                Object object = feed.newInstance(new Object[]{});
                AbstractPriceFeed f = (AbstractPriceFeed) object;
                FEED_NAMES_MAP.put(f.getName(), f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public LastPriceResponse getLastPrices() {
        LastPriceResponse response = new LastPriceResponse();
        boolean isMainFeedValid = false;
        ArrayList<LastPrice> prices = new ArrayList<>();
        for (int i = 0; i < feedList.size(); i++) {
            AbstractPriceFeed tempFeed = feedList.get(i);

            LastPrice lastPrice = tempFeed.getLastPrice(pair);
            if (lastPrice != null) {
                if (!lastPrice.isError()) {
                    prices.add(lastPrice);
                    if (i == 0) {
                        isMainFeedValid = true;
                    }
                } else {
                    LOG.warn("Error while updating " + pair.getOrderCurrency().getCode() + ""
                            + " price from " + tempFeed.name);
                }
            } else {
                LOG.warn("Error (null) while updating " + pair.getOrderCurrency().getCode() + ""
                        + " price from " + tempFeed.name);
            }
        }
        response.setMainFeedValid(isMainFeedValid);
        response.setPrices(prices);
        return response;
    }

    public LastPrice getLastPrice() {
        boolean ok = false;

        for (int i = 0; i < feedList.size(); i++) {
            AbstractPriceFeed tempFeed = feedList.get(i);
            LastPrice lastPrice = tempFeed.getLastPrice(pair);
            if (!lastPrice.isError()) {
                LOG.info("Got last price of 1" + pair.getOrderCurrency().getCode() + ""
                        + " from " + tempFeed.name + " : " + lastPrice.getPrice().getQuantity() + " " + lastPrice.getPrice().getCurrency().getCode());
                return lastPrice;
            } else {
                //handle error
                LOG.error("Problem while updating the price on " + tempFeed.name);
            }
        }

        //None of them worked. Caution now
        return new LastPrice(true, "", pair.getOrderCurrency(), null);

    }

    private AbstractPriceFeed getFeed(String feedname) throws NuBotConfigException {
        if (FEED_NAMES_MAP.containsKey(feedname)) {
            return FEED_NAMES_MAP.get(feedname);
        } else {
            throw new NuBotConfigException("Error wile adding price seed with name unrecognized : " + feedname);
        }

    }

    public ArrayList<AbstractPriceFeed> getFeedList() {
        return feedList;
    }

    public void setFeedList(ArrayList<AbstractPriceFeed> feedList) {
        this.feedList = feedList;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public void setPair(CurrencyPair pair) {
        this.pair = pair;
    }

    /**
     * class to wrap results from getLastPrices
     */
    public class LastPriceResponse {

        private boolean mainFeedValid;
        private ArrayList<LastPrice> prices;

        public LastPriceResponse() {
        }

        public boolean isMainFeedValid() {
            return mainFeedValid;
        }

        public void setMainFeedValid(boolean mainFeedValid) {
            this.mainFeedValid = mainFeedValid;
        }

        public ArrayList<LastPrice> getPrices() {
            return prices;
        }

        public void setPrices(ArrayList<LastPrice> prices) {
            this.prices = prices;
        }
    }
}
