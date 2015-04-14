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

package com.nubits.nubot.testsmanual;



import com.nubits.nubot.NTP.NTPClient;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.pricefeeds.*;
import com.nubits.nubot.pricefeeds.feedservices.*;
import com.nubits.nubot.utils.InitTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TestSync extends TimerTask {

    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestSync.class.getName());
    private static final int TASK_INTERVAL = 61;
    private static final int TASK_MAX_EXECUTION_INTERVAL = 50;
    private static String id;
    private static int startTime;
    private static CurrencyPair pair = CurrencyList.BTC_USD;
    private static AbstractPriceFeed feed;

    public static void main(String[] args) throws InterruptedException {
        InitTests.setLoggingFilename(LOG);

        //Run multiple instance of this test to see if they read the same price.
        //It sends a notification on hipchat after syncing with a remote time server
        //Change parameters above

        startTime = (int) (System.currentTimeMillis() / 1000);
        System.out.println("Start-time = " + startTime);
        id = UUID.randomUUID().toString();

        init();

        message("Started");
        //Random sleep + 10 seconds
        int rand = 10 + (int) Math.round(Math.random() * 10);
        Thread.sleep(rand * 1000);

        //Read remote date
        message("Reading remote time");
        Date remoteDate = new NTPClient().getTime();
        Calendar remoteCalendar = new GregorianCalendar();
        remoteCalendar.setTime(remoteDate);

        //Compute the delay
        message("Computing delay");

        int remoteTimeInSeconds = remoteCalendar.get(Calendar.SECOND);
        message("Remote time in sec = " + remoteTimeInSeconds);
        int delay = (60 - remoteTimeInSeconds);
        message("Delay = " + delay + "  s");

        //Construct and use a TimerTask and Timer.
        TimerTask testSync = new TestSync();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(testSync, delay * 1000, TASK_INTERVAL * 1000);
        message("Timer scheduled");
    }

    private static void message(String msg) {
        System.out.println(getIdString() + msg);
    }

    private static String getIdString() {
        int now = (int) (System.currentTimeMillis() / 1000);
        int secondsFromStart = now - startTime;
        return id.substring(id.lastIndexOf("-") + 10) + " , t=" + secondsFromStart + "     - ";
    }

    private static void init() {
        //feed = new BitcoinaveragePriceFeed();
        String folderName = "tests_" + System.currentTimeMillis() + "/";

        InitTests.loadKeystore(true);

    }

    @Override
    public void run() {
        //Send hipchat notification
        message("Run");

        HipChatNotifications.sendMessageCritical(getIdString() + " test price reading : 1BTC =" + readPrice()
                + "$ ");

        //Add a random sleep after the notification to see if the keep sync

        int rand = (int) Math.round(Math.random() * TASK_MAX_EXECUTION_INTERVAL);
        try {
            Thread.sleep(rand * 1000);
        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage());
        }

    }

    private double readPrice() {

        String mainFeed = null;
        ArrayList<String> backupFeedList = new ArrayList<>();

        try{
            mainFeed = FeedFacade.BtcePriceFeed;
            String  f1 = FeedFacade.BitcoinaveragePriceFeed;
            String f2 = FeedFacade.BlockchainPriceFeed;
            String f3 = FeedFacade.CoinbasePriceFeed;
            backupFeedList.add(f1);
            backupFeedList.add(f2);
            backupFeedList.add(f3);
        }catch(Exception e){

        }

        PriceFeedManager pfm = null;
        try{
            pfm = new PriceFeedManager(mainFeed, backupFeedList, pair);
        }catch(NuBotConfigException e){

        }

        ArrayList<LastPrice> priceList = pfm.fetchLastPrices().getPrices();

        return priceList.get(0).getPrice().getQuantity();

    }
}
