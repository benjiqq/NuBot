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

package com.nubits.nubot.pricefeeds;

import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.pricefeeds.feedservices.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * all feeds
 */
public class FeedFacade {

    private final static String basepackage = "com.nubits.nubot.pricefeeds.";

    /*public final static String[] feedlclasses = {"BitcoinaveragePriceFeed",
            "CoinbasePriceFeed", "BterPriceFeed", "CcedkPriceFeed", "BtcePriceFeed",
            "CoinmarketcapnorthpolePriceFeed", "CoinmarketcapnexuistPriceFeed",
            "BitstampPriceFeed", "BitstampEURPriceFeed", "GoogleUnofficialPriceFeed",
            "YahooPriceFeed", "OpenexchangeratesPriceFeed", "BitfinexPriceFeed",
            "ExchangeratelabPriceFeed"};*/

    public static HashMap<String, AbstractPriceFeed> FEED_NAMES_MAP;

    static {
        FEED_NAMES_MAP =new HashMap<>();
        FEED_NAMES_MAP.put(BitcoinaveragePriceFeed.name, new BitcoinaveragePriceFeed());
        FEED_NAMES_MAP.put(CoinbasePriceFeed.name, new CoinbasePriceFeed());
        FEED_NAMES_MAP.put(BlockchainPriceFeed.name, new BitcoinaveragePriceFeed());
        FEED_NAMES_MAP.put(BterPriceFeed.name, new BterPriceFeed());
        FEED_NAMES_MAP.put(CcedkPriceFeed.name, new CcedkPriceFeed());
        FEED_NAMES_MAP.put(BtcePriceFeed.name, new BtcePriceFeed());
        FEED_NAMES_MAP.put(CoinmarketcapnorthpolePriceFeed.name, new CoinmarketcapnorthpolePriceFeed());
        FEED_NAMES_MAP.put(CoinmarketcapnexuistPriceFeed.name, new CoinmarketcapnexuistPriceFeed());
        FEED_NAMES_MAP.put(BitstampPriceFeed.name, new BitstampPriceFeed());
        FEED_NAMES_MAP.put(YahooPriceFeed.name, new YahooPriceFeed());
        FEED_NAMES_MAP.put(BitfinexPriceFeed.name, new BitfinexPriceFeed());
    }

    /**
     * init feeds based on the classes. classes contains the name
     */
    public static void initValidFeeds() {


        //FAILING
        //FEED_NAMES_MAP.put(OpenexchangeratesPriceFeed.name, new OpenexchangeratesPriceFeed());
        //FEED_NAMES_MAP.put(GoogleUnofficialPriceFeed.name, new GoogleUnofficialPriceFeed());
        //FEED_NAMES_MAP.put(ExchangeratelabPriceFeed.name, new ExchangeratelabPriceFeed());


        //FEED_NAMES_MAP.put(BitstampEURPriceFeed.name, new BitstampEURPriceFeed());


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


        //with reflection
        /*for (int i = 0; i < feedlclasses.length; i++) {
            try {
                String classname = basepackage + feedlclasses[i];
                Class<?> feedclass = Class.forName(classname);
                <?> feed = feedclass.getConstructor(String.class);
                //Object object = feed.newInstance(new Object[]{});
                Object obj = feedclass.newInstance();
                AbstractPriceFeed f = (AbstractPriceFeed) obj;
                System.out.println(classname + " ::: " + f);
                FEED_NAMES_MAP.put(f.getName(), f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

    }

    public static AbstractPriceFeed getFeed(String feedname) throws NuBotConfigException {
        if (FEED_NAMES_MAP.containsKey(feedname)) {
            return FEED_NAMES_MAP.get(feedname);
        } else {
            throw new NuBotConfigException("Error wile adding price seed with name unrecognized : " + feedname);
        }

    }

    public static boolean isValidFeed(String feedname){
        return FEED_NAMES_MAP.containsKey(feedname);
    }

    public static ArrayList<AbstractPriceFeed> getAllExistingFeeds() {

        ArrayList<AbstractPriceFeed> list = new ArrayList<>();

        Iterator<AbstractPriceFeed> it = FEED_NAMES_MAP.values().iterator();
        while (it.hasNext()) {
            AbstractPriceFeed f = it.next();
            list.add(f);
        }

        return list;
    }


}
