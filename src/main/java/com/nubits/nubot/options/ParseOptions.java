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

package com.nubits.nubot.options;

import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.pricefeeds.FeedFacade;
import com.nubits.nubot.utils.FilesystemUtils;
import com.nubits.nubot.utils.Utils;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * ParseOptions from one JSON files
 */
public class ParseOptions {

    private static final Logger LOG = LoggerFactory.getLogger(ParseOptions.class.getName());

    public static String exchangename = "exchangename";
    public static String apikey = "apikey";
    public static String apisecret = "apisecret";
    public static String mailrecipient = "mailrecipient";
    public static String pair = "pair";
    public static String nudip = "nudip";
    public static String priceincrement = "priceincrement";
    public static String txfee = "txfee";
    public static String submitliquidity = "submitliquidity";
    public static String maxsellvolume = "maxsellvolume";
    public static String maxbuyvolume = "maxbuyvolume";
    public static String executeorders = "executeorders";
    public static String dualside = "dualside";
    public static String verbose = "verbose";
    public static String hipchat = "hipchat";
    public static String emergencytimeout = "emergencytimeout";
    public static String keepproceeds = "keepproceeds";
    public static String multiplecustodians = "multiplecustodians";
    public static String nubitaddress = "nubitaddress";
    public static String rpcpass = "rpcpass";
    public static String rpcuser = "rpcuser";
    public static String nudport = "nudport";
    public static String mailnotifications = "mailnotifications";
    public static String mainfeed = "mainfeed";
    public static String backupfeeds = "backupfeeds";
    public static String wallchangethreshold = "wallchangethreshold";
    public static String spread = "spread";
    //public static String distributeliquidity = "distributeliquidity";

    public static String[] allkeys = {
            exchangename,
            apikey,
            apisecret,
            mailrecipient,
            pair,
            nudip,
            priceincrement,
            txfee,
            submitliquidity,
            maxsellvolume,
            maxbuyvolume,
            executeorders,
            dualside,
            verbose,
            hipchat,
            emergencytimeout,
            keepproceeds,
            multiplecustodians,
            nubitaddress,
            rpcpass,
            rpcuser,
            nudport,
            mailnotifications,
            mainfeed,
            backupfeeds,
            wallchangethreshold,
            spread};
            //distributeliquidity


    private static String[] comp = {exchangename, apisecret, mailrecipient, dualside, pair};


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

        for (int i = 0; i < allkeys.length; i++) {
            if (!containsIgnoreCase(optionsJSON, allkeys[i]))
                throw new NuBotConfigException("necessary key: " + allkeys[i]);
        }

        boolean submitLiquidity = (boolean) getIgnoreCase(optionsJSON, submitliquidity);

        if (submitLiquidity) {

            String[] sneeded = {nubitaddress, rpcpass, rpcuser, nudport};

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

        //NuBotOptions options = NuBotOptionsDefault.defaultFactory();
        NuBotOptions options = new NuBotOptions();

        try {
            isValidJSON(optionsJSON);
        } catch (NuBotConfigException e) {
            throw e;
        }

        //First try to parse compulsory parameters
        options.exchangeName = (String) getIgnoreCase(optionsJSON, exchangename);

        boolean supported = ExchangeFacade.supportedExchange(options.exchangeName);
        LOG.trace("exchange supported? " + options.exchangeName + " " + supported);
        if (!supported)
            throw new NuBotConfigException("exchange " + options.exchangeName + " not supported");

        try {
            options.dualSide = (boolean) getIgnoreCase(optionsJSON, dualside);
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast dualSide to boolean " + e);
        }

        if (!options.exchangeName.equalsIgnoreCase(ExchangeFacade.CCEX)) { //for ccex this parameter can be omitted
            if (!containsIgnoreCase(optionsJSON, apikey)) {
                throw new NuBotConfigException("The apikey parameter is compulsory.");
            } else {
                options.apiKey = (String) getIgnoreCase(optionsJSON, apikey);
            }
        }

        options.apiKey = (String) getIgnoreCase(optionsJSON, apikey);

        options.apiSecret = (String) getIgnoreCase(optionsJSON, apisecret);

        options.mailRecipient = (String) getIgnoreCase(optionsJSON, mailrecipient);

        options.pair = (String) getIgnoreCase(optionsJSON, pair);

        //test if configuration is supported
        if (!isSupportedPair(options.getPair())) {
            throw new NuBotConfigException("This bot doesn't work yet with trading pair " + options.getPair().toString());
        }

        boolean aggregate = true; //true only for USD
        if (!options.getPair().getPaymentCurrency().getCode().equalsIgnoreCase("USD")) {
            options.aggregate = false; //default to false
        }

        //Based on the pair, set a parameter do define whether setting SecondaryPegOptionsJSON i necessary or not
        //boolean requireCryptoOptions = PegOptions.requiresSecondaryPegStrategy(pair);
        //org.json.JSONObject pegOptionsJSON;

        LOG.trace("options requiresSecondaryPegStrategy: " + options.requiresSecondaryPegStrategy());

        if (options.requiresSecondaryPegStrategy()) {
            try {
                parseSecondary(options, optionsJSON);
            } catch (NuBotConfigException e) {
                throw e;
            }
        }


        options.nudIp = (String) getIgnoreCase(optionsJSON, nudip);

        options.priceIncrement = Utils.getDouble(getIgnoreCase(optionsJSON, priceincrement));

        options.txFee = Utils.getDouble(getIgnoreCase(optionsJSON, txfee));

        try {
            options.submitLiquidity = (boolean) getIgnoreCase(optionsJSON, submitliquidity);
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast submitLiquidity to boolean " + e);
        }
        options.maxSellVolume = Utils.getDouble(getIgnoreCase(optionsJSON, maxsellvolume));
        options.maxBuyVolume = Utils.getDouble(getIgnoreCase(optionsJSON, maxbuyvolume));
        try {
            options.executeOrders = (boolean) getIgnoreCase(optionsJSON, executeorders);
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast executeOrders to boolean " + e);
        }
        options.verbose = (boolean) getIgnoreCase(optionsJSON, verbose);
        options.hipchat = (boolean) getIgnoreCase(optionsJSON, hipchat);

        try {
            String lstr = "" + getIgnoreCase(optionsJSON, emergencytimeout);
            LOG.debug("lstr " + lstr);
            int emergencyTimeoutLong = new Integer(lstr).intValue();
            options.emergencyTimeout = emergencyTimeoutLong;
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast emergencytimeout to int " + e);
        }

        options.keepProceeds = Utils.getDouble((getIgnoreCase(optionsJSON, keepproceeds)));
        try {
            options.multipleCustodians = (boolean) getIgnoreCase(optionsJSON, multiplecustodians);
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast multipleCustodians to boolean " + e);
        }



        //TOOO distributeLiquidity not implemented
        /*try {
            options.distributeLiquidity = (boolean) getIgnoreCase(optionsJSON, distributeliquidity);
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast distributeLiquidity to boolean " + e);
        }*/


        options.nubitAddress = (String) getIgnoreCase(optionsJSON, nubitaddress);
        options.rpcPass = (String) getIgnoreCase(optionsJSON, rpcpass);
        options.rpcUser = (String) getIgnoreCase(optionsJSON, rpcuser);
        try {
            String pstr = "" + getIgnoreCase(optionsJSON, nudport);
            int nudPortlong = new Integer(pstr).intValue();
            options.nudPort = nudPortlong;
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast nudPortlong to long " + e);
        }

        String tmpsendMails = (String) getIgnoreCase(optionsJSON, mailnotifications);
        if (tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_ALL)
                || tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_NONE)
                || tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_SEVERE)) {
            options.mailnotifications = tmpsendMails.toUpperCase(); //Convert to upper case
        } else {
            String error = "Value not accepted for \"mail-notifications\" : " + tmpsendMails + " . Admitted values  : "
                    + MailNotifications.MAIL_LEVEL_ALL + " , "
                    + MailNotifications.MAIL_LEVEL_SEVERE + " or "
                    + MailNotifications.MAIL_LEVEL_NONE;
            LOG.error(error);
            throw new NuBotConfigException(error);
        }

        return options;
    }

    public static void parseSecondary(NuBotOptions options, JSONObject optionsJSON) throws NuBotConfigException {

        options.mainFeed = (String) getIgnoreCase(optionsJSON, mainfeed);

        options.backupFeeds = new ArrayList<>();

        //Iterate on backupFeeds
        JSONArray bfeeds = null;
        try{
            bfeeds = (JSONArray) getIgnoreCase(optionsJSON, backupfeeds);
        }catch (Exception e){
            throw new NuBotConfigException("can't parse array " + e);
        }

        if (bfeeds.size() < 2) {
            throw new NuBotConfigException("The bot requires at least two backup data feeds to run");
        }
        for (int i = 0; i < bfeeds.size(); i++) {
            try {
                String feedname = (String) bfeeds.get(i);
                if (!FeedFacade.isValidFeed(feedname))
                    throw new NuBotConfigException("invalid feed configured");
                else
                    options.backupFeeds.add(feedname);
            } catch (JSONException ex) {
                throw new NuBotConfigException("parse feeds json error" + ex);
            }
        }

        options.wallchangeThreshold = Utils.getDouble(getIgnoreCase(optionsJSON, wallchangethreshold));

        options.spread = Utils.getDouble(getIgnoreCase(optionsJSON, spread));

        if (options.spread != 0) {
            LOG.warn("You are using the \"spread\" != 0 , which is not reccomented by Nu developers for purposes different from testing.");
        }

    }

    public static NuBotOptions parsePost(JSONObject postJson) throws Exception {

        NuBotOptions newopt = null;

        try {
            //Check if NuBot has valid parameters

            newopt = ParseOptions.parseOptionsFromJson(postJson);
            LOG.debug("parse post opt: " + newopt);

        } catch (NuBotConfigException e) {
            throw e;

        }

        return newopt;
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
        JSONObject fileJSON = (JSONObject) (parser.parse(FilesystemUtils.readFromFile(filepath)));
        return fileJSON;
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
