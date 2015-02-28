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

public class TestPriceFeedsAll extends TestCase {

    double lastbtce, lastbf, lastbci, lastcb;
    ArrayList<Double> alllast;

    @Test
    public void testBtce() {

        CurrencyPair testPair = Constant.BTC_USD;

        BtcePriceFeed btce = new BtcePriceFeed();
        LastPrice lastprice = btce.getLastPrice(testPair);
        lastbtce = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastbtce > 0);

        alllast.add(lastbtce);

    }

    @Test
    public void testBitfinex() {

        CurrencyPair testPair = Constant.BTC_USD;

        BitfinexPriceFeed bf = new BitfinexPriceFeed();
        LastPrice lastprice = bf.getLastPrice(testPair);
        lastbf = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastbf > 0);

        alllast.add(lastbf);
    }

    @Test
    public void testBCI() {

        CurrencyPair testPair = Constant.BTC_USD;

        BlockchainPriceFeed bci = new BlockchainPriceFeed();
        LastPrice lastprice = bci.getLastPrice(testPair);
        lastbci = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastbci > 0);

        alllast.add(lastbci);
    }

    @Test
    public void testCoinbase() {

        CurrencyPair testPair = Constant.BTC_USD;

        CoinbasePriceFeed cb = new CoinbasePriceFeed();
        LastPrice lastprice = cb.getLastPrice(testPair);
        lastcb = lastprice.getPrice().getQuantity();
        assertNotNull(lastprice);
        assertTrue(lastcb > 0);

        alllast.add(lastcb);
    }

    @Test
    public void testAvg() {
        double sumlast = 0.0;
        double min = 0.0;
        double max = 9999999.9;
        int n = 0;
        for (Iterator<Double> it = alllast.iterator(); it.hasNext();) {
            double p = it.next();
            sumlast += p;

            if (p > max)
                max = p;

            if (p < min)
                min = p;

            n++;
        }


        assertTrue(sumlast > 0.0);
        //sum should be bigger than min * number of prices (n)
        assertTrue(sumlast > min*n);
        //same for max
        assertTrue(sumlast < max*n);



    }
}