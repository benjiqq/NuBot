import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.BtcePriceFeed;
import junit.framework.TestCase;
import org.junit.Test;

public class TestPriceFeedBtce extends TestCase {

        @Test
        public void test() {
            CurrencyPair testPair = Constant.BTC_USD;

            BtcePriceFeed btce = new BtcePriceFeed();
            LastPrice lastprice = btce.getLastPrice(testPair);

            assertNotNull(lastprice);
            System.out.println(lastprice.getPrice());
            assertTrue(lastprice.getPrice().getQuantity()>0);

        }

    }