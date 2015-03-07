import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.AbstractPriceFeed;
import com.nubits.nubot.pricefeeds.BtcePriceFeed;
import com.nubits.nubot.pricefeeds.Feeds;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
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

        CurrencyPair testPair = Constant.BTC_USD;

        ArrayList<AbstractPriceFeed> allfeeds = Feeds.getAllExistingFeeds();

        Iterator<AbstractPriceFeed> it = allfeeds.iterator();
        while (it.hasNext()){
            AbstractPriceFeed feed = it.next();
            LastPrice lastprice = feed.getLastPrice(testPair);
            assertNotNull(lastprice);
            double ld = lastprice.getPrice().getQuantity();
            assertTrue(ld > 0);
        }





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