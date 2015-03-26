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

package com.nubits.nubot.tasks;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.NuBotConnectionException;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.BidAskPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.strategy.Secondary.StrategySecondaryPegTask;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

/**
 * A task for monitoring prices and triggering actions
 */
public class PriceMonitorTriggerTask extends TimerTask {

    private double wallchangeThreshold;

    private double sellPriceUSD, buyPriceUSD;
    private String pegPriceDirection;
    private LastPrice currentWallPEGPrice;

    private boolean wallsBeingShifted = false;

    private BidAskPair bidask;

    protected PriceFeedManager pfm = null;

    //set up a Queue to hold the prices used to calculate the moving average of prices
    protected Queue<Double> queueMA = new LinkedList<>();

    private final int MOVING_AVERAGE_SIZE = 30; //this is how many elements the Moving average queue holds


    /**
     * threshold for signaling a deviation of prices
     */
    private final double DISTANCE_TRESHHOLD = 10;

    protected LastPrice lastPrice;
    protected ArrayList<LastPrice> lastPrices;

    //options

    private static final int REFRESH_OFFSET = 1000; //this is how close to the refresh interval is considered a fail (millisecond)
    private static final int PRICE_PERCENTAGE = 10; //this is the percentage at which refresh action is taken
    private static int SLEEP_COUNT = 0;
    private StrategySecondaryPegTask strategy = null;
    private final int MAX_ATTEMPTS = 5;

    private int count;

    private boolean isFirstTimeExecution = true;

    private String wallshiftsFilePath = Global.sessionLogFolders + "/" + "wall_shifts.csv";
    private String wallshiftsFilePathJson = Global.sessionLogFolders + "/" + "wall_shifts.json";

    private String emailHistory = "";

    private Long currentTime = null;

    private static final Logger LOG = LoggerFactory.getLogger(PriceMonitorTriggerTask.class.getName());

    private boolean first = true;

    public void init(){
        File c = new File(this.wallshiftsFilePath);
        if (!c.exists()) {
            try {
                c.createNewFile();
            } catch (Exception e) {
                LOG.error("error creating " + c);
            }
        }

        FileSystem.writeToFile("timestamp,source,crypto,price,currency,sellprice,buyprice,otherfeeds\n", wallshiftsFilePath, true);

        //create json file if it doesn't already exist
        File json = new File(this.wallshiftsFilePathJson);
        if (!json.exists()) {
            try {
                json.createNewFile();
            } catch (Exception e) {
                LOG.error("error creating " + json);
            }
            JSONObject history = new JSONObject();
            JSONArray wall_shifts = new JSONArray();
            history.put("wall_shifts", wall_shifts);
            FileSystem.writeToFile(history.toJSONString(), this.wallshiftsFilePathJson, true);
        }
    }

    public void setPriceFeedManager(PriceFeedManager pfm) {
        this.pfm = pfm;
    }

    @Override
    public void run() {

        if (first) {
            LOG.info("running PriceMonitorTrigger for first time");
            init();
            first = false;
        }

        //if a problem occurred we sleep for a period using the SLEEP_COUNTER
        if (SLEEP_COUNT > 0) {
            SLEEP_COUNT--;
            currentTime = System.currentTimeMillis();
            return;
        }

        //take a note of the current time.
        //sudden changes in price can cause the bot to re-request the price data repeatedly
        // until the moving average is within 10% of the reported price.
        //we don't want that process to take longer than the price refresh interval
        currentTime = System.currentTimeMillis();
        LOG.debug("Executing task : PriceMonitorTriggerTask ");
        if (pfm == null || strategy == null) {
            LOG.error("PriceMonitorTriggerTask task needs a PriceFeedManager and a Strategy to work. Please assign it before running it");

        } else {
            count = 1;
            try {
                executeUpdatePrice(count);
            } catch (FeedPriceException e) {
                sendErrorNotification();
                Global.exchange.getTrade().clearOrders(Global.options.getPair());
            }
        }

    }

    private void initStrategy(double peg_price) throws NuBotConnectionException {

        Global.conversion = peg_price; //used then for liquidity info
        //Compute the buy/sell prices in USD

        //get the TX fee
        ApiResponse txFeeNTBPEGResponse = Global.exchange.getTrade().getTxFee(Global.options.getPair());
        if (!txFeeNTBPEGResponse.isPositive()) {
            throw new NuBotConnectionException("Cannot get txFee : " + txFeeNTBPEGResponse.getError().getDescription());
        }

        double txfee = (Double) txFeeNTBPEGResponse.getResponseObject();

        sellPriceUSD = 1 + (0.01 * txfee);
        if (!Global.options.isDualSide()) {
            sellPriceUSD = sellPriceUSD + Global.options.getPriceIncrement();
        }
        buyPriceUSD = 1 - (0.01 * txfee);

        //Add(remove) the offset % from prices

        //compute half of the spread
        double halfSpread = Utils.round(Global.options.getSpread() / 2, 6);

        double offset = Utils.round(halfSpread / 100, 6);

        LOG.debug("halfspread " + halfSpread);
        LOG.debug("offset " + offset);

        sellPriceUSD = sellPriceUSD + offset;
        buyPriceUSD = buyPriceUSD - offset;

        String message = "Computing USD prices with spread " + Global.options.getSpread() + "%  : sell @ " + sellPriceUSD;
        if (Global.options.isDualSide()) {
            message += " buy @ " + buyPriceUSD;
        }
        LOG.info(message);

        //convert sell price to PEG

        double sellPricePEGInitial;
        double buyPricePEGInitial;
        if (Global.swappedPair) { //NBT as paymentCurrency
            sellPricePEGInitial = Utils.round(Global.conversion * sellPriceUSD, 8);
            buyPricePEGInitial = Utils.round(Global.conversion * buyPriceUSD, 8);
        } else {
            sellPricePEGInitial = Utils.round(sellPriceUSD / peg_price, 8);
            buyPricePEGInitial = Utils.round(buyPriceUSD / peg_price, 8);
        }

        //store first value
        this.bidask = new BidAskPair(buyPricePEGInitial, sellPricePEGInitial);

        String message2 = "Converted price (using 1 " + Global.options.getPair().getPaymentCurrency().getCode() + " = " + peg_price + " USD)"
                + " : sell @ " + sellPricePEGInitial + " " + Global.options.getPair().getPaymentCurrency().getCode() + "";

        if (Global.options.isDualSide()) {
            message2 += "; buy @ " + buyPricePEGInitial + " " + Global.options.getPair().getPaymentCurrency().getCode();
        }
        LOG.info(message2);

        //Assign prices
        if (!Global.swappedPair) {
            ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask())).setBuyPricePEG(buyPricePEGInitial);
            ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask())).setSellPricePEG(sellPricePEGInitial);
        } else {
            ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask())).setBuyPricePEG(sellPricePEGInitial);
            ((StrategySecondaryPegTask) (Global.taskManager.getSecondaryPegTask().getTask())).setSellPricePEG(buyPricePEGInitial);
        }
        //Start strategy
        Global.taskManager.getSecondaryPegTask().start();

        //Send email notification
        String title = " production (" + Global.options.getExchangeName() + ") [" + pfm.getPair().toString() + "] price tracking started";
        String tldr = pfm.getPair().getOrderCurrency().getCode().toUpperCase() + " price trackin started at " + peg_price + " " + pfm.getPair().getPaymentCurrency().getCode().toUpperCase() + ".\n"
                + "Will send a new mail notification everytime the price of " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + " changes more than " + Global.options.getWallchangeThreshold() + "%.";
        MailNotifications.send(Global.options.getMailRecipient(), title, tldr);

    }

    private void executeUpdatePrice(int countTrials) throws FeedPriceException {

        if (countTrials <= MAX_ATTEMPTS) {
            ArrayList<LastPrice> priceList = pfm.fetchLastPrices().getPrices();

            LOG.debug("CheckLastPrice received values from remote feeds. ");

            if (priceList.size() == pfm.getFeedList().size()) {
                //All feeds returned a positive value
                //Check if mainPrice is close enough to the others
                // I am assuming that mainPrice is the first element of the list
                if (sanityCheck(priceList, 0)) {
                    //mainPrice is reliable compared to the others
                    this.updateLastPrice(priceList.get(0),priceList);

                } else {
                    //mainPrice is not reliable compared to the others
                    //Check if other backup prices are close enough to each other
                    boolean foundSomeValidBackUp = false;
                    LastPrice goodPrice = null;
                    for (int l = 1; l < priceList.size(); l++) {
                        if (sanityCheck(priceList, l)) {
                            goodPrice = priceList.get(l);
                            foundSomeValidBackUp = true;
                            break;
                        }
                    }

                    if (foundSomeValidBackUp) {
                        //goodPrice is a valid price backup!

                        this.updateLastPrice(goodPrice,priceList);
                    } else {
                        //None of the source are in accord with others.
                        //Try to send a notification
                        unableToUpdatePrice(priceList);
                    }
                }
            } else {
                //One or more feed returned an error value

                if (priceList.size() == 2) { // if only 2 values are available
                    double p1 = priceList.get(0).getPrice().getQuantity();
                    double p2 = priceList.get(1).getPrice().getQuantity();
                    if (closeEnough(this.DISTANCE_TRESHHOLD, p1, p2)) {

                        this.updateLastPrice(priceList.get(0),priceList);
                    } else {
                        //The two values are too unreliable
                        unableToUpdatePrice(priceList);
                    }
                } else if (priceList.size() > 2) { // more than two
                    //Check if other backup prices are close enough to each other
                    boolean foundSomeValidBackUp = false;
                    LastPrice goodPrice = null;
                    for (int l = 1; l < priceList.size(); l++) {
                        if (sanityCheck(priceList, l)) {
                            goodPrice = priceList.get(l);
                            foundSomeValidBackUp = true;
                            break;
                        }
                    }
                    if (foundSomeValidBackUp) {
                        //goodPrice is a valid price backup!

                        this.updateLastPrice(goodPrice,priceList);
                    } else {
                        //None of the source are in accord with others.
                        //Try to send a notification
                        unableToUpdatePrice(priceList);
                    }
                } else {//if only one or 0 feeds are positive
                    unableToUpdatePrice(priceList);
                }
            }

        } else {
            //Tried more than three times without success
            throw new FeedPriceException("The price has failed updating more than " + MAX_ATTEMPTS + " times in a row");

        }
    }


    private void unableToUpdatePrice(ArrayList<LastPrice> priceList) {
        count++;
        try {
            Thread.sleep(count * 60 * 1000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }
        try {
            executeUpdatePrice(count);
        } catch (FeedPriceException ex) {
            LOG.error(ex.toString());
        }
    }


    public void gracefulPause(LastPrice lp) {
        //This is called is an abnormal price is detected for one whole refresh period
        String logMessage;
        String notification;
        String subject;
        MessageColor notificationColor;
        double sleepTime = 0;

        //we need to check the reason that the refresh took a whole period.
        //if it's because of a no connection issue, we need to wait to see if connection restarts
        if (!Global.exchange.getLiveData().isConnected()) {
            currentTime = System.currentTimeMillis();


            logMessage = "There has been a connection issue for " + Settings.CHECK_PRICE_INTERVAL + " seconds\n"
                    + "Consider restarting the bot if the connection issue persists";
            notification = "";
            notificationColor = MessageColor.YELLOW;
            subject = Global.exchange.getName() + " Bot is suffering a connection issue";

        } else { //otherwise something bad has happened so we shutdown.
            int p = 3;
            sleepTime = Settings.CHECK_PRICE_INTERVAL * p;

            logMessage = "The Fetched Exchange rate data has remained outside of the required price band for "
                    + Settings.CHECK_PRICE_INTERVAL + "seconds.\nThe bot will notify and restart in "
                    + sleepTime + "seconds.";
            notification = "A large price difference was detected at " + Global.exchange.getName()
                    + ".\nThe Last obtained price of " + Objects.toString(lp.getPrice().getQuantity()) + " was outside of "
                    + Objects.toString(PRICE_PERCENTAGE) + "% of the moving average figure of " + Objects.toString(getMovingAverage())
                    + ".\nNuBot will remove the current orders and replace them in "
                    + sleepTime + "seconds.";
            notificationColor = MessageColor.PURPLE;
            subject = Global.exchange.getName() + " Moving Average issue. Bot will replace orders in "
                    + sleepTime + "seconds.";
        }
        //we want to send Hip Chat and mail notifications,
        // cancel all orders to avoid arbitrage against the bot and
        // exit execution gracefully
        LOG.error(logMessage);
        LOG.error("Notifying HipChat");
        HipChatNotifications.sendMessage(notification, notificationColor);
        LOG.error("Sending Email");
        MailNotifications.send(Global.options.getMailRecipient(), subject, notification);
        if (sleepTime > 0) {
            LOG.error("Cancelling Orders to avoid Arbitrage against the bot");
            Global.exchange.getTrade().clearOrders(Global.options.getPair());
            //clear the moving average so the restart is fresh
            queueMA.clear();
            LOG.error("Sleeping for " + sleepTime);
            SLEEP_COUNT = 3;
        }
        currentTime = System.currentTimeMillis();
    }

    public void updateLastPrice(LastPrice lp,ArrayList<LastPrice> priceList) {

        //We need to fill up the moving average queue so that 30 data points exist.
        if (queueMA.size() < MOVING_AVERAGE_SIZE) {
            initMA(lp.getPrice().getQuantity());
        }

        if (!Global.options.isMultipleCustodians()) {  //
            //we check against the moving average
            double current = lp.getPrice().getQuantity();
            double MA = getMovingAverage();

            //calculate the percentage difference
            double percentageDiff = (((MA - current) / ((MA + current) / 2)) * 100);
            if ((percentageDiff > PRICE_PERCENTAGE) || (percentageDiff < -PRICE_PERCENTAGE)) {
                //The potential price is more than % different to the moving average
                //add it to the MA-Queue to raise the Moving Average and re-request the currency data
                //in this way we can react to a large change in price when we are sure it is not an anomaly
                LOG.warn("Latest price " + Objects.toString(current) + " is " + Objects.toString(percentageDiff) + "% outside of the moving average of " + Objects.toString(MA) + "."
                        + "\nShifting moving average and re-fetching exchange rate data.");
                updateMovingAverageQueue(current);

                try {
                    int trials = 1;
                    executeUpdatePrice(trials);
                } catch (FeedPriceException ex) {

                }
                return;
            }
            //the potential price is within the % boundary.
            //add it to the MA-Queue to keep the moving average moving
            // Only do this if the standard update interval hasn't passed
            if (((System.currentTimeMillis() - (currentTime + REFRESH_OFFSET)) / 1000L) < Settings.CHECK_PRICE_INTERVAL) {
                updateMovingAverageQueue(current);
            } else {
                //If we get here, we haven't had a price within % of the average for as long as a standard update period
                //the action is to send notifications, cancel all orders and turn off the bot
                gracefulPause(lp);
                return;
            }
        }

        //carry on with updating the wall price shift
        this.lastPrice = lp;
        this.lastPrices = priceList ;

        LOG.info("Price Updated. " + lp.getSource() + ":1 " + lp.getCurrencyMeasured().getCode() + " = "
                + "" + lp.getPrice().getQuantity() + " " + lp.getPrice().getCurrency().getCode());
        if (isFirstTimeExecution) {
            try {
                initStrategy(lp.getPrice().getQuantity());
            } catch (NuBotConnectionException e) {

            }
            currentWallPEGPrice = lp;
            isFirstTimeExecution = false;
        } else {
            verifyPegPrices();
        }
    }

    private void verifyPegPrices() {

        LOG.debug("Executing tryMoveWalls");

        boolean needToShift = true;
        if (!Global.options.isMultipleCustodians()) {
            needToShift = needToMoveWalls(lastPrice);         //check if price moved more than x% from when the wall was setup
        }

        if (needToShift && !isWallsBeingShifted()) { //prevent a wall shift trigger if the strategy is already shifting walls.
            LOG.info("Walls needs to be shifted");
            //Compute price for walls
            currentWallPEGPrice = lastPrice;
            computeNewPrices();

        } else {
            LOG.debug("No need to move walls");
            currentTime = System.currentTimeMillis();
            if (isWallsBeingShifted() && needToShift) {
                LOG.warn("Wall shift is postponed: another process is already shifting existing walls. Will try again on next execution.");
            }
        }
    }

    private boolean needToMoveWalls(LastPrice last) {
        double currentWallPEGprice = currentWallPEGPrice.getPrice().getQuantity();
        double distance = Math.abs(last.getPrice().getQuantity() - currentWallPEGprice);
        double percentageDistance = Utils.round((distance * 100) / currentWallPEGprice, 4);
        LOG.debug("delta =" + percentageDistance + "% (old : " + currentWallPEGprice + " new " + last.getPrice().getQuantity() + ")");

        if (percentageDistance < wallchangeThreshold) {
            return false;
        } else {
            return true;
        }
    }

    private void computeNewPrices() {

        double peg_price = lastPrice.getPrice().getQuantity();

        double sellPricePEG_new;
        double buyPricePEG_new;

        if (Global.swappedPair) { //NBT as paymentCurrency
            sellPricePEG_new = Utils.round(sellPriceUSD * Global.conversion, Settings.DEFAULT_PRECISION);
            buyPricePEG_new = Utils.round(buyPriceUSD * Global.conversion, Settings.DEFAULT_PRECISION);
        } else {
            //convert sell price to PEG
            sellPricePEG_new = Utils.round(sellPriceUSD / peg_price, Settings.DEFAULT_PRECISION);
            buyPricePEG_new = Utils.round(buyPriceUSD / peg_price, Settings.DEFAULT_PRECISION);
        }

        BidAskPair newPrice = new BidAskPair(buyPricePEG_new, sellPricePEG_new);

        //check if the price increased or decreased compared to last
        if ((newPrice.getAsk() - this.bidask.getAsk()) > 0) {
            this.pegPriceDirection = Constant.UP;
        } else {
            this.pegPriceDirection = Constant.DOWN;
        }

        //Store new value
        this.bidask = newPrice;

        LOG.info("Sell Price " + sellPricePEG_new + "  | "
                + "Buy Price  " + buyPricePEG_new);

        //------------ here for output csv

        String source = currentWallPEGPrice.getSource();
        double price = currentWallPEGPrice.getPrice().getQuantity();
        String currency = currentWallPEGPrice.getPrice().getCurrency().getCode();
        String crypto = pfm.getPair().getOrderCurrency().getCode();

        //Call Strategy and notify the price change

        strategy.notifyPriceChanged(sellPricePEG_new, buyPricePEG_new, price, pegPriceDirection);

        Global.conversion = price;

        Date currentDate = new Date();
        String row = currentDate + ","
                + source + ","
                + crypto + ","
                + price + ","
                + currency + ","
                + sellPricePEG_new + ","
                + buyPricePEG_new + ",";

        JSONArray backup_feeds = new JSONArray();
        JSONObject otherPricesAtThisTime = new JSONObject();


        for (int i = 0; i < this.lastPrices.size(); i++) {
            LastPrice tempPrice = lastPrices.get(i);
            otherPricesAtThisTime.put("feed", tempPrice.getSource());
            otherPricesAtThisTime.put("price", tempPrice.getPrice().getQuantity());
        }

        LOG.warn(row);

        row += otherPricesAtThisTime.toString() + "\n";
        backup_feeds.add(otherPricesAtThisTime);
        logrow(row, wallshiftsFilePath, true);

        //Also update a json version of the output file
        //build the latest data into a JSONObject
        JSONObject wall_shift = new JSONObject();
        wall_shift.put("timestamp", currentDate.getTime());
        wall_shift.put("feed", source);
        wall_shift.put("crypto", crypto);
        wall_shift.put("price", price);
        wall_shift.put("currency", currency);
        wall_shift.put("sell_price", sellPricePEG_new);
        wall_shift.put("buy_price", buyPricePEG_new);
        wall_shift.put("backup_feed", backup_feeds);
        //now read the existing object if one exists
        JSONParser parser = new JSONParser();
        JSONObject wall_shift_info = new JSONObject();
        JSONArray wall_shifts = new JSONArray();
        try { //object already exists in file
            wall_shift_info = (JSONObject) parser.parse(FileSystem.readFromFile(this.wallshiftsFilePathJson));
            wall_shifts = (JSONArray) wall_shift_info.get("wall_shifts");
        } catch (ParseException pe) {
            LOG.error("Unable to parse order_history.json");
        }
        //add the latest orders to the orders array
        wall_shifts.add(wall_shift);
        wall_shift_info.put("wall_shifts", wall_shifts);
        //then save

        logWallShift(wall_shift_info.toJSONString());

        if (Global.options.sendMails()) {
            String title = " production (" + Global.options.getExchangeName() + ") [" + pfm.getPair().toString() + "] price changed more than " + wallchangeThreshold + "%";

            String messageNow = row;
            emailHistory += messageNow;

            String tldr = pfm.getPair().toString() + " price changed more than " + wallchangeThreshold + "% since last notification: "
                    + "now is " + price + " " + pfm.getPair().getPaymentCurrency().getCode().toUpperCase() + ".\n"
                    + "Here are the prices the bot used in the new orders : \n"
                    + "Sell at " + sellPricePEG_new + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + " "
                    + "and buy at " + buyPricePEG_new + " " + pfm.getPair().getOrderCurrency().getCode().toUpperCase() + "\n"
                    + "\n#########\n"
                    + "Below you can see the history of price changes. You can copy paste to create a csv report."
                    + "For each row the bot should have shifted the sell/buy walls.\n\n";


            if(!Global.options.isMultipleCustodians()) {
                MailNotifications.send(Global.options.getMailRecipient(), title, tldr + emailHistory);
            }
        }
    }


    public void setWallchangeThreshold(double wallchangeThreshold) {
        this.wallchangeThreshold = wallchangeThreshold;
    }

    public void setStrategy(StrategySecondaryPegTask strategy) {
        this.strategy = strategy;
    }

    public boolean isWallsBeingShifted() {
        return wallsBeingShifted;
    }

    public void setWallsBeingShifted(boolean wallsBeingShifted) {
        currentTime = System.currentTimeMillis();
        this.wallsBeingShifted = wallsBeingShifted;
    }


    private void sendErrorNotification() {
        String title = "Problems while updating " + pfm.getPair().getOrderCurrency().getCode() + " price. Cannot find a reliable feed.";
        String message = "NuBot timed out after " + MAX_ATTEMPTS + " failed attempts to update " + pfm.getPair().getOrderCurrency().getCode() + ""
                + " price. Please restart the bot and get in touch with Nu Dev team";
        MailNotifications.sendCritical(Global.options.getMailRecipient(), title, message);
        HipChatNotifications.sendMessageCritical(title + message);
        LOG.error(title + message);

    }

    // ----- price utils ------

    public double getMovingAverage() {
        double MA = 0;
        for (Iterator<Double> price = queueMA.iterator(); price.hasNext(); ) {
            MA += price.next();
        }
        MA = MA / queueMA.size();
        return MA;
    }

    public void updateMovingAverageQueue(double price) {
        if (price == 0) {
            //don't add 0
            return;
        }
        queueMA.add(price);
        //trim the queue so that it is a moving average over the correct number of data points
        if (queueMA.size() > MOVING_AVERAGE_SIZE) {
            queueMA.remove();
        }
    }

    /**
     * init queue by filling it with one price only
     *
     * @param price
     */
    protected void initMA(double price) {
        for (int i = 0; i <= 30; i++) {
            updateMovingAverageQueue(price);
        }
    }

    protected boolean closeEnough(double distanceTreshold, double mainPrice, double temp) {
        //if temp differs from mainPrice for more than a threshold%, return false
        double distance = Math.abs(mainPrice - temp);

        double percentageDistance = Utils.round(distance * 100 / mainPrice, 4);
        if (percentageDistance > distanceTreshold) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Measure if mainPrice is close to other two values
     *
     * @param priceList
     * @param mainPriceIndex
     * @return
     */
    protected boolean sanityCheck(ArrayList<LastPrice> priceList, int mainPriceIndex) {


        boolean[] ok = new boolean[priceList.size() - 1];
        double mainPrice = priceList.get(mainPriceIndex).getPrice().getQuantity();

        //Test mainPrice vs backup sources
        int f = 0;
        for (int i = 0; i < priceList.size(); i++) {
            if (i != mainPriceIndex) {
                LastPrice tempPrice = priceList.get(i);
                double temp = tempPrice.getPrice().getQuantity();
                ok[f] = closeEnough(DISTANCE_TRESHHOLD, mainPrice, temp);
                f++;
            }
        }

        int countOk = 0;
        for (int j = 0; j < ok.length; j++) {
            if (ok[j]) {
                countOk++;
            }
        }

        boolean overallOk = false; //is considered ok if the mainPrice is closeEnough to more than a half of backupPrices
        //Need to distinguish pair vs odd
        if (ok.length % 2 == 0) {
            if (countOk >= (int) ok.length / 2) {
                overallOk = true;
            }
        } else {
            if (countOk > (int) ok.length / 2) {
                overallOk = true;
            }
        }

        return overallOk;
    }


    protected void notifyDeviation(ArrayList<LastPrice> priceList) {
        String title = "Problems while updating " + pfm.getPair().getOrderCurrency().getCode() + " price. Cannot find a reliable feed.";
        String message = "Positive response from " + priceList.size() + "/" + pfm.getFeedList().size() + " feeds\n";
        for (int i = 0; i < priceList.size(); i++) {
            LastPrice tempPrice = priceList.get(i);
            message += (tempPrice.getSource() + ":1 " + tempPrice.getCurrencyMeasured().getCode() + " = "
                    + tempPrice.getPrice().getQuantity() + " " + tempPrice.getPrice().getCurrency().getCode()) + "\n";
        }


        MailNotifications.sendCritical(Global.options.getMailRecipient(), title, message);
        HipChatNotifications.sendMessageCritical(title + message);

        LOG.error(title + message);
    }


    private void logrow(String row, String outputPath, boolean append) {
        FileSystem.writeToFile(row, outputPath, append);
    }

    private void logWallShift(String wall_shift) {
        FileSystem.writeToFile(wall_shift, this.wallshiftsFilePathJson, false);
    }
    
}
