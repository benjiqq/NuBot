import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.BitfinexPriceFeed;
import com.nubits.nubot.pricefeeds.BlockchainPriceFeed;
import com.nubits.nubot.pricefeeds.BtcePriceFeed;
import com.nubits.nubot.pricefeeds.CoinbasePriceFeed;
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
