/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
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
package com.nubits.nubot.global;

import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import java.util.logging.Logger;

/**
 *
 * @author desrever < desrever@nubits.com >
 */
public class Constant {

    private static final Logger LOG = Logger.getLogger(Constant.class.getName());
    //Exchanges
    public static final String BTCE = "btce";
    public static final String CCEDK = "ccedk";
    public static final String BTER = "bter";
    public static final String INTERNAL_EXCHANGE_PEATIO = "peatio";
    public static final String BITSPARK_PEATIO = "bitspark";
    public static final String POLONIEX = "poloniex";
    public static final String CCEX = "ccex";
    public static final String ALLCOIN = "allcoin";
    public static final String EXCOIN = "excoin";
    //API base url for peatio instances
    public static final String INTERNAL_EXCHANGE_PEATIO_API_BASE = "http://178.62.186.229/";
    //Order types
    public static final String BUY = "BUY";
    public static final String SELL = "SELL";
    //Currencies
    public static final Currency USD = new Currency(true, "USD", "US Dollar");
    public static final Currency CNY = new Currency(true, "CNY", "CNY");
    public static final Currency EUR = new Currency(true, "EUR", "Euro");
    public static final Currency BTC = new Currency(false, "BTC", "Bitcoin");
    public static final Currency NBT = new Currency(false, "NBT", "Nubits");
    public static final Currency PPC = new Currency(false, "PPC", "Peercoin");
    public static final Currency LTC = new Currency(false, "LTC", "Litecoin");
    //!! When adding one here, also add it down
    public static final CurrencyPair NBT_USD = new CurrencyPair(NBT, USD);
    public static final CurrencyPair NBT_BTC = new CurrencyPair(NBT, BTC);
    public static final CurrencyPair BTC_NBT = new CurrencyPair(BTC, NBT);
    public static final CurrencyPair NBT_PPC = new CurrencyPair(NBT, PPC);
    public static final CurrencyPair NBT_EUR = new CurrencyPair(NBT, EUR);
    public static final CurrencyPair NBT_CNY = new CurrencyPair(NBT, CNY);
    public static final CurrencyPair BTC_USD = new CurrencyPair(BTC, USD);
    public static final CurrencyPair PPC_USD = new CurrencyPair(PPC, USD);
    public static final CurrencyPair PPC_BTC = new CurrencyPair(PPC, BTC);
    public static final CurrencyPair PPC_LTC = new CurrencyPair(PPC, LTC);
    public static final CurrencyPair BTC_CNY = new CurrencyPair(BTC, CNY);
    public static final CurrencyPair EUR_USD = new CurrencyPair(EUR, USD);
    public static final CurrencyPair CNY_USD = new CurrencyPair(CNY, USD);
    //Direction of price
    public static final String UP = "up";
    public static final String DOWN = "down";

    public static Currency getCurrencyFromCode(String codeString) {
        Currency toRet = null;
        codeString = codeString.toUpperCase();
        switch (codeString) {
            case "USD":
                toRet = USD;
                break;
            case "EUR":
                toRet = EUR;
                break;
            case "LTC":
                toRet = LTC;
                break;
            case "CNY":
                toRet = CNY;
                break;
            case "BTC":
                toRet = BTC;
                break;
            case "PPC":
                toRet = PPC;
                break;
            case "NBT":
                toRet = NBT;
                break;
            default:
                LOG.severe("Currency " + codeString + " not available");
                break;
        }

        return toRet;
    }
}
