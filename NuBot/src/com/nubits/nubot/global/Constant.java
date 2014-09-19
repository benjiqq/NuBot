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
    public static final String PEATIO_BTCCNY = "peatio_btccny";
    public static final String PEATIO_MULTIPAIR = "peatio_multipair";
    //API base url for peatio instances
    public static final String PEATIO_MULTIPAIR_API_BASE = "http://198.52.199.61/"; //TODO UPDATE WHEN AVAILABLE
    public static final String PEATIO_BTCCNY_API_BASE = "http://198.52.199.61/";
    //UI components
    public static final String MAIN_DASHBOARD = "main-dasbhboard";
    public static final String OPTIONS = "options-dasbhboard";
    //Order types
    public static final String BUY = "BUY";
    public static final String SELL = "SELL";
    //Currencies
    public static final Currency USD = new Currency("$", true, "USD", "US Dollars");
    public static final Currency CNY = new Currency("C", true, "CNY", "CNY");
    public static final Currency BTC = new Currency("B", false, "BTC", "Bitcoins");
    public static final Currency NBT = new Currency("N", false, "NBT", "Nubits");
    public static final Currency PPC = new Currency("P", false, "PPC", "Peercoin");
    //!! When adding one here, also add it down
    public static final CurrencyPair NBT_USD = new CurrencyPair(NBT, USD);
    public static final CurrencyPair NBT_BTC = new CurrencyPair(NBT, BTC);
    public static final CurrencyPair NBT_PPC = new CurrencyPair(NBT, PPC);
    public static final CurrencyPair BTC_USD = new CurrencyPair(BTC, USD);
    public static final CurrencyPair PPC_USD = new CurrencyPair(PPC, USD);
    public static final CurrencyPair PPC_BTC = new CurrencyPair(PPC, BTC);
    public static final CurrencyPair BTC_CNY = new CurrencyPair(BTC, CNY);

    public static Currency getCurrencyFromCode(String codeString) {
        Currency toRet = null;
        codeString = codeString.toUpperCase();
        switch (codeString) {
            case "USD":
                toRet = USD;
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
