package com.nubits.nubot.tasks.strategy;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.pricefeeds.PriceFeedManager;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.TimerTask;


public abstract class MonitorTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorTask.class.getName());

    protected PriceFeedManager pfm = null;

    /**
     * threshold for signaling a deviation of prices
     */
    protected double distanceTreshold;

    protected LastPrice lastPrice;

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


}
