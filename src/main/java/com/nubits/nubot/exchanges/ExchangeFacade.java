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

package com.nubits.nubot.exchanges;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * a facade for all exchanges
 * Changes need to be made in 1. Strings, 2. supportedExchanges ArrayList and 3. exchangeInterfaces
 */
public class ExchangeFacade {

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeFacade.class.getName());


    public static final String ALTSTRADE = "Altstrade";
    public static final String BTCE = "Btce";
    public static final String CCEDK = "Ccedk";
    public static final String BTER = "Bter";
    public static final String INTERNAL_EXCHANGE_PEATIO = "Peatio";
    public static final String BITSPARK_PEATIO = "Bitspark";
    public static final String POLONIEX = "Poloniex";
    public static final String CCEX = "Ccex";
    public static final String ALLCOIN = "Allcoin";
    public static final String EXCOIN = "Excoin";
    public static final String BITCOINCOID = "Bitcoincoid";
    public static final String COMKORT = "Comkort";
    public static final String BITTREX = "Bittrex";

    //API base url for peatio instances
    public static final String INTERNAL_EXCHANGE_PEATIO_API_BASE = "https://178.62.140.24";
    private static ArrayList<String> supportedExchanges, liveExchanges;
    private static HashMap<String, Class> exchangeInterfaces;
    ;

    static {
        supportedExchanges = new ArrayList<>();
        exchangeInterfaces = new HashMap<>();

        supportedExchanges.add(ALTSTRADE);
        supportedExchanges.add(POLONIEX);
        supportedExchanges.add(CCEX);
        supportedExchanges.add(ALLCOIN);
        supportedExchanges.add(BITSPARK_PEATIO);
        supportedExchanges.add(BITCOINCOID);
        supportedExchanges.add(INTERNAL_EXCHANGE_PEATIO);
        supportedExchanges.add(BTCE);
        supportedExchanges.add(BTER);
        supportedExchanges.add(CCEDK);
        supportedExchanges.add(EXCOIN);
        supportedExchanges.add(COMKORT);
        supportedExchanges.add(BITTREX);

        exchangeInterfaces.put(ALTSTRADE, AltsTradeWrapper.class);
        exchangeInterfaces.put(POLONIEX, PoloniexWrapper.class);
        exchangeInterfaces.put(CCEX, CcexWrapper.class);
        exchangeInterfaces.put(ALLCOIN, AllCoinWrapper.class);
        exchangeInterfaces.put(BITSPARK_PEATIO, BitSparkWrapper.class);
        exchangeInterfaces.put(BITCOINCOID, BitcoinCoIDWrapper.class);
        exchangeInterfaces.put(INTERNAL_EXCHANGE_PEATIO, PeatioWrapper.class);
        exchangeInterfaces.put(BTCE, BtceWrapper.class);
        exchangeInterfaces.put(EXCOIN, ExcoinWrapper.class);
        exchangeInterfaces.put(COMKORT, ComkortWrapper.class);
        exchangeInterfaces.put(BITTREX, BittrexWrapper.class);
        exchangeInterfaces.put(BTER, BterWrapper.class);
        exchangeInterfaces.put(CCEDK, CcedkWrapper.class);


    }

    public static boolean supportedExchange(String exchange) {

        Iterator<String> iter = supportedExchanges.iterator();
        boolean contains = false;
        while (iter.hasNext()) {
            String key = iter.next();
            if (key.equalsIgnoreCase(exchange))
                return true;
        }

        return contains;

    }


    /**
     * set up interface based on options
     *
     * @param opt
     * @return
     */
    public static TradeInterface exchangeInterfaceSetup(NuBotOptions opt) {
        Global.exchange = new Exchange(Global.options.getExchangeName());
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());
        TradeInterface ti = ExchangeFacade.getInterfaceByName(Global.exchange.getName(), keys, Global.exchange);
        //TODO remove when tested
        //ti.setKeys(keys);
        //ti.setExchange(Global.exchange);
        return ti;
    }

    private static String capitalizieName(String name) {
        char firstChar = name.charAt(0);
        char fu = Character.toUpperCase(firstChar);
        name = fu + name.substring(1, name.length());
        return name;
    }

    public static TradeInterface getInterfaceByName(String name, ApiKeys keys, Exchange exchange) {

        LOG.debug("get exchange interface for " + name);

        if (supportedExchange(name)) {

            TradeInterface ti = null;
            try {

                name = capitalizieName(name);

                Class<?> wrapperClazz = exchangeInterfaces.get(name);

                Object instance = null;
                Constructor<?> constructor;
                if (name.equalsIgnoreCase(INTERNAL_EXCHANGE_PEATIO)) {
                    constructor = wrapperClazz.getConstructor(ApiKeys.class, Exchange.class, String.class);
                    instance = constructor.newInstance(keys, exchange, INTERNAL_EXCHANGE_PEATIO_API_BASE);
                } else {
                    constructor = wrapperClazz.getConstructor(ApiKeys.class, Exchange.class);
                    instance = constructor.newInstance(keys, exchange);
                }
                ti = (TradeInterface) instance;
                return ti;
            } catch (Exception e) {
                LOG.error("" + e);
            }
        }
        return null;
    }

}
