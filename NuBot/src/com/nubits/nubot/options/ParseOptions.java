package com.nubits.nubot.options;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * ParseOptions from one or several JSON files
 */
public class ParseOptions {

    private static String[] comp = {"exchangename", "apisecret", "mailrecipient", "dualside", "pair"};

    private static final Logger LOG = Logger.getLogger(ParseOptions.class.getName());


    /**
     * Parse Options from an array of paths
     *
     * @param paths
     * @return
     * @throws NuBotConfigException
     */
    public static NuBotOptions parseOptions(String[] paths) throws NuBotConfigException {
        ArrayList<String> filePaths = new ArrayList();
        filePaths.addAll(Arrays.asList(paths));
        JSONObject inputJSON = parseFiles(filePaths);
        JSONObject optionsJSON = (JSONObject) inputJSON.get("options");
        return parseOptionsFromJson(optionsJSON);
    }

    /**
     * parse single JSON file to NuBoptions
     *
     * @param filepath
     * @return
     * @throws NuBotConfigException
     */
    public static NuBotOptions parseOptionsSingle(String filepath) throws NuBotConfigException {

        File f = new File(filepath);
        if (!f.exists())
            throw new NuBotConfigException("file " + f.getAbsolutePath() + " does not exist");

        try {
            JSONObject inputJSON = parseSingleJsonFile(filepath);
            return parseOptionsFromJson(inputJSON);

        } catch (ParseException ex) {
            throw new NuBotConfigException("Parse Exception. Configuration error from single file");
        } catch (Exception e){
            throw new NuBotConfigException("Configuration error from single file " + e);
        }

    }

    public static Object getIgnoreCase(JSONObject jobj, String key) {

        Iterator<String> iter = jobj.keySet().iterator();
        while (iter.hasNext()) {
            String key1 = iter.next();
            if (key1.equalsIgnoreCase(key)) {
                return jobj.get(key1);
            }
        }

        return null;

    }

    public static boolean containsIgnoreCase(JSONObject jobj, String key) {

        Iterator<String> iter = jobj.keySet().iterator();
        boolean contains = false;
        while (iter.hasNext()) {
            String key1 = iter.next();
            if (key1.equalsIgnoreCase(key))
                return true;
        }

        return contains;

    }


    /**
     * parseOptions from JSON into NuBotOptions
     *
     * @param optionsJSON
     * @return
     */
    public static NuBotOptions parseOptionsFromJson(JSONObject optionsJSON) throws NuBotConfigException {

        NuBotOptions options = null;

        for (int i = 0; i < comp.length; i++) {
            if (!containsIgnoreCase(optionsJSON, comp[i]))
                throw new NuBotConfigException("necessary key: " + comp[i]);
        }


        //default values for optional settings

        String nudIp = NuBotOptionsDefault.nudIp;
        String sendMails = NuBotOptionsDefault.sendMails;
        boolean submitLiquidity = NuBotOptionsDefault.submitLiquidity;
        boolean executeOrders = NuBotOptionsDefault.executeOrders;
        boolean verbose = NuBotOptionsDefault.verbose;
        boolean sendHipchat = NuBotOptionsDefault.sendHipchat;
        boolean multipleCustodians = NuBotOptionsDefault.multipleCustodians;
        int executeStrategyInterval = NuBotOptionsDefault.executeStrategyInterval;
        double txFee = NuBotOptionsDefault.txFee;
        double priceIncrement = NuBotOptionsDefault.priceIncrement;
        double keepProceeds = NuBotOptionsDefault.keepProceeds;
        double maxSellVolume = NuBotOptionsDefault.maxSellVolume;
        double maxBuyVolume = NuBotOptionsDefault.maxBuyVolume;
        int emergencyTimeout = NuBotOptionsDefault.emergencyTimeout;
        boolean distributeLiquidity = NuBotOptionsDefault.distributeLiquidity;

        //First try to parse compulsory parameters
        String exchangeName = (String) getIgnoreCase(optionsJSON, "exchangename");

        boolean dualside = (boolean) getIgnoreCase(optionsJSON, "dualSide");

        String apiKey = "";

        if (!exchangeName.equalsIgnoreCase(Constant.CCEX)) { //for ccex this parameter can be omitted
            if (!containsIgnoreCase(optionsJSON, "apiKey")) {
                throw new NuBotConfigException("The apikey parameter is compulsory.");
            } else {
                apiKey = (String) getIgnoreCase(optionsJSON, "apikey");
            }
        }

        String apiSecret = (String) getIgnoreCase(optionsJSON, "apisecret");

        String mailRecipient = (String) getIgnoreCase(optionsJSON, "mailrecipient");

        String pairStr = (String) getIgnoreCase(optionsJSON, "pair");
        CurrencyPair pair = CurrencyPair.getCurrencyPairFromString(pairStr, "_");

        boolean aggregate = true; //true only for USD
        if (!pair.getPaymentCurrency().getCode().equalsIgnoreCase("USD")) {
            aggregate = false; //default to false
        }


        //Based on the pair, set a parameter do define whether setting SecondaryPegOptionsJSON i necessary or not
        boolean requireCryptoOptions = Utils.requiresSecondaryPegStrategy(pair);
        org.json.JSONObject pegOptionsJSON;
        SecondaryPegOptionsJSON cpo = null;
        if (requireCryptoOptions) {

            if (optionsJSON.containsKey("secondary-peg-options")) {

                Map setMap = new HashMap();

                //convert from simple JSON to org.json.JSONObject
                JSONObject oldObject = (JSONObject) getIgnoreCase(optionsJSON, "secondary-peg-options");

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

        }

        if (containsIgnoreCase(optionsJSON, "nudip")) {
            nudIp = (String) getIgnoreCase(optionsJSON, "nudip");
        }
        //---- optional settings ----

        if (optionsJSON.containsKey("nudip")) {
            nudIp = (String) getIgnoreCase(optionsJSON, "nudip");
        }

        if (containsIgnoreCase(optionsJSON, "priceincrement")) {
            priceIncrement = Utils.getDouble(getIgnoreCase(optionsJSON, "priceincrement"));
        }

        if (containsIgnoreCase(optionsJSON, "txfee")) {
            txFee = Utils.getDouble(getIgnoreCase(optionsJSON, "txfee"));
        }

        if (containsIgnoreCase(optionsJSON, "submitliquidity")) {
            submitLiquidity = (boolean) getIgnoreCase(optionsJSON, "submitliquidity");
        }

        if (containsIgnoreCase(optionsJSON, "max-sell-order-volume")) {
            maxSellVolume = Utils.getDouble(getIgnoreCase(optionsJSON, "max-sell-order-volume"));
        }

        if (containsIgnoreCase(optionsJSON, "max-buy-order-volume")) {
            maxBuyVolume = Utils.getDouble(getIgnoreCase(optionsJSON, "max-buy-order-volume"));
        }

        if (containsIgnoreCase(optionsJSON, "executeorders")) {
            executeOrders = (boolean) getIgnoreCase(optionsJSON, "executeorders");
        }

        if (containsIgnoreCase(optionsJSON, "verbose")) {
            verbose = (boolean) getIgnoreCase(optionsJSON, "verbose");
        }

        if (containsIgnoreCase(optionsJSON, "hipchat")) {
            sendHipchat = (boolean) getIgnoreCase(optionsJSON, "hipchat");
        }

        if (containsIgnoreCase(optionsJSON, "emergency-timeout")) {
            long emergencyTimeoutLong = (long) getIgnoreCase(optionsJSON, "emergency-timeout");
            emergencyTimeout = (int) emergencyTimeoutLong;
        }

        if (containsIgnoreCase(optionsJSON, "keep-proceeds")) {
            keepProceeds = Utils.getDouble((getIgnoreCase(optionsJSON, "keep-proceeds")));
        }

        if (containsIgnoreCase(optionsJSON, "multiple-custodians")) {
            multipleCustodians = (boolean) getIgnoreCase(optionsJSON, "multiple-custodians");
        }

        if (containsIgnoreCase(optionsJSON, "distribute-liquidity")) {
            distributeLiquidity = (boolean) getIgnoreCase(optionsJSON, "distribute-liquidity");
        }

        //Now require the parameters only if submitLiquidity is true, otherwise can use the default value

        String nubitAddress = "", rpcPass = "", rpcUser = "";
        int nudPort = 9091;

        if (submitLiquidity) {
            if (containsIgnoreCase(optionsJSON, "nubitaddress")) {
                nubitAddress = (String) getIgnoreCase(optionsJSON, "nubitaddress");
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"nubitaddress\" ");
            }

            if (containsIgnoreCase(optionsJSON, "rpcpass")) {
                rpcPass = (String) getIgnoreCase(optionsJSON, "rpcpass");
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"rpcpass\" ");
            }

            if (containsIgnoreCase(optionsJSON, "rpcuser")) {
                rpcUser = (String) getIgnoreCase(optionsJSON, "rpcuser");
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"rpcuser\" ");
            }

            if (containsIgnoreCase(optionsJSON, "nudport")) {
                long nudPortlong = (long) getIgnoreCase(optionsJSON, "nudport");
                nudPort = (int) nudPortlong;
            } else {
                throw new NuBotConfigException("When submit-liquidity is set to true "
                        + "you need to declare a value for \"nudport\" ");
            }

        }

        if (containsIgnoreCase(optionsJSON, "mail-notifications")) {
            sendMails = (String) getIgnoreCase(optionsJSON, "mail-notifications");
            if (sendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_ALL)
                    || sendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_NONE)
                    || sendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_SEVERE)) {
                sendMails = sendMails.toUpperCase(); //Convert to upper case
            } else {
                String error = "Value not accepted for \"mail-notifications\" : " + sendMails + " . Admitted values  : "
                        + MailNotifications.MAIL_LEVEL_ALL + " , "
                        + MailNotifications.MAIL_LEVEL_SEVERE + " or "
                        + MailNotifications.MAIL_LEVEL_NONE;
                LOG.severe(error);
                throw new NuBotConfigException(error);
            }
        }


        //Create a new Instance
        options = new NuBotOptions(dualside, apiKey, apiSecret, nubitAddress, rpcUser,
                rpcPass, nudIp, nudPort, priceIncrement, txFee, submitLiquidity, exchangeName,
                executeOrders, verbose, pair, executeStrategyInterval,
                sendHipchat, sendMails, mailRecipient,
                emergencyTimeout, keepProceeds, aggregate, multipleCustodians,
                maxSellVolume, maxBuyVolume, distributeLiquidity, cpo);


        if (options == null) {
            throw new NuBotConfigException("error parsing configuration files");
        }

        return options;
    }

    /**
     * parse single json file
     *
     * @param filepath
     * @return
     * @throws ParseException
     */
    public static JSONObject parseSingleJsonFile(String filepath) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject fileJSON = (JSONObject) (parser.parse(FileSystem.readFromFile(filepath)));
        return fileJSON;
    }

    /**
     * get options value in dictionary
     *
     * @param fileJSON
     * @return
     */
    public static JSONObject getOptionsKey(JSONObject fileJSON) {
        JSONObject tempOptions = (JSONObject) fileJSON.get("options");
        return tempOptions;
    }

    /**
     * Concatenate a list of of files into a JSONObject
     *
     * @param filePaths
     * @return
     * @throws NuBotConfigException
     */
    public static JSONObject parseFiles(ArrayList<String> filePaths) throws NuBotConfigException {
        JSONObject optionsObject = new JSONObject();
        Map setMap = new HashMap();

        for (int i = 0; i < filePaths.size(); i++) {
            try {

                String filepath = filePaths.get(i);

                JSONObject fileJSON = parseSingleJsonFile(filepath);
                JSONObject tempOptions = getOptionsKey(fileJSON);

                Set tempSet = tempOptions.entrySet();
                for (Object o : tempSet) {
                    Map.Entry entry = (Map.Entry) o;
                    setMap.put(entry.getKey(), entry.getValue());
                }

            } catch (ParseException ex) {
                throw new NuBotConfigException("Parse exception \n" + ex.toString());
            }
        }

        JSONObject content = new JSONObject(setMap);
        optionsObject.put("options", content);
        return optionsObject;
    }
}
