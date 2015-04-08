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

package com.nubits.nubot.testsmanual;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.utils.FrozenBalancesManager;
import com.nubits.nubot.utils.InitTests;
import com.nubits.nubot.utils.NuLog;
import com.nubits.nubot.utils.Utils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

public class TestFroozenAmounts {

    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestFroozenAmounts.class.getName());

    public static void main(String[] args) {
        InitTests.setLoggingFilename(LOG);

        CurrencyPair pair = CurrencyList.NBT_BTC;
        Currency currency = pair.getPaymentCurrency();
        String exchangeName = ExchangeFacade.BTER;

        FrozenBalancesManager fbm = new FrozenBalancesManager(exchangeName, pair);

        fbm.updateFrozenBalance(new Amount(0.000000091, currency));
        fbm.updateFrozenBalance(new Amount(5032, currency));
        fbm.updateFrozenBalance(new Amount(202, currency));
        fbm.updateFrozenBalance(new Amount(30.3, currency));
        fbm.updateFrozenBalance(new Amount(34.3, currency));
        fbm.updateFrozenBalance(new Amount(330.1233, currency));
        fbm.updateFrozenBalance(new Amount(1130.13, currency));
        fbm.updateFrozenBalance(new Amount(303.32342, currency));
        fbm.updateFrozenBalance(new Amount(30.3, currency));
        fbm.updateFrozenBalance(new Amount(1, currency));

        LOG.info("Loaded Froozen balance : " + fbm.getFrozenAmount().getAmount().getQuantity());

        fbm.updateFrozenBalance(new Amount(231.2, currency));

        LOG.info("then Froozen balance : " + fbm.getFrozenAmount().getAmount().getQuantity());


    }
}
