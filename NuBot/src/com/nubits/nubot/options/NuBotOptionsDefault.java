package com.nubits.nubot.options;

import com.nubits.nubot.global.Global;
import com.nubits.nubot.notifications.MailNotifications;

/**
 * Default options for NuBot
 */
public class NuBotOptionsDefault {

    public static String nudIp = "127.0.0.1";
    public static int nudport = 9091;
    public static String sendMails = MailNotifications.MAIL_LEVEL_SEVERE;
    public static boolean submitLiquidity = false;
    public static boolean executeOrders = true;
    public static boolean verbose = false;
    public static boolean sendHipchat = true;
    public static boolean multipleCustodians = false;
    public static int executeStrategyInterval = 41;
    public static double txFee = 0.2;
    public static double priceIncrement = 0.0003;
    public static double keepProceeds = 0;
    public static double maxSellVolume = 0;
    public static double maxBuyVolume = 0;
    public static int emergencyTimeout = 60;
    public static boolean distributeLiquidity = false;

    //double wallchangeThreshold = 0.5;
    //double spread = 0;
    //double distanceThreshold = 10;

    public static NuBotOptions defaultFactory() {

        NuBotOptions opt = new NuBotOptions(true, nudIp, "", "", "", "",
                nudIp, nudport, nudport, nudport, true, "", true, true, null,
                30, false, "", "", -1, 0, false, true, 0, 0, false, false);
        return opt;
    }
}
