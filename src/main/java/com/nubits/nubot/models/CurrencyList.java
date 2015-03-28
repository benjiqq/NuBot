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

package com.nubits.nubot.models;


public class CurrencyList {

    public static Currency USD;
    public static Currency CNY;
    public static Currency EUR;
    public static Currency PHP;
    public static Currency HKD;
    public static Currency BTC;
    public static Currency NBT;
    public static Currency NSR;
    public static Currency PPC;
    public static Currency LTC;
    public static CurrencyPair NBT_USD;
    public static CurrencyPair NBT_BTC;
    public static CurrencyPair BTC_NBT;
    public static CurrencyPair NBT_PPC;
    public static CurrencyPair NBT_EUR;
    public static CurrencyPair NBT_CNY;
    public static CurrencyPair BTC_USD;
    public static CurrencyPair PPC_USD;
    public static CurrencyPair PPC_BTC;
    public static CurrencyPair PPC_LTC;
    public static CurrencyPair BTC_CNY;
    public static CurrencyPair EUR_USD;
    public static CurrencyPair CNY_USD;
    public static CurrencyPair PHP_USD;
    public static CurrencyPair HKD_USD;

    static {
        USD = Currency.createCurrency("USD");
        CNY = Currency.createCurrency("CNY");
        EUR = Currency.createCurrency("EUR");
        PHP = Currency.createCurrency("PHP");
        HKD = Currency.createCurrency("HKD");
        BTC = Currency.createCurrency("BTC");
        NBT = Currency.createCurrency("NBT");
        NSR = Currency.createCurrency("NSR");
        PPC = Currency.createCurrency("PPC");
        LTC = Currency.createCurrency("LTC");
        NBT_USD = new CurrencyPair(NBT, USD);
        BTC_USD = new CurrencyPair(BTC, USD);
        NBT_BTC = new CurrencyPair(NBT, BTC);
        BTC_NBT = new CurrencyPair(BTC, NBT);
        NBT_PPC = new CurrencyPair(NBT, PPC);
        NBT_EUR = new CurrencyPair(NBT, EUR);
        NBT_CNY = new CurrencyPair(NBT, CNY);
        BTC_USD = new CurrencyPair(BTC, USD);
        PPC_USD = new CurrencyPair(PPC, USD);
        PPC_BTC = new CurrencyPair(PPC, BTC);
        PPC_LTC = new CurrencyPair(PPC, LTC);
        BTC_CNY = new CurrencyPair(BTC, CNY);
        EUR_USD = new CurrencyPair(EUR, USD);
        CNY_USD = new CurrencyPair(CNY, USD);
        PHP_USD = new CurrencyPair(PHP, USD);
        HKD_USD = new CurrencyPair(HKD, USD);
    }



}
