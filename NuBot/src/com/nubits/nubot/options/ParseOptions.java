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

    private static String[] comp = {"secondarypegoptions", "exchangename", "apisecret", "mailrecipient", "dualside", "pair"};

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
        } catch (Exception e) {
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
     * makes sure the parses object is valid
     *
     * @param optionsJSON
     * @return
     */
    public static NuBotOptions parseOptionsFromJson(JSONObject optionsJSON) throws NuBotConfigException {

        NuBotOptions options = new NuBotOptions();

        for (int i = 0; i < comp.length; i++) {
            if (!containsIgnoreCase(optionsJSON, comp[i]))
                throw new NuBotConfigException("necessary key: " + comp[i]);
        }


        boolean secondarypeg = false;

        double wallchangeThreshold = -1;
        double spread = -1;
        double distanceThreshold = -1;

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

        if (!exchangeName.equalsIgnoreCase(Constant.CCEX)) { //for ccex this parameter can be omitted
            if (!containsIgnoreCase(optionsJSON, "apiKey")) {
                throw new NuBotConfigException("The apikey parameter is compulsory.");
            } else {
                options.apiKey = (String) getIgnoreCase(optionsJSON, "apikey");
            }
        }

        String apiKey = (String) getIgnoreCase(optionsJSON, "apikey");

        String apiSecret = (String) getIgnoreCase(optionsJSON, "apisecret");

        String mailRecipient = (String) getIgnoreCase(optionsJSON, "mailrecipient");

        String pairStr = (String) getIgnoreCase(optionsJSON, "pair");
        CurrencyPair pair = CurrencyPair.getCurrencyPairFromString(pairStr, "_");

        boolean aggregate = true; //true only for USD
        if (!pair.getPaymentCurrency().getCode().equalsIgnoreCase("USD")) {
            aggregate = false; //default to false
        }


        //Based on the pair, set a parameter do define whether setting SecondaryPegOptionsJSON i necessary or not
        //boolean requireCryptoOptions = PegOptions.requiresSecondaryPegStrategy(pair);
        //org.json.JSONObject pegOptionsJSON;

        boolean speg = containsIgnoreCase(optionsJSON, "secondarypegoptions");

        if (speg) {
            parseSecondary(options, optionsJSON);
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

        if (containsIgnoreCase(optionsJSON, "maxsellordervolume")) {
            maxSellVolume = Utils.getDouble(getIgnoreCase(optionsJSON, "maxsellordervolume"));
        }

        if (containsIgnoreCase(optionsJSON, "maxbuyordervolume")) {
            maxBuyVolume = Utils.getDouble(getIgnoreCase(optionsJSON, "maxbuyordervolume"));
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

        if (containsIgnoreCase(optionsJSON, "emergencytimeout")) {
            long emergencyTimeoutLong = (long) getIgnoreCase(optionsJSON, "emergencytimeout");
            emergencyTimeout = (int) emergencyTimeoutLong;
        }

        if (containsIgnoreCase(optionsJSON, "keepproceeds")) {
            keepProceeds = Utils.getDouble((getIgnoreCase(optionsJSON, "keepproceeds")));
        }

        if (containsIgnoreCase(optionsJSON, "multiplecustodians")) {
            multipleCustodians = (boolean) getIgnoreCase(optionsJSON, "multiplecustodians");
        }

        if (containsIgnoreCase(optionsJSON, "distributeliquidity")) {
            distributeLiquidity = (boolean) getIgnoreCase(optionsJSON, "distributeliquidity");
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

        if (containsIgnoreCase(optionsJSON, "mailnotifications")) {
            sendMails = (String) getIgnoreCase(optionsJSON, "mailnotifications");
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
                maxSellVolume, maxBuyVolume, distributeLiquidity, secondarypeg, wallchangeThreshold, spread, distanceThreshold);


        if (options == null) {
            throw new NuBotConfigException("error parsing configuration files");
        }

        return options;
    }

    public static void parseSecondary(NuBotOptions options, JSONObject optionsJSON) throws NuBotConfigException {

        if (!containsIgnoreCase(optionsJSON, "mainfeed"))
            throw new NuBotConfigException("mainfeed necessary parameter");

        options.mainFeed = (String) optionsJSON.get("mainfeed");

        //ArrayList<String> backupFeedNames = new ArrayList<>();
        //org.json.JSONObject dataJson = (org.json.JSONObject) optionsJSON.get("backupfeeds");

        //Iterate on backupFeeds

        /*String names[] = org.json.JSONObject.getNames(dataJson);
        if (names.length < 2) {
            throw new NuBotConfigException("The bot requires at least two backup data feeds to run");
        }
        for (int i = 0; i < names.length; i++) {
            try {
                org.json.JSONObject tempJson = dataJson.getJSONObject(names[i]);
                options.backupFeedNames.add((String) tempJson.get("name"));
            } catch (JSONException ex) {
                throw new NuBotConfigException(ex.toString());
            }
        }*/

        options.secondarypeg = (boolean) getIgnoreCase(optionsJSON, "secondarypegoptions");

        if (!containsIgnoreCase(optionsJSON, "wallchangeThreshold"))
            throw new NuBotConfigException("wallchangeThreshold needed if secondary peg defined");
        else
            options.wallchangeThreshold = (double) getIgnoreCase(optionsJSON, "wallchangeThreshold");


        if (!containsIgnoreCase(optionsJSON, "spread"))
            throw new NuBotConfigException("spread needed if secondary peg defined");
        else
            options.spread = (double) getIgnoreCase(optionsJSON, "spread");

        if (!containsIgnoreCase(optionsJSON, "distanceThreshold"))
            throw new NuBotConfigException("distanceThreshold needed if secondary peg defined");
        else
            options.distanceThreshold = (double) getIgnoreCase(optionsJSON, "distanceThreshold");

        if (options.spread != 0) {
            throw new NuBotConfigException("You are using the \"spread\" != 0 , which is not reccomented by Nu developers for purposes different from testing.");
        }


        options.mainFeed = (String) optionsJSON.get("main-feed");


    }

    public static void parseBackupfeeds() {
                //            ArrayList<String> backupFeedNames = new ArrayList<>();
        //            org.json.JSONObject dataJson = (org.json.JSONObject) optionsJSON.get("backup-feeds");
        //
        //            //Iterate on backupFeeds
        //
        //            String names[] = org.json.JSONObject.getNames(dataJson);
        //            if (names.length < 2) {
        //                throw new NuBotConfigException("The bot requires at least two backup data feeds to run");
        //            }
        //            for (int i = 0; i < names.length; i++) {
        //                try {
        //                    org.json.JSONObject tempJson = dataJson.getJSONObject(names[i]);
        //                    backupFeedNames.add((String) tempJson.get("name"));
        //
        // }
        //            }
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
                //JSONObject tempOptions = getOptionsKey(fileJSON);

                Set tempSet = fileJSON.entrySet();
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
