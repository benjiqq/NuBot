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
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.FeedFacade;
import com.nubits.nubot.pricefeeds.feedservices.AbstractPriceFeed;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * test feed in aggregated
 */
public class TestPriceFeedsAll extends TestCase {

    List<Double> alllast;
    //private String dbfile = "testprices.db";

    /*public void setUp(){

        File ff = new File(dbfile);
        if (ff.exists()) {
            ff.delete();
        }


    }*/

    @Test
    public void testAll() {

        CurrencyPair testPair = CurrencyList.BTC_USD;
        ArrayList<AbstractPriceFeed> allfeeds = FeedFacade.getAllExistingFeeds();

        int fn = allfeeds.size();

        Iterator<AbstractPriceFeed> it = allfeeds.iterator();
        System.out.println("query feeds " + allfeeds.size());
        assert (allfeeds.size() > 0);

        double sum = 0.0;
        double min = 9999.9;
        double max = 0.0;
        int n = 0;
        int fails = 0;

        while (it.hasNext()) {

            AbstractPriceFeed feed = it.next();
            System.out.println("query feed " + feed);
            LastPrice lastprice = feed.getLastPrice(testPair);


            assertNotNull(lastprice);

            try {
                double ld = lastprice.getPrice().getQuantity();
                System.out.println("price: " + ld);

                sum += ld;

                if (ld > max)
                    max = ld;

                if (ld < min)
                    min = ld;

                n++;
            } catch (Exception e) {
                fails++;
            }
            //double ld = lastprice.getPrice().getQuantity();
            //System.out.println(">>> " + feed.getName() + ": " + ld);
            //assertTrue(ld > 0);
        }

        System.out.println("feed n: " + fn);
        System.out.println("succes: " + n);
        System.out.println("fails: " + fails);

        assertTrue(fails <= 4);

        double avg = sum / n;

        assertTrue(avg < sum);
        assertTrue(avg > min);
        assertTrue(avg < max);



       /* alllast.add(lastbtce);
        alllast.add(lastbci);
        alllast.add(lastbf);
        alllast.add(lastcb);

        assertTrue(alllast.size()==4);

        double sumlast = 0.0;
        double min = 0.0;
        double max = 9999999.9;
        int n = 0;

        System.out.println("n: " + n);
        for (Iterator<Double> it = alllast.iterator(); it.hasNext();) {
            double p = it.next();
            System.out.println("p: " + p);
            sumlast += p;

            if (p > max)
                max = p;

            if (p < min)
                min = p;

            n++;
        }

        System.out.println("sum: " + sumlast);

        assertTrue(sumlast > 0.0);
        //sum should be bigger than min * number of prices (n)
        assertTrue(sumlast > min*n);
        //same for max
        assertTrue(sumlast < max*n);
*/


    }
}