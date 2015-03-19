import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.LastPrice;
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
        backup.add(BitfinexPriceFeed.name);
        PriceFeedManager pfm = null;
        try{
             pfm = new PriceFeedManager(BtcePriceFeed.name, backup, CurrencyList.BTC_USD);
        }catch(Exception e){

        }

        ArrayList<LastPrice> prices = pfm.fetchLastPrices().getPrices();
        Iterator<LastPrice> it = prices.iterator();
        while(it.hasNext()){
            LastPrice p = it.next();
            double pd = p.getPrice().getQuantity();
            System.out.println(pd);
        }
        //assertTrue(prices.size()==2);

    }
}