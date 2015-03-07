package com.nubits.nubot.pricefeeds;

import com.nubits.nubot.options.NuBotConfigException;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * all feeds
 */
public class Feeds {

    private final static String basepackage = "com.nubits.nubot.pricefeeds.";
    public final static String[] feedlclasses = {"BitcoinaveragePriceFeed", "BitcoinaveragePriceFeed",
            "CoinbasePriceFeed", "BterPriceFeed", "CcedkPriceFeed", "BtcePriceFeed",
            "CoinmarketcapnorthpolePriceFeed", "CoinmarketcapnexuistPriceFeed",
            "BitstampPriceFeed", "BitstampEURPriceFeed", "GoogleUnofficialPriceFeed",
            "YahooPriceFeed", "OpenexchangeratesPriceFeed", "BitfinexPriceFeed",
            "ExchangeratelabPriceFeed"};

    public static HashMap<String, AbstractPriceFeed> FEED_NAMES_MAP;

    /**
     * init feeds based on the classes. classes contains the name
     */
    public static void initValidFeeds() {

        FEED_NAMES_MAP = new HashMap<>();
        for (int i = 0; i < feedlclasses.length; i++) {
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

    public static AbstractPriceFeed getFeed(String feedname) throws NuBotConfigException {
        if (FEED_NAMES_MAP.containsKey(feedname)) {
            return FEED_NAMES_MAP.get(feedname);
        } else {
            throw new NuBotConfigException("Error wile adding price seed with name unrecognized : " + feedname);
        }

    }

    public static ArrayList<AbstractPriceFeed> getAllExistingFeeds() {

        ArrayList<AbstractPriceFeed> list = new ArrayList<>();

        Iterator<AbstractPriceFeed> it = FEED_NAMES_MAP.values().iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

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

}
