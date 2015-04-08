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

package testexchanges;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.testsmanual.WrapperTestUtils;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.utils.InitTests;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExchangePeatio extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestExchangePeatio.class
            .getName());


    private static String testconfigFile = "peatio.json";
    private static String testconfig = Settings.TESTS_CONFIG_PATH + "/" + testconfigFile;

    private CurrencyPair testPair = CurrencyList.NBT_BTC;

    @Test
    public void testPing() {

        TradeInterface ti = setupTI();

        Global.exchange.getLiveData().setConnected(true);

        ApiResponse balancesResponse = ti.getAvailableBalances(testPair);

        if (balancesResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance() ");
            PairBalance balance = (PairBalance) balancesResponse.getResponseObject();

            LOG.info(balance.toString());

            //assertTrue(balance.getNubitsBalance().getQuantity()==0.0);
            //assertTrue(balance.getPEGBalance().getQuantity()==1000.0);

        } else {
            assertTrue(false);
        }
    }

    private TradeInterface setupTI(){
        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(opt != null);

            assertTrue(opt.getExchangeName().equals("peatio"));

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }

        //Load keystore
        InitTests.loadKeystore(true);

        Global.options = opt;


        try{
            WrapperTestUtils.configureExchange(opt.getExchangeName());
        }catch(NuBotConfigException ex){

        }

        TradeInterface ti;// = ExchangeFacade.exchangeInterfaceSetup(Global.options);
        ti = Global.exchange.getTradeInterface();
        //Bug
        ti.setExchange(Global.exchange);

        assertTrue(ti != null);

        Currency btc = CurrencyList.BTC;
        Global.exchange.getLiveData().setConnected(true);
        return ti;
    }

    @Test
    public void testGetBalance() {

        TradeInterface ti = setupTI();

        long start = System.currentTimeMillis();
        ApiResponse balancesResponse = ti.getAvailableBalance(CurrencyList.BTC);
        assertTrue(balancesResponse!=null);
        long stop = System.currentTimeMillis();
        long delta = stop - start;
        assertTrue(delta < 5000);

        if (balancesResponse.isPositive()) {
            LOG.info("Positive response  from TradeInterface.getBalance() ");
            Object o = balancesResponse.getResponseObject();
            LOG.info("response " + o);
            try {
                Amount a = (Amount) o;
                assertTrue(a.getQuantity() >= 0);
            } catch (Exception e) {
                assertTrue(false);
            }
            //Balance balance = (Balance) o;

            //LOG.info(balance.toStringSep());

            //assertTrue(balance.getNubitsBalance().getQuantity() == 0.0);
            //assertTrue(balance.getPEGBalance().getQuantity() == 1000.0);

        } else {
            assertTrue(false);
        }
    }
}