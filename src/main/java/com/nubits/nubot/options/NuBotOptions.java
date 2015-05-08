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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.MailNotifications;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class NuBotOptions {

    private static final Logger LOG = LoggerFactory.getLogger(NuBotOptions.class.getName());

    /**
     * Custodian's public key to access the exchange
     */
    public String apiKey;

    /**
     * Custodian's secret key to access the exchange
     */
    public String apiSecret;

    /**
     * the email to which emergency email are sent
     */
    public String mailRecipient;

    /**
     * Name of the exchange where the bots operates
     */
    public String exchangeName;

    /**
     * If set to true, the bot will behave as a dual side custodian, if false as a sell side custodian.
     */
    public boolean dualSide;

    /**
     * valid currency pair for the specified eg. "nbt_usd"
     */
    public String pair;

    //Conditional settings with a default value

    /**
     * The RPC username of the Nu daemon
     */
    public String rpcUser;

    /**
     * The RPC password of the Nu daemon
     */
    public String rpcPass;

    /**
     * The public address where the custodial grant has been received
     */
    public String nubitAddress;

    /**
     * The RPC port of the Nu daemon
     */
    public int nudPort;

    //Optional settings with a default value  ----------------------------

    /**
     * The IP address of the machine that hosts the Nu Client
     */
    public String nudIp;

    /**
     * if set to false will disable email notifications
     */
    public String mailnotifications;

    /**
     * if set to false, the bot will not try to submit liquidity info.
     * If set to false, it will also allow the custodian to omit the declaration of nubitaddress , nudport , rpcuser and rpcpass
     */
    public boolean submitLiquidity;

    /**
     * if set to false the bot will print a warning instead of executing orders
     */
    public boolean executeOrders;

    /**
     * if set to true, will print on screen additional debug messages
     */
    public boolean verbose;

    /**
     * if set to false will disable hipchat notifications
     */
    public boolean hipchat;

    /**
     * if set to true, will sync with remote NPT and reset orders often
     */
    public boolean multipleCustodians;

    /**
     * If transaction fee not available from the exchange via api, this value will be used
     */
    public double txFee;

    /**
     * if working in sell-side mode, this value (considered USD) will be added to the sell price
     */
    public double priceIncrement;

    /**
     * max amount of minutes of consecutive failure. After those minute elapse, emergency procedure starts
     */
    public int emergencyTimeout;

    /**
     * Specific setting for KTm's proposal. Will keep the specified proceeds from sales apart instead of putting 100% of balance on buy .
     */
    public double keepProceeds;

    /**
     * maximum volume to put on sell walls.
     */
    public double maxSellVolume;

    /**
     * maximum volume to put on buy walls.
     */
    public double maxBuyVolume;

    /**
     * When this flag is true, the bot will put tier2 on orderbook at different price levels
     */
    public boolean distributeLiquidity;

    public double wallchangeThreshold;

    public double spread;

    // feeds

    public String mainFeed;
    public ArrayList<String> backupFeeds;

    /**
     * empty constructor. assumes safe creation of valid options
     */
    public NuBotOptions() {
        backupFeeds = new ArrayList<>();
    }

    public static String optionsToJson(NuBotOptions opt) {
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        //gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);
        return js;

    }


    public boolean isDualSide() {
        return dualSide;
    }

    public void setDualSide(boolean dualSide) {
        this.dualSide = dualSide;
    }

    public boolean requiresSecondaryPegStrategy() {
        //Return TRUE when it requires a dedicated NBT peg to something that is not USD
        if (this.pair.equalsIgnoreCase(CurrencyList.NBT_USD.toStringSep())) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isSubmitliquidity() {
        return submitLiquidity;
    }

    public void setSubmitLiquidity(boolean submitLiquidity) {
        this.submitLiquidity = submitLiquidity;
    }

    public boolean isExecuteOrders() {
        return executeOrders;
    }

    public void setExecuteOrders(boolean executeOrders) {
        this.executeOrders = executeOrders;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getNubitsAddress() {
        return nubitAddress;
    }

    public String getRpcUser() {
        return rpcUser;
    }

    public String getRpcPass() {
        return rpcPass;
    }

    public void setRpcPass(String rpcPass) {
        this.rpcPass = rpcPass;
    }

    public String getNudIp() {
        return nudIp;
    }

    public int getNudPort() {
        return nudPort;
    }

    public void setNudPort(int nudPort) {
        this.nudPort = nudPort;
    }

    public double getPriceIncrement() {
        return priceIncrement;
    }

    public double getTxFee() {
        return txFee;
    }

    public void setTxFee(double txFee) {
        this.txFee = txFee;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public CurrencyPair getPair() {
        CurrencyPair pair = CurrencyPair.getCurrencyPairFromString(this.pair);
        return pair;
    }

    public void setPair(CurrencyPair pair) {
        this.pair = pair.toStringSep();
    }

    public boolean isHipchat() {
        return hipchat;
    }

    public String getSendMailsLevel() {
        return mailnotifications;
    }

    public boolean sendMails() {
        boolean not_none = !(this.mailnotifications.equals(MailNotifications.MAIL_LEVEL_NONE));
        return not_none;
    }

    public String getMailRecipient() {
        return mailRecipient;
    }

    public boolean isMultipleCustodians() {
        return multipleCustodians;
    }

    public double getMaxSellVolume() {
        return maxSellVolume;
    }

    public double getMaxBuyVolume() {
        return maxBuyVolume;
    }

    public int getEmergencyTimeout() {
        return emergencyTimeout;
    }


    public double getKeepProceeds() {
        return keepProceeds;
    }

    public String getNubitAddress() {
        return nubitAddress;
    }

    public boolean isDistributeLiquidity() {
        return distributeLiquidity;
    }

    public double getSpread() {
        return this.spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public String getMainFeed() {
        return mainFeed;
    }

    public double getWallchangeThreshold() {
        return this.wallchangeThreshold;
    }

    public void setWallchangeThreshold(double wallchangeThreshold) {
        this.wallchangeThreshold = wallchangeThreshold;
    }

    public ArrayList<String> getBackupFeeds() {
        return backupFeeds;
    }

    @Override
    public String toString() {
        return toStringNoKeys(); //removes sensitive information
    }


    private String toStringNoKeys() {
        String toRet = "";

        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        //gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();

        String serializedOptionsStr = parser.toJson(this);
        org.json.simple.parser.JSONParser p = new org.json.simple.parser.JSONParser();
        try {
            JSONObject serializedOptionsJSON = (JSONObject) (p.parse(serializedOptionsStr));

            //Replace sensitive information
            String[] sensitiveKeys = {"apisecret", "apikey", "rpcpass", "apiSecret", "apiKey", "rpcPass"};
            String replaceString = "hidden";

            for (int i = 0; i < sensitiveKeys.length; i++) {
                if (serializedOptionsJSON.containsKey(sensitiveKeys[i])) {
                    serializedOptionsJSON.replace(sensitiveKeys[i], replaceString);
                }
            }

            toRet = serializedOptionsJSON.toString();
        } catch (org.json.simple.parser.ParseException e) {
            LOG.error(e.toString());
        }

        return toRet;
    }
}


