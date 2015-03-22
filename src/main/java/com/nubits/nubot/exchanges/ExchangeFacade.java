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
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;
import com.nubits.nubot.trading.wrappers.BterWrapper;
import com.nubits.nubot.trading.wrappers.CcedkWrapper;
import com.nubits.nubot.trading.wrappers.ExcoinWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

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

    //API base url for peatio instances
    public static final String INTERNAL_EXCHANGE_PEATIO_API_BASE = "http://178.62.186.229/";   //Old
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

        exchangeInterfaces.put(POLONIEX,AltsTradeWrapper.class);
        exchangeInterfaces.put(POLONIEX,PoloniexWrapper.class);
        exchangeInterfaces.put(POLONIEX,CcedkWrapper.class);
        exchangeInterfaces.put(POLONIEX,AllCoinWrapper.class);
        exchangeInterfaces.put(POLONIEX,BitSparkWrapper.class);
        exchangeInterfaces.put(POLONIEX,BitcoinCoIDWrapper.class);
        exchangeInterfaces.put(POLONIEX,PeatioWrapper.class);
        exchangeInterfaces.put(POLONIEX,BtceWrapper.class);
        exchangeInterfaces.put(POLONIEX,CcedkWrapper.class);
        exchangeInterfaces.put(POLONIEX,ExcoinWrapper.class);


    }

    public static boolean supportedExchange(String exchange) {
        return supportedExchanges.contains(exchange);
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
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);
        return ti;
    }

    public static TradeInterface getInterfaceByName(String name, ApiKeys keys, Exchange exchange) {

        LOG.info("get exchange interface for " + name);

        if (exchangeInterfaces.containsKey(name)) {

            TradeInterface ti = null;
            try {

                Class<?> wrapperClazz = exchangeInterfaces.get(name);
                Constructor<?> constructor = wrapperClazz.getConstructor(ApiKeys.class, Exchange.class);
                Object instance = constructor.newInstance(keys, exchange);
                ti = (TradeInterface) instance;
                return ti;
            } catch (Exception e) {
                LOG.error("" + e);
            }
        }
        return null;
    }

    /**
     * simplified balance query. returns -1 on error
     *
     * @param currency
     * @return
     */
    public static double getBalance(TradeInterface ti, Currency currency) {
        ApiResponse balancesResponse = ti.getAvailableBalance(currency);
        if (balancesResponse.isPositive()) {
            Object o = balancesResponse.getResponseObject();
            try {
                Amount a = (Amount) o;
                return a.getQuantity();
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    public static ArrayList<Order> getOpenOrders(TradeInterface ti) {
        ApiResponse orderResponse = ti.getActiveOrders();
        if (orderResponse.isPositive()) {
            Object o = orderResponse.getResponseObject();
            try {
                ArrayList<Order> orders = (ArrayList<Order>) o;
                return orders;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return null;
    }
}
