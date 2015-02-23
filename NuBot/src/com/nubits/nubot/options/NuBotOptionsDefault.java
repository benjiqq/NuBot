package com.nubits.nubot.options;

import com.nubits.nubot.global.Global;
import com.nubits.nubot.notifications.MailNotifications;

/**
 * Default options for NuBot
 */
public class NuBotOptionsDefault {

    //TODO consistent names
    public static int sendLiquidityInterval = Integer.parseInt(Global.settings.getProperty("submit_liquidity_seconds"));
    public static int reset_every = Integer.parseInt(Global.settings.getProperty("reset_every_minutes"));
    public static int refresh_time_seconds = Integer.parseInt(Global.settings.getProperty("refresh_time_seconds"));

    public static String nudIp = "127.0.0.1";
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
}
