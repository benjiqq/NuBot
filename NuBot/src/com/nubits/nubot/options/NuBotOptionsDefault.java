package com.nubits.nubot.options;

import com.nubits.nubot.global.Constant;
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

        NuBotOptions opt = new NuBotOptions();
        opt.dualSide = true;
        opt.apiKey = "";
        opt.apiSecret = "";
        opt.rpcUser = "";
        opt.rpcPass = "";
        opt.nudIp = "127.0.0.1";
        opt.priceIncrement = 0.0;
        opt.txFee = 0.0;
        opt.submitLiquidity = false;
        opt.executeOrders = false;
        opt.executeStrategyInterval = 30;
        opt.sendHipchat = true;
        opt.sendMails = "NONE";
        opt.mailRecipient = "";
        opt.emergencyTimeout = 30;
        opt.keepProceeds = 0.0;
        opt.distributeLiquidity = false;
        opt.secondarypeg = false;
        opt.pair = Constant.NBT_USD;
        return opt;
    }
}
