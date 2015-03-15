
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.pricefeeds.feedservices.BitfinexPriceFeed;
import com.nubits.nubot.pricefeeds.feedservices.BtcePriceFeed;
import com.nubits.nubot.utils.SSLConnectionTest;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class TestSSL extends TestCase {


    @Test
    public void testPolo() {

        //Load settings
        try {
            Utils.loadProperties("settings.properties");
        } catch (IOException e) {
            //System.exit(0);
            assertTrue(false);
        }


        Utils.installKeystore(false);

        String API_BASE_URL = "poloniex.com"; //"https://poloniex.com/tradingApi";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }

    @Test
    public void testBitspark() {

        //Load settings
        try {
            Utils.loadProperties("settings.properties");
        } catch (IOException e) {
            //System.exit(0);
            assertTrue(false);
        }


        Utils.installKeystore(false);

        String API_BASE_URL = "bitspark.io";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }
}