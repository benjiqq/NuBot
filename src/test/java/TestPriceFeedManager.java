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

import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.FeedFacade;
import com.nubits.nubot.pricefeeds.feedservices.BitfinexPriceFeed;
import com.nubits.nubot.pricefeeds.feedservices.BtcePriceFeed;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;


public class TestPriceFeedManager extends TestCase {


    @Test
    public void testAll() {

        //public PriceFeedManager(String mainFeed, ArrayList<String> backupFeedList, CurrencyPair pair) throws
        ArrayList<String>backup = new ArrayList<>();
        backup.add(FeedFacade.BitfinexPriceFeed);
        PriceFeedManager pfm = null;
        try{
             pfm = new PriceFeedManager(FeedFacade.BtcePriceFeed, backup, CurrencyList.BTC_USD);
        }catch(Exception e){

        }

        pfm.fetchLastPrices();
        ArrayList<LastPrice> prices = pfm.getLastPrices();
        Iterator<LastPrice> it = prices.iterator();
        while(it.hasNext()){
            LastPrice p = it.next();
            double pd = p.getPrice().getQuantity();
            System.out.println(pd);
        }
        //assertTrue(prices.size()==2);

    }
}