package com.nubits.nubot.options;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Logger;

/**
 *
 */
public class ParseOptions {

    private static final Logger LOG = Logger.getLogger(ParseOptions.class.getName());

    /**
     * Parse Options from an array of paths
     *
     * @param paths
     * @return
     */
    public static NuBotOptions parseOptions(String[] paths) throws NuBotConfigException {
        NuBotOptions options = null;
        ArrayList<String> filePaths = new ArrayList();
        filePaths.addAll(Arrays.asList(paths));


        JSONObject inputJSON = NuBotOptions.parseFiles(filePaths);
        JSONObject optionsJSON = (JSONObject) inputJSON.get("options");


        //First try to parse compulsory parameters
        String exchangeName = (String) optionsJSON.get("exchangename");

        String apiKey = "";


        if (!exchangeName.equalsIgnoreCase(Constant.CCEX)) { //for ccex this parameter can be omitted
            if (!optionsJSON.containsKey("apikey")) {
                Utils.exitWithMessage("The apikey parameter is compulsory.");
            } else {
                apiKey = (String) optionsJSON.get("apikey");
            }

        }

        String apiSecret = (String) optionsJSON.get("apisecret");

        String mailRecipient = (String) optionsJSON.get("mail-recipient");

        String pairStr = (String) optionsJSON.get("pair");
        CurrencyPair pair = CurrencyPair.getCurrencyPairFromString(pairStr, "_");

        boolean aggregate = true; //true only for USD
        if (!pair.getPaymentCurrency().getCode().equalsIgnoreCase("USD")) {
            aggregate = false; //default to false
        }


        boolean dualside = (boolean) optionsJSON.get("dualside");


        //Based on the pair, set a parameter do define whether setting SecondaryPegOptionsJSON i necessary or not
        boolean requireCryptoOptions = Utils.requiresSecondaryPegStrategy(pair);
        org.json.JSONObject pegOptionsJSON;
        SecondaryPegOptionsJSON cpo = null;
        if (requireCryptoOptions) {

            if (optionsJSON.containsKey("secondary-peg-options")) {

                Map setMap = new HashMap();


                //convert from simple JSON to org.json.JSONObject
                JSONObject oldObject = (JSONObject) optionsJSON.get("secondary-peg-options");

                Set tempSet = oldObject.entrySet();
                for (Object o : tempSet) {
                    Map.Entry entry = (Map.Entry) o;
                    setMap.put(entry.getKey(), entry.getValue());
                }

                pegOptionsJSON = new org.json.JSONObject(setMap);
                cpo = SecondaryPegOptionsJSON.create(pegOptionsJSON, pair);
            } else {
                throw new NuBotConfigException("secondary-peg-options are required in the options");
            }

                /*
                 org.json.JSONObject jsonString = new org.json.JSONObject(optionsString);
                 org.json.JSONObject optionsJSON2 = (org.json.JSONObject) jsonString.get("options");
                 pegOptionsJSON = (org.json.JSONObject) optionsJSON2.get("secondary-peg-options");
                 cpo = SecondaryPegOptionsJSON.create(pegOptionsJSON, pair);*/
        }

        //Then parse optional settings. If not use the default value declared here

        String nudIp = "127.0.0.1";
        String sendMails = MailNotifications.MAIL_LEVEL_SEVERE;
        boolean submitLiquidity = true;
        boolean executeOrders = true;
        boolean verbose = false;
        boolean sendHipchat = true;

        boolean multipleCustodians = false;
        int executeStrategyInterval = 41;
        int sendLiquidityInterval = Integer.parseInt(Global.settings.getProperty("submit_liquidity_seconds"));

        double txFee = 0.2;
        double priceIncrement = 0.0003;
        double keepProceeds = 0;

        double maxSellVolume = 0;
        double maxBuyVolume = 0;


        int emergencyTimeout = 60;

        if (optionsJSON.containsKey("nudip")) {
            nudIp = (String) optionsJSON.get("nudip");
        }

        if (optionsJSON.containsKey("priceincrement")) {
            priceIncrement = Utils.getDouble(optionsJSON.get("priceincrement"));
        }

        if (optionsJSON.containsKey("txfee")) {
            txFee = Utils.getDouble(optionsJSON.get("txfee"));
        }

        if (optionsJSON.containsKey("submit-liquidity")) {
            submitLiquidity = (boolean) optionsJSON.get("submit-liquidity");
        }

        if (optionsJSON.containsKey("max-sell-order-volume")) {
            maxSellVolume = Utils.getDouble(optionsJSON.get("max-sell-order-volume"));
        }

        if (optionsJSON.containsKey("max-buy-order-volume")) {
            maxBuyVolume = Utils.getDouble(optionsJSON.get("max-buy-order-volume"));
        }

        //Now require the parameters only if submitLiquidity is true, otherwise can use the default value

        String nubitAddress = "", rpcPass = "", rpcUser = "";
        int nudPort = 9091;

        if (submitLiquidity) {
            if (optionsJSON.containsKey("nubitaddress")) {
                nubitAddress = (String) optionsJSON.get("nubitaddress");
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"nubitaddress\" ");
            }

            if (optionsJSON.containsKey("rpcpass")) {
                rpcPass = (String) optionsJSON.get("rpcpass");
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"rpcpass\" ");
            }

            if (optionsJSON.containsKey("rpcuser")) {
                rpcUser = (String) optionsJSON.get("rpcuser");
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"rpcuser\" ");
            }

            if (optionsJSON.containsKey("nudport")) {
                long nudPortlong = (long) optionsJSON.get("nudport");
                nudPort = (int) nudPortlong;
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"nudport\" ");
            }

        }


        if (optionsJSON.containsKey("executeorders")) {
            executeOrders = (boolean) optionsJSON.get("executeorders");
        }

        if (optionsJSON.containsKey("verbose")) {
            verbose = (boolean) optionsJSON.get("verbose");
        }

        if (optionsJSON.containsKey("hipchat")) {
            sendHipchat = (boolean) optionsJSON.get("hipchat");
        }

        if (optionsJSON.containsKey("mail-notifications")) {
            sendMails = (String) optionsJSON.get("mail-notifications");
            if (sendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_ALL)
                    || sendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_NONE)
                    || sendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_SEVERE)) {
                sendMails = sendMails.toUpperCase(); //Convert to upper case
            } else {
                LOG.severe("Value not accepted for \"mail-notifications\" : " + sendMails + " . Admitted values  : "
                        + MailNotifications.MAIL_LEVEL_ALL + " , "
                        + MailNotifications.MAIL_LEVEL_SEVERE + " or "
                        + MailNotifications.MAIL_LEVEL_NONE);
                System.exit(0);
            }
        }


        //TODO: ?
            /*Ignore this parameter to prevent one custodian to execute faster than others (walls collapsing)
             if (optionsJSON.containsKey("check-balance-interval")) {
             long checkBalanceIntervallong = (long) optionsJSON.get("check-balance-interval");
             checkBalanceInterval = (int) checkBalanceIntervallong;
             }

             if (optionsJSON.containsKey("check-orders-interval")) {
             long checkOrdersIntevallong = (long) optionsJSON.get("check-orders-interval");
             checkOrdersInteval = (int) checkOrdersIntevallong;
             }
             */

        if (optionsJSON.containsKey("emergency-timeout")) {
            long emergencyTimeoutLong = (long) optionsJSON.get("emergency-timeout");
            emergencyTimeout = (int) emergencyTimeoutLong;
        }

        if (optionsJSON.containsKey("keep-proceeds")) {
            keepProceeds = Utils.getDouble((optionsJSON.get("keep-proceeds")));
        }

        if (optionsJSON.containsKey("multiple-custodians")) {
            multipleCustodians = (boolean) optionsJSON.get("multiple-custodians");
        }
        //Create a new Instance
        options = new NuBotOptions(dualside, apiKey, apiSecret, nubitAddress, rpcUser,
                rpcPass, nudIp, nudPort, priceIncrement, txFee, submitLiquidity, exchangeName,
                executeOrders, verbose, pair, executeStrategyInterval,
                sendLiquidityInterval, sendHipchat, sendMails, mailRecipient,
                emergencyTimeout, keepProceeds, aggregate, multipleCustodians, maxSellVolume, maxBuyVolume, cpo);


        if (options == null)
            throw new NuBotConfigException("error parsing configuration files");

        return options;
    }
}
