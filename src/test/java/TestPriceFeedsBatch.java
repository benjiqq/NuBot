package com.nubits.nubot.global;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.pricefeeds.*;
import com.nubits.nubot.testsmanual.TestPriceFeed;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * test feed via pfm
 */
public class TestPriceFeedsBatch extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestPriceFeedsBatch.class.getName());


    public void setup(){
        TestPriceFeed test = new TestPriceFeed();
        try{
            Utils.loadProperties("settings.properties");
        }catch(IOException e){

        }
        //feed = new BitcoinaveragePriceFeed();
        String folderName = "tests_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;

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

        PriceFeedManager.LastPriceResponse lpr = pfm.getLastPrices();

        ArrayList<LastPrice> priceList = pfm.getLastPrices().getPrices();

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
