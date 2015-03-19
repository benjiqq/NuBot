package com.nubits.nubot.options;

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

import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.pricefeeds.FeedFacade;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.*;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * ParseOptions from one or several JSON files
 */
public class ParseOptions {

    private static String[] comp = {"secondarypeg", "exchangename", "apisecret", "mailrecipient", "dualside", "pair"};

    private static final Logger LOG = LoggerFactory.getLogger(ParseOptions.class.getName());


    /**
     * Parse Options from an array of paths
     *
     * @param paths
     * @return
     * @throws NuBotConfigException
     */
    /*public static NuBotOptions parseOptions(String[] paths) throws NuBotConfigException {
        ArrayList<String> filePaths = new ArrayList();
        filePaths.addAll(Arrays.asList(paths));
        JSONObject inputJSON = parseFiles(filePaths);
        JSONObject optionsJSON = (JSONObject) inputJSON.get("options");
        return parseOptionsFromJson(optionsJSON);
    }*/

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
     * the rules for valid configurations
     *
     * @param optionsJSON
     * @return
     * @throws NuBotConfigException
     */
    public static boolean isValidJSON(JSONObject optionsJSON) throws NuBotConfigException {

        for (int i = 0; i < comp.length; i++) {
            if (!containsIgnoreCase(optionsJSON, comp[i]))
                throw new NuBotConfigException("necessary key: " + comp[i]);
        }

        boolean submitLiquidity = (boolean) getIgnoreCase(optionsJSON, "submitliquidity");

        if (submitLiquidity) {

            String[] sneeded = {"nubitaddress", "rpcpass", "rpcuser", "nudport"};

            for (int i = 0; i < sneeded.length; i++) {
                String s = sneeded[i];
                if (!containsIgnoreCase(optionsJSON, s)) {
                    throw new NuBotConfigException("When submit-liquidity is set to true "
                            + "you need to declare a value for \"" +
                            s + "\" ");
                }
            }
        }

        return true;
    }

    /**
     * parseOptions from JSON into NuBotOptions
     * makes sure the parses object is valid
     *
     * @param optionsJSON
     * @return
     */
    public static NuBotOptions parseOptionsFromJson(JSONObject optionsJSON) throws NuBotConfigException {

        //default values for optional settings

        NuBotOptions options = NuBotOptionsDefault.defaultFactory();

        try {
            isValidJSON(optionsJSON);
        } catch (NuBotConfigException e) {
            throw e;
        }

        //First try to parse compulsory parameters
        options.exchangeName = (String) getIgnoreCase(optionsJSON, "exchangename");

        boolean supported = ExchangeFacade.exchangeSupported(options.exchangeName);
        LOG.info("exchange supported? " + options.exchangeName + " " + supported);
        if (!supported)
            throw new NuBotConfigException("exchange not supported");

        options.dualSide = (boolean) getIgnoreCase(optionsJSON, "dualSide");

        if (!options.exchangeName.equalsIgnoreCase(ExchangeFacade.CCEX)) { //for ccex this parameter can be omitted
            if (!containsIgnoreCase(optionsJSON, "apiKey")) {
                throw new NuBotConfigException("The apikey parameter is compulsory.");
            } else {
                options.apiKey = (String) getIgnoreCase(optionsJSON, "apikey");
            }
        }

        options.apiKey = (String) getIgnoreCase(optionsJSON, "apikey");

        options.apiSecret = (String) getIgnoreCase(optionsJSON, "apisecret");

        options.mailRecipient = (String) getIgnoreCase(optionsJSON, "mailrecipient");

        String pairStr = (String) getIgnoreCase(optionsJSON, "pair");
        options.pair = CurrencyPair.getCurrencyPairFromString(pairStr);

        //test if configuration is supported
        if (!isSupportedPair(options.getPair())) {
            throw new NuBotConfigException("This bot doesn't work yet with trading pair " + options.getPair().toString());
        }


        boolean aggregate = true; //true only for USD
        if (!options.pair.getPaymentCurrency().getCode().equalsIgnoreCase("USD")) {
            options.aggregate = false; //default to false
        }


        //Based on the pair, set a parameter do define whether setting SecondaryPegOptionsJSON i necessary or not
        //boolean requireCryptoOptions = PegOptions.requiresSecondaryPegStrategy(pair);
        //org.json.JSONObject pegOptionsJSON;

        options.secondarypeg = (boolean) optionsJSON.get("secondarypeg");

        try{
            parseSecondary(options, optionsJSON);
        }catch(NuBotConfigException e){
            if (options.secondarypeg) {
                throw e;
            }
        }



        //---- optional settings ----

        if (containsIgnoreCase(optionsJSON, "nudip")) {
            options.nudIp = (String) getIgnoreCase(optionsJSON, "nudip");
        }

        if (containsIgnoreCase(optionsJSON, "priceincrement")) {
            options.priceIncrement = Utils.getDouble(getIgnoreCase(optionsJSON, "priceincrement"));
        }

        if (containsIgnoreCase(optionsJSON, "txfee")) {
            options.txFee = Utils.getDouble(getIgnoreCase(optionsJSON, "txfee"));
        }

        if (containsIgnoreCase(optionsJSON, "submitliquidity")) {
            options.submitLiquidity = (boolean) getIgnoreCase(optionsJSON, "submitliquidity");
        }

        if (containsIgnoreCase(optionsJSON, "maxsellordervolume")) {
            options.maxSellVolume = Utils.getDouble(getIgnoreCase(optionsJSON, "maxsellordervolume"));
        }

        if (containsIgnoreCase(optionsJSON, "maxbuyordervolume")) {
            options.maxBuyVolume = Utils.getDouble(getIgnoreCase(optionsJSON, "maxbuyordervolume"));
        }

        if (containsIgnoreCase(optionsJSON, "executeorders")) {
            options.executeOrders = (boolean) getIgnoreCase(optionsJSON, "executeorders");
        }

        if (containsIgnoreCase(optionsJSON, "verbose")) {
            options.verbose = (boolean) getIgnoreCase(optionsJSON, "verbose");
        }

        if (containsIgnoreCase(optionsJSON, "hipchat")) {
            options.sendHipchat = (boolean) getIgnoreCase(optionsJSON, "hipchat");
        }

        if (containsIgnoreCase(optionsJSON, "emergencytimeout")) {
            long emergencyTimeoutLong = (long) getIgnoreCase(optionsJSON, "emergencytimeout");
            options.emergencyTimeout = (int) emergencyTimeoutLong;
        }

        if (containsIgnoreCase(optionsJSON, "keepproceeds")) {
            options.keepProceeds = Utils.getDouble((getIgnoreCase(optionsJSON, "keepproceeds")));
        }

        if (containsIgnoreCase(optionsJSON, "multiplecustodians")) {
            options.multipleCustodians = (boolean) getIgnoreCase(optionsJSON, "multiplecustodians");
        }

        if (containsIgnoreCase(optionsJSON, "distributeliquidity")) {
            options.distributeLiquidity = (boolean) getIgnoreCase(optionsJSON, "distributeliquidity");
        }

        //Now require the parameters only if submitLiquidity is true, otherwise can use the default value


        if (containsIgnoreCase(optionsJSON, "nubitaddress")) {
            options.nubitAddress = (String) getIgnoreCase(optionsJSON, "nubitaddress");
        }

        if (containsIgnoreCase(optionsJSON, "rpcpass")) {
            options.rpcPass = (String) getIgnoreCase(optionsJSON, "rpcpass");
        }

        if (containsIgnoreCase(optionsJSON, "rpcuser")) {
            options.rpcUser = (String) getIgnoreCase(optionsJSON, "rpcuser");
        }

        if (containsIgnoreCase(optionsJSON, "nudport")) {
            long nudPortlong = (long) getIgnoreCase(optionsJSON, "nudport");
            options.nudPort = (int) nudPortlong;
        }


        if (containsIgnoreCase(optionsJSON, "mailnotifications")) {
            String tmpsendMails = (String) getIgnoreCase(optionsJSON, "mailnotifications");
            if (tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_ALL)
                    || tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_NONE)
                    || tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_SEVERE)) {
                options.sendMails = tmpsendMails.toUpperCase(); //Convert to upper case
            } else {
                String error = "Value not accepted for \"mail-notifications\" : " + tmpsendMails + " . Admitted values  : "
                        + MailNotifications.MAIL_LEVEL_ALL + " , "
                        + MailNotifications.MAIL_LEVEL_SEVERE + " or "
                        + MailNotifications.MAIL_LEVEL_NONE;
                LOG.error(error);
                throw new NuBotConfigException(error);
            }
        }

        return options;
    }

    public static void parseSecondary(NuBotOptions options, JSONObject optionsJSON) throws NuBotConfigException {

        if (!containsIgnoreCase(optionsJSON, "mainfeed"))
            throw new NuBotConfigException("mainfeed necessary parameter");

        options.mainFeed = (String) optionsJSON.get("mainfeed");

        if (!containsIgnoreCase(optionsJSON, "backupfeeds"))
            throw new NuBotConfigException("backupfeed necessary parameter");

        ArrayList<String> backupFeedNames = new ArrayList<>();

        //Iterate on backupFeeds

        JSONArray bfeeds = (JSONArray) optionsJSON.get("backupfeeds");

        LOG.info("bfeeds " + bfeeds);

        if (bfeeds.size() < 2) {
            throw new NuBotConfigException("The bot requires at least two backup data feeds to run");
        }
        for (int i = 0; i < bfeeds.size(); i++) {
            try {
                String feedname = (String)bfeeds.get(i);
                if (!FeedFacade.isValidFeed(feedname))
                    throw new NuBotConfigException("invalid feed configured");
                else
                    options.backupFeedNames.add(feedname);
            } catch (JSONException ex) {
                throw new NuBotConfigException("" + ex);
            }
        }

        options.secondarypeg = (boolean) getIgnoreCase(optionsJSON, "secondarypeg");

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



    }

    //TODO: redundant. this handles webUI json parsing
    public static NuBotOptions parsePost(JSONObject postJson) throws Exception {

        String variableset = "none";
        NuBotOptions newopt = new NuBotOptions();

        if (containsIgnoreCase(postJson, "exchangename")) {
            newopt.exchangeName ="" + getIgnoreCase(postJson,"exchangename");
        }

        if (containsIgnoreCase(postJson, "apikey")) {
            newopt.apiKey = "" + getIgnoreCase(postJson, "apikey");
        }

        if (containsIgnoreCase(postJson, "apisecret")) {
            newopt.apiSecret = "" + getIgnoreCase(postJson, "apisecret");
        }

        if (containsIgnoreCase(postJson, "mailRecipient")) {
            newopt.mailRecipient = "" + getIgnoreCase(postJson,"mailRecipient");
        }

        if (containsIgnoreCase(postJson, "dualside")) {
            newopt.dualSide = (boolean) getIgnoreCase(postJson,"dualside");
        }

        if (containsIgnoreCase(postJson, "multiplecustodians")) {
            newopt.multipleCustodians =(boolean) getIgnoreCase(postJson,"multiplecustodians");
        }

        if (containsIgnoreCase(postJson, "submitliquidity")) {
            newopt.submitLiquidity= (boolean) getIgnoreCase(postJson,"submitliquidity");
        }

        if (containsIgnoreCase(postJson, "secondarypeg")) {
            newopt.secondarypeg =(boolean) getIgnoreCase(postJson,"secondarypeg");
        }

        if (containsIgnoreCase(postJson, "executeorders")) {
            newopt.executeOrders =(boolean) getIgnoreCase(postJson,"executeorders");
        }

        if (containsIgnoreCase(postJson, "verbose")) {
            newopt.verbose =(boolean) getIgnoreCase(postJson,"verbose");
        }

        if (containsIgnoreCase(postJson, "hipchat")) {
            newopt.sendHipchat =(boolean) getIgnoreCase(postJson,"hipchat");
        }

        if (containsIgnoreCase(postJson, "nubitaddress")) {
            newopt.nubitAddress = "" + getIgnoreCase(postJson,"nubitaddress");
        }

        if (containsIgnoreCase(postJson, "rpcUser")) {
            newopt.rpcUser = "" + getIgnoreCase(postJson,"rpcUser");
        }

        if (containsIgnoreCase(postJson,"rpcPass")) {
            newopt.rpcPass = "" + getIgnoreCase(postJson,"rpcPass");
        }

        if (containsIgnoreCase(postJson, "nudIp")) {
            newopt.nudIp = "" + getIgnoreCase(postJson,"nudIp");
        }

        if (containsIgnoreCase(postJson, "sendMails")) {
            newopt.sendMails = "" + getIgnoreCase(postJson,"sendMails");
        }


        if (postJson.containsKey("nudport")) {
            try {
                int newv = (new Integer("" + getIgnoreCase(postJson,"nudport"))).intValue();
                newopt.setNudPort(newv);
            } catch (Exception e) {
                //TODO
            }
        }

        if (postJson.containsKey("pair")) {
            String p = "" + getIgnoreCase(postJson,"pair");
            CurrencyPair newpair = CurrencyPair.getCurrencyPairFromString(p, "_");
            newopt.setPair(newpair);
        }


        return newopt;
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


    /**
     * check if pair is supported
     *
     * @param pair
     * @return
     */
    public static boolean isSupportedPair(CurrencyPair pair) {
        if (pair.equals(CurrencyList.NBT_USD)
                || pair.equals(CurrencyList.NBT_BTC)
                || pair.equals(CurrencyList.BTC_NBT)
                || pair.equals(CurrencyList.NBT_EUR)
                || pair.equals(CurrencyList.NBT_CNY)
                || pair.equals(CurrencyList.NBT_PPC)) {
            return true;
        } else {
            return false;
        }
    }


}
