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

package com.nubits.nubot.global;

import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.testsmanual.TestPriceFeed;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * test feed via pfm
 */
public class TestPriceFeedsBatch extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestPriceFeedsBatch.class.getName());


    public void setup(){
        TestPriceFeed test = new TestPriceFeed();



        //feed = new BitcoinaveragePriceFeed();
        String folderName = "tests_" + System.currentTimeMillis() + "/";

        LOG.info("Set up SSL certificates");
        Utils.installKeystore(false);
    }


    @Test
    public void testBtce() {


    }


    private void execute(String mainFeed, ArrayList<String> backupFeedList, CurrencyPair pair) {

        PriceFeedManager pfm = null;
        try{
            pfm = new PriceFeedManager(mainFeed, backupFeedList, pair);
        }catch(NuBotConfigException e){

        }

        pfm.fetchLastPrices();
        ArrayList<LastPrice> priceList = pfm.getLastPrices();

        int s = pfm.getFeedList().size();
        int ps = priceList.size();
        assertTrue(s == ps);

        for (int i = 0; i < priceList.size(); i++) {
            LastPrice tempPrice = priceList.get(i);
          //  LOG.info(tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
            //        + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode());
        }
    }

}
