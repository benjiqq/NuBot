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

package com.nubits.nubot.exchanges;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * a facade for all exchanges
 */
public class ExchangeFacade {

    public static final String ALTSTRADE = "altstrade";
    public static final String BTCE = "btce";
    public static final String CCEDK = "ccedk";
    public static final String BTER = "bter";
    public static final String INTERNAL_EXCHANGE_PEATIO = "peatio";
    public static final String BITSPARK_PEATIO = "bitspark";
    public static final String POLONIEX = "poloniex";
    public static final String CCEX = "ccex";
    public static final String ALLCOIN = "allcoin";
    public static final String EXCOIN = "excoin";
    public static final String BITCOINCOID = "bitcoincoid";
    public static final String BITTREX = "bittrex";

    //API base url for peatio instances
    public static final String INTERNAL_EXCHANGE_PEATIO_API_BASE = "http://178.62.186.229/";   //Old
    private static HashMap<String, TradeInterface> supportedExchanges, liveExchanges;

    static{
        supportedExchanges = new HashMap<>();

        supportedExchanges.put(ALTSTRADE, new AltsTradeWrapper());
        supportedExchanges.put(POLONIEX, new PoloniexWrapper());
        supportedExchanges.put(CCEX, new CcexWrapper());
        supportedExchanges.put(ALLCOIN, new AllCoinWrapper());
        supportedExchanges.put(BITSPARK_PEATIO, new BitSparkWrapper());
        supportedExchanges.put(BITCOINCOID, new BitcoinCoIDWrapper());
        supportedExchanges.put(INTERNAL_EXCHANGE_PEATIO, new PeatioWrapper());
        supportedExchanges.put(BTCE, new BtceWrapper());

        supportedExchanges.put(BTER, new BterWrapper());
        supportedExchanges.put(CCEDK, new CcedkWrapper());
        supportedExchanges.put(EXCOIN, new ExcoinWrapper());
    }

    static {
        liveExchanges = new HashMap<>();

        liveExchanges.put(ALTSTRADE, new AltsTradeWrapper());
        liveExchanges.put(POLONIEX, new PoloniexWrapper());
        liveExchanges.put(CCEX, new CcexWrapper());
        liveExchanges.put(ALLCOIN, new AllCoinWrapper());
        liveExchanges.put(BITSPARK_PEATIO, new BitSparkWrapper());
        liveExchanges.put(BITCOINCOID, new BitcoinCoIDWrapper());
        liveExchanges.put(INTERNAL_EXCHANGE_PEATIO, new PeatioWrapper());
        liveExchanges.put(BTCE, new BtceWrapper());
    }

    public static boolean exchangeSupported(String exchange){
        return supportedExchanges.containsKey(exchange);
    }


    public static boolean exchangeIsLive(String exchangename) {
        return liveExchanges.containsKey(exchangename);
    }

    /**
     * set up interface based on options
     * @param opt
     * @return
     */
    public static TradeInterface exchangeInterfaceSetup(NuBotOptions opt) {
        Global.exchange = new Exchange(Global.options.getExchangeName());
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());
        TradeInterface ti = ExchangeFacade.getInterface(Global.exchange);
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);
        return ti;
    }

    public static TradeInterface getInterface(Exchange exc){
        return getInterfaceByName(exc.getName());
    }

    public static TradeInterface getInterfaceByName(String name){


        if (supportedExchanges.containsKey(name)) {
            return supportedExchanges.get(name);
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
                ArrayList<Order> orders = (ArrayList<Order>)o;
                return orders;
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return null;
    }
}
