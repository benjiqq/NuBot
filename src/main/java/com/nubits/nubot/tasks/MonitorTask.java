/*
 * Copyright (C) 2014-2015 Nu Development Team
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
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public abstract class MonitorTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorTask.class.getName());

    protected PriceFeedManager pfm = null;

    //set up a Queue to hold the prices used to calculate the moving average of prices
    protected Queue<Double> queueMA = new LinkedList<>();

    protected int MOVING_AVERAGE_SIZE = 30; //this is how many elements the Moving average queue holds

    /*public MonitorTask(){

    }*/

    /**
     * threshold for signaling a deviation of prices
     */
    protected double distanceTreshold;

    protected LastPrice lastPrice;

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
                ok[f] = closeEnough(distanceTreshold, mainPrice, temp);
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


}
