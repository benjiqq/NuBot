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

package testexchanges;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.testsmanual.WrapperTestUtils;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

public class TestExchangePeatio extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestExchangePeatio.class
            .getName());


    private static String testconfigFile = "peatio.json";
    private static String testconfigdir = "config/testconfig";
    private static String testconfig = testconfigdir + "/" + testconfigFile;

    @Test
    public void testPing() {

        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(opt != null);

            assertTrue(opt.getExchangeName().equals("peatio"));

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }

        Global.options = opt;

        CurrencyPair testPair = CurrencyList.NBT_BTC;

        try{
            WrapperTestUtils.configExchange(opt.getExchangeName());
        }catch(NuBotConfigException ex){

        }

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(testPair);


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

    @Test
    public void testGetBalance() {

        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(opt != null);

            assertTrue(opt.getExchangeName().equals("peatio"));

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }

        Global.options = opt;

        CurrencyPair testPair = CurrencyList.NBT_BTC;

        try{
            WrapperTestUtils.configExchange(opt.getExchangeName());
        }catch(NuBotConfigException ex){

        }


        System.out.println("get balance");

        try{
            Utils.loadProperties("settings.properties");
        }catch(IOException e){

        }

        try{
            WrapperTestUtils.configExchangeSimple(opt.getExchangeName());
        }catch(NuBotConfigException ex){

        }

        TradeInterface ti = ExchangeFacade.exchangeInterfaceSetup(Global.options);
        ti = Global.exchange.getTradeInterface();
        //Bug
        ti.setExchange(Global.exchange);

        assertTrue(ti != null);

        Currency btc = CurrencyList.BTC;
        Global.exchange.getLiveData().setConnected(true);
        ExchangeLiveData ld = Global.exchange.getLiveData();

        assertTrue(ld != null);
        long start = System.currentTimeMillis();
        ApiResponse balancesResponse = ti.getAvailableBalance(btc);
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
