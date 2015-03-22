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

    public static HashMap<String, AbstractPriceFeed> FEED_NAMES_MAP;

    public static final String BitcoinaveragePriceFeed = "bitcoinaverage";
    public static final String CoinbasePriceFeed = "coinbase";
    public static final String BlockchainPriceFeed = "blockchain";
    public static final String BterPriceFeed = "bter";
    public static final String CcedkPriceFeed = "ccedk";

    public static final String CoinmarketcapnexuistPriceFeed = "coinmarketcap_no";
    public static final String CoinmarketcapnorthpolePriceFeed = "coinmarketcap_ne";
    public static final String BitstampPriceFeed = "bitstamp";
    public static final String YahooPriceFeed = "yahoo";
    public static final String BitfinexPriceFeed = "bitfinex";
    public static final String BtcePriceFeed = "btce";

    public static final String BitstampEURPriceFeed = "BitstampEUR";
    public static final String ExchangeratelabPriceFeed = "Exchangeratelab";
    public static final String GoogleUnofficialPriceFeed = "GoogleUnofficialPrice";
    public static final String OpenexchangeratesPriceFeed = "Openexchangerates";


    static {
        FEED_NAMES_MAP = new HashMap<>();
        FEED_NAMES_MAP.put(BitcoinaveragePriceFeed, new BitcoinaveragePriceFeed());
        FEED_NAMES_MAP.put(CoinbasePriceFeed, new CoinbasePriceFeed());
        FEED_NAMES_MAP.put(BlockchainPriceFeed, new BitcoinaveragePriceFeed());
        FEED_NAMES_MAP.put(BterPriceFeed, new BterPriceFeed());
        FEED_NAMES_MAP.put(CcedkPriceFeed, new CcedkPriceFeed());
        FEED_NAMES_MAP.put(BtcePriceFeed, new BtcePriceFeed());
        FEED_NAMES_MAP.put(CoinmarketcapnorthpolePriceFeed, new CoinmarketcapnorthpolePriceFeed());
        FEED_NAMES_MAP.put(CoinmarketcapnexuistPriceFeed, new CoinmarketcapnexuistPriceFeed());
        FEED_NAMES_MAP.put(BitstampPriceFeed, new BitstampPriceFeed());
        FEED_NAMES_MAP.put(YahooPriceFeed, new YahooPriceFeed());
        FEED_NAMES_MAP.put(BitfinexPriceFeed, new BitfinexPriceFeed());
    }

    public static AbstractPriceFeed getFeed(String feedname) throws NuBotConfigException {
        if (FEED_NAMES_MAP.containsKey(feedname)) {
            return FEED_NAMES_MAP.get(feedname);
        } else {
            throw new NuBotConfigException("Error wile adding price seed with name unrecognized : " + feedname);
        }

    }

    public static boolean isValidFeed(String feedname) {
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
