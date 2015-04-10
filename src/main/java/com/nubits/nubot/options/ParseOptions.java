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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * ParseOptions from one JSON files
 */
public class ParseOptions {

    private static final Logger LOG = LoggerFactory.getLogger(ParseOptions.class.getName());

    public static String exchangename = "exchangeName";
    public static String apikey = "apiKey";
    public static String apisecret = "apiSecret";
    public static String mailrecipient = "mailRecipient";
    public static String pair = "pair";
    public static String nudip = "nudIp";
    public static String priceincrement = "priceIncrement";
    public static String txfee = "txfee";
    public static String submitliquidity = "submitLiquidity";
    public static String maxsellvolume = "maxSellVolume";
    public static String maxbuyvolume = "maxBuyVolume";
    public static String executeorders = "executeOrders";
    public static String dualside = "dualSide";
    public static String verbose = "verbose";
    public static String hipchat = "hipchat";
    public static String emergencytimeout = "emergencyTimeout";
    public static String keepproceeds = "keepProceeds";
    public static String multiplecustodians = "multipleCustodians";
    public static String nubitaddress = "nubitAddress";
    public static String rpcpass = "rpcPass";
    public static String rpcuser = "rpcUser";
    public static String nudport = "nudPort";
    public static String mailnotifications = "mailnotifications";
    public static String mainfeed = "mainfeed";
    public static String backupfeeds = "backupFeeds";
    public static String wallchangethreshold = "wallchangeThreshold";
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

    private static String[] boolkeys = {submitliquidity, hipchat, verbose, executeorders, dualside, multiplecustodians};


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

        for (int i = 0; i < boolkeys.length; i++) {
            try {
                boolean b = (boolean) getIgnoreCase(optionsJSON, boolkeys[i]);
            } catch (Exception e) {
                throw new NuBotConfigException("can't parse to boolean: " + boolkeys[i]);
            }
        }

        try {
            String lstr = "" + getIgnoreCase(optionsJSON, emergencytimeout);
            int emergencyTimeoutLong = new Integer(lstr).intValue();
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast emergencytimeout to int " + e);
        }

        try {
            int nudPortlong = new Integer("" + getIgnoreCase(optionsJSON, nudport)).intValue();
        } catch (Exception e) {
            throw new NuBotConfigException("can not cast nudPortlong to long " + e);
        }

        return true;
    }

    private static ArrayList parseBackupFeeds(JSONObject optionsJSON) throws NuBotConfigException {

        ArrayList backupFeeds = new ArrayList<>();

        //Iterate on backupFeeds
        JSONArray bfeeds = null;
        try {
            bfeeds = (JSONArray) getIgnoreCase(optionsJSON, backupfeeds);
        } catch (Exception e) {
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
                    backupFeeds.add(feedname);

            } catch (JSONException ex) {
                throw new NuBotConfigException("parse feeds json error" + ex);
            }
        }

        return backupFeeds;
    }

    private static String parseMails(JSONObject optionsJSON) throws NuBotConfigException {
        String tmpsendMails = (String) getIgnoreCase(optionsJSON, mailnotifications);

        if (tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_ALL)
                || tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_NONE)
                || tmpsendMails.equalsIgnoreCase(MailNotifications.MAIL_LEVEL_SEVERE)) {
            return tmpsendMails.toUpperCase(); //Convert to upper case
        } else {
            String error = "Value not accepted for \"mail-notifications\" : " + tmpsendMails + " . Admitted values  : "
                    + MailNotifications.MAIL_LEVEL_ALL + " , "
                    + MailNotifications.MAIL_LEVEL_SEVERE + " or "
                    + MailNotifications.MAIL_LEVEL_NONE;
            LOG.error(error);
            throw new NuBotConfigException(error);
        }
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
        options.dualSide = (boolean) getIgnoreCase(optionsJSON, dualside);
        options.apiKey = (String) getIgnoreCase(optionsJSON, apikey);
        options.apiSecret = (String) getIgnoreCase(optionsJSON, apisecret);
        options.mailRecipient = (String) getIgnoreCase(optionsJSON, mailrecipient);
        options.pair = (String) getIgnoreCase(optionsJSON, pair);
        options.nudIp = (String) getIgnoreCase(optionsJSON, nudip);
        options.priceIncrement = Utils.getDouble(getIgnoreCase(optionsJSON, priceincrement));
        options.txFee = Utils.getDouble(getIgnoreCase(optionsJSON, txfee));
        options.submitLiquidity = (boolean) getIgnoreCase(optionsJSON, submitliquidity);
        options.maxSellVolume = Utils.getDouble(getIgnoreCase(optionsJSON, maxsellvolume));
        options.maxBuyVolume = Utils.getDouble(getIgnoreCase(optionsJSON, maxbuyvolume));
        options.executeOrders = (boolean) getIgnoreCase(optionsJSON, executeorders);
        options.verbose = (boolean) getIgnoreCase(optionsJSON, verbose);
        options.hipchat = (boolean) getIgnoreCase(optionsJSON, hipchat);
        options.emergencyTimeout = new Integer("" + getIgnoreCase(optionsJSON, emergencytimeout)).intValue();
        options.keepProceeds = Utils.getDouble((getIgnoreCase(optionsJSON, keepproceeds)));
        options.multipleCustodians = (boolean) getIgnoreCase(optionsJSON, multiplecustodians);
        options.nubitAddress = (String) getIgnoreCase(optionsJSON, nubitaddress);
        options.rpcPass = (String) getIgnoreCase(optionsJSON, rpcpass);
        options.rpcUser = (String) getIgnoreCase(optionsJSON, rpcuser);
        options.nudPort = new Integer("" + getIgnoreCase(optionsJSON, nudport)).intValue();
        options.mainFeed = (String) getIgnoreCase(optionsJSON, mainfeed);
        options.wallchangeThreshold = Utils.getDouble(getIgnoreCase(optionsJSON, wallchangethreshold));
        options.spread = Utils.getDouble(getIgnoreCase(optionsJSON, spread));
        options.backupFeeds = parseBackupFeeds(optionsJSON);
        options.mailnotifications = parseMails(optionsJSON);


        if (options.spread != 0) {
            LOG.warn("You are using the \"spread\" != 0 , which is not reccomented by Nu developers for purposes different from testing.");
        }

        //valididty tests

        boolean supported = ExchangeFacade.supportedExchange(options.exchangeName);
        LOG.trace("exchange supported? " + options.exchangeName + " " + supported);
        if (!supported)
            throw new NuBotConfigException("exchange " + options.exchangeName + " not supported");

        //test if configuration is supported
        if (!isSupportedPair(options.getPair())) {
            throw new NuBotConfigException("This bot doesn't work yet with trading pair " + options.getPair().toString());
        }

        return options;
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
