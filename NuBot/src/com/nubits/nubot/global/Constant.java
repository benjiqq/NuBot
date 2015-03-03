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
package com.nubits.nubot.global;

import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import org.slf4j.LoggerFactory; import org.slf4j.Logger;

/**
 *
 * @author desrever < desrever@nubits.com >
 */
public class Constant {

    private static final Logger LOG = LoggerFactory.getLogger(Constant.class.getName());
    //public static final String INTERNAL_EXCHANGE_PEATIO_API_BASE = "http://178.62.140.24/";  //Casa di nu
    public static final String BUY = "BUY";
    public static final String SELL = "SELL";
    //Pairs
    public static final CurrencyPair NBT_USD = new CurrencyPair(CurrencyList.NBT, CurrencyList.USD);
    public static final CurrencyPair NBT_BTC = new CurrencyPair(CurrencyList.NBT, CurrencyList.BTC);
    public static final CurrencyPair BTC_NBT = new CurrencyPair(CurrencyList.BTC, CurrencyList.NBT);
    public static final CurrencyPair NBT_PPC = new CurrencyPair(CurrencyList.NBT, CurrencyList.PPC);
    public static final CurrencyPair NBT_EUR = new CurrencyPair(CurrencyList.NBT, CurrencyList.EUR);
    public static final CurrencyPair NBT_CNY = new CurrencyPair(CurrencyList.NBT, CurrencyList.CNY);
    public static final CurrencyPair BTC_USD = new CurrencyPair(CurrencyList.BTC, CurrencyList.USD);
    public static final CurrencyPair PPC_USD = new CurrencyPair(CurrencyList.PPC, CurrencyList.USD);
    public static final CurrencyPair PPC_BTC = new CurrencyPair(CurrencyList.PPC, CurrencyList.BTC);
    public static final CurrencyPair PPC_LTC = new CurrencyPair(CurrencyList.PPC, CurrencyList.LTC);
    public static final CurrencyPair BTC_CNY = new CurrencyPair(CurrencyList.BTC, CurrencyList.CNY);
    public static final CurrencyPair EUR_USD = new CurrencyPair(CurrencyList.EUR, CurrencyList.USD);
    public static final CurrencyPair CNY_USD = new CurrencyPair(CurrencyList.CNY, CurrencyList.USD);
    public static final CurrencyPair PHP_USD = new CurrencyPair(CurrencyList.PHP, CurrencyList.USD);
    public static final CurrencyPair HKD_USD = new CurrencyPair(CurrencyList.HKD, CurrencyList.USD);
    //Direction of price
    public static final String UP = "up";
    public static final String DOWN = "down";
}
