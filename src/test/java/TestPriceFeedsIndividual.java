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

import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.feedservices.BitfinexPriceFeed;
import com.nubits.nubot.pricefeeds.feedservices.BlockchainPriceFeed;
import com.nubits.nubot.pricefeeds.feedservices.BtcePriceFeed;
import com.nubits.nubot.pricefeeds.feedservices.CoinbasePriceFeed;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * test each feed
 */
public class TestPriceFeedsIndividual extends TestCase {


    double lastbtce, lastbf, lastbci, lastcb;

    @Test
    public void testBtce() {

        CurrencyPair testPair = CurrencyList.BTC_USD;
        BtcePriceFeed btce = new BtcePriceFeed();
        LastPrice lastprice = btce.getLastPrice(testPair);
        lastbtce = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastbtce > 0);

    }

    @Test
    public void testBitfinex() {

        CurrencyPair testPair = CurrencyList.BTC_USD;

        BitfinexPriceFeed bf = new BitfinexPriceFeed();
        LastPrice lastprice = bf.getLastPrice(testPair);
        lastbf = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastbf > 0);

    }

    @Test
    public void testBCI() {

        CurrencyPair testPair = CurrencyList.BTC_USD;

        BlockchainPriceFeed bci = new BlockchainPriceFeed();
        LastPrice lastprice = bci.getLastPrice(testPair);
        lastbci = lastprice.getPrice().getQuantity();

    }

    @Test
    public void testCoinbase() {

        CurrencyPair testPair = CurrencyList.BTC_USD;

        CoinbasePriceFeed cb = new CoinbasePriceFeed();
        LastPrice lastprice = cb.getLastPrice(testPair);
        lastcb = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastcb > 0);

    }

}
