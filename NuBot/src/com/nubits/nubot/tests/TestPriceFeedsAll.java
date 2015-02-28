import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.BitfinexPriceFeed;
import com.nubits.nubot.pricefeeds.BlockchainPriceFeed;
import com.nubits.nubot.pricefeeds.BtcePriceFeed;
import com.nubits.nubot.pricefeeds.CoinbasePriceFeed;
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

    public void setUp(){

    }

    @Test
    public void testZAvg() {

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