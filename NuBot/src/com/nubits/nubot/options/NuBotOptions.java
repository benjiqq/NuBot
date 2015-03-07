/*
 * Copyright (C) 2014-2015 Nu Development Team
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.CurrencyPair;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.nubits.nubot.notifications.MailNotifications;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class NuBotOptions {

    private static final Logger LOG = LoggerFactory.getLogger(NuBotOptions.class.getName());

    //Compulsory settings ----------------------------

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
    public CurrencyPair pair;

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
     * TODO: rename mailnotifications
     */
    public String sendMails;

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
    public boolean sendHipchat;

    /**
     * TODO: describe this
     */
    public boolean aggregate;

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
     * TODO
     */
    public boolean distributeLiquidity;

    /**
     * TODO
     */
    public int executeStrategyInterval; //disabled

    /**
     * TODO
     */
    public int sendLiquidityInterval; //disabled

    // ------- secondary peg ----------

    public boolean secondarypeg;

    public double wallchangeThreshold;

    public double spread;

    public double distanceThreshold;

    // feeds

    public String mainFeed;
    public ArrayList<String> backupFeedNames;

    /**
     * empty constructor. assumes safe creation of valid options
     */
    public NuBotOptions() {

    }


    /**
     * standard constructor with all options
     */
    public NuBotOptions(boolean dualSide, String apiKey, String apiSecret, String nubitAddress,
                        String rpcUser, String rpcPass, String nudIp, int nudPort, double priceIncrement,
                        double txFee, boolean sendRPC, String exchangeName, boolean executeOrders, boolean verbose, CurrencyPair pair,
                        int executeStrategyInterval, boolean sendHipchat,
                        String sendMails, String mailRecipient, int emergencyTimeout, double keepProceeds, boolean aggregate,
                        boolean multipleCustodians, double maxSellVolume, double maxBuyVolume,
                        boolean distributeLiquidity, boolean secondarypeg,
                        double wallchangeThreshold, double spread, double distanceThreshold) {
        this.dualSide = dualSide;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.nubitAddress = nubitAddress;
        this.rpcUser = rpcUser;
        this.rpcPass = rpcPass;
        this.nudIp = nudIp;
        this.nudPort = nudPort;
        this.priceIncrement = priceIncrement;
        this.txFee = txFee;
        this.submitLiquidity = sendRPC;
        this.exchangeName = exchangeName;
        this.verbose = verbose;
        this.executeOrders = executeOrders;
        this.pair = pair;
        this.executeStrategyInterval = executeStrategyInterval;
        this.sendHipchat = sendHipchat;
        this.sendMails = sendMails;
        this.mailRecipient = mailRecipient;
        this.emergencyTimeout = emergencyTimeout;
        this.keepProceeds = keepProceeds;
        this.aggregate = aggregate;
        this.multipleCustodians = multipleCustodians;
        this.maxSellVolume = maxSellVolume;
        this.maxBuyVolume = maxBuyVolume;
        this.distributeLiquidity = distributeLiquidity;

        this.secondarypeg = secondarypeg;
        this.wallchangeThreshold = wallchangeThreshold;
        this.spread = spread;
        this.distanceThreshold = distanceThreshold;

    }

    /**
     * standard constructor with all options
     */
    public NuBotOptions(boolean dualSide, String apiKey, String apiSecret, String nubitAddress,
                        String rpcUser, String rpcPass, String nudIp, int nudPort, double priceIncrement,
                        double txFee, boolean sendRPC, String exchangeName, boolean executeOrders, boolean verbose, CurrencyPair pair,
                        int executeStrategyInterval, boolean sendHipchat,
                        String sendMails, String mailRecipient, int emergencyTimeout, double keepProceeds, boolean aggregate,
                        boolean multipleCustodians, double maxSellVolume, double maxBuyVolume,
                        boolean distributeLiquidity, boolean secondarypeg) {
        this.dualSide = dualSide;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.nubitAddress = nubitAddress;
        this.rpcUser = rpcUser;
        this.rpcPass = rpcPass;
        this.nudIp = nudIp;
        this.nudPort = nudPort;
        this.priceIncrement = priceIncrement;
        this.txFee = txFee;
        this.submitLiquidity = sendRPC;
        this.exchangeName = exchangeName;
        this.verbose = verbose;
        this.executeOrders = executeOrders;
        this.pair = pair;
        this.executeStrategyInterval = executeStrategyInterval;
        this.sendHipchat = sendHipchat;
        this.sendMails = sendMails;
        this.mailRecipient = mailRecipient;
        this.emergencyTimeout = emergencyTimeout;
        this.keepProceeds = keepProceeds;
        this.aggregate = aggregate;
        this.multipleCustodians = multipleCustodians;
        this.maxSellVolume = maxSellVolume;
        this.maxBuyVolume = maxBuyVolume;
        this.distributeLiquidity = distributeLiquidity;

        this.secondarypeg = secondarypeg;

    }


    public boolean isDualSide() {
        return dualSide;
    }

    public void setDualSide(boolean dualSide) {
        this.dualSide = dualSide;
    }

    public boolean isSecondarypeg() {
        return this.secondarypeg;
    }

    public void setSecondary(boolean secondary) {
        this.secondarypeg= secondarypeg;
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

    /**
     * @param apiKey
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return
     */
    public String getApiSecret() {
        return apiSecret;
    }

    /**
     * @param apiSecret
     */
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    /**
     * @return
     */
    public String getNubitsAddress() {
        return nubitAddress;
    }

    /**
     * @param nubitAddress
     */
    public void setNubitsAddress(String nubitAddress) {
        this.nubitAddress = nubitAddress;
    }

    /**
     * @return
     */
    public String getRpcUser() {
        return rpcUser;
    }

    /**
     * @param rpcUser
     */
    public void setRpcUser(String rpcUser) {
        this.rpcUser = rpcUser;
    }

    /**
     * @return
     */
    public String getRpcPass() {
        return rpcPass;
    }

    /**
     * @param rpcPass
     */
    public void setRpcPass(String rpcPass) {
        this.rpcPass = rpcPass;
    }

    /**
     * @return
     */
    public String getNudIp() {
        return nudIp;
    }

    /**
     * @param nudIp
     */
    public void setNudIp(String nudIp) {
        this.nudIp = nudIp;
    }

    /**
     * @return
     */
    public int getNudPort() {
        return nudPort;
    }

    /**
     * @param nudPort
     */
    public void setNudPort(int nudPort) {
        this.nudPort = nudPort;
    }

    /**
     * @return
     */
    public double getPriceIncrement() {
        return priceIncrement;
    }

    /**
     * @param priceIncrement
     */
    public void setPriceIncrement(double priceIncrement) {
        this.priceIncrement = priceIncrement;
    }

    /**
     * @return
     */
    public double getTxFee() {
        return txFee;
    }

    /**
     * @param txFee
     */
    public void setTxFee(double txFee) {
        this.txFee = txFee;
    }

    /**
     * @return
     */
    public String getExchangeName() {
        return exchangeName;
    }

    /**
     * @param exchangeName
     */
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    /**
     * @return
     */
    public CurrencyPair getPair() {
        return pair;
    }

    /**
     * @param pair
     */
    public void setPair(CurrencyPair pair) {
        this.pair = pair;
    }

    /**
     * @return
     */
    public int getExecuteStrategyInterval() {
        return executeStrategyInterval;
    }

    /**
     * @param executeStrategyInterval
     */
    public void getExecuteStrategyInterval(int executeStrategyInterval) {
        this.executeStrategyInterval = executeStrategyInterval;
    }

    /**
     * @return
     */
    public int getSendLiquidityInteval() {
        return sendLiquidityInterval;
    }

    /**
     * @param sendLiquidityInterval
     */
    public void setSendLiquidityInteval(int sendLiquidityInterval) {
        this.sendLiquidityInterval = sendLiquidityInterval;
    }

    /**
     * @return
     */
    public boolean isSendHipchat() {
        return sendHipchat;
    }

    /**
     * @param sendHipchat
     */
    public void setSendHipchat(boolean sendHipchat) {
        this.sendHipchat = sendHipchat;
    }

    /**
     * @return
     */
    public String sendMailsLevel() {
        return sendMails;
    }

    /**
     * @return
     */
    public boolean sendMails() {
        boolean not_none = !(this.sendMails.equals(MailNotifications.MAIL_LEVEL_NONE));
        return not_none;
    }

    /**
     * @param sendMails
     */
    public void setSendMailsLevel(String sendMails) {
        this.sendMails = sendMails;
    }

    /**
     * @return
     */
    public String getMailRecipient() {
        return mailRecipient;
    }

    /**
     * @param mailRecipient
     */
    public void setMailRecipient(String mailRecipient) {
        this.mailRecipient = mailRecipient;
    }

    public boolean isMultipleCustodians() {
        return multipleCustodians;
    }

    public void setMultipleCustodians(boolean multipleCustodians) {
        this.multipleCustodians = multipleCustodians;
    }

    public double getMaxSellVolume() {
        return maxSellVolume;
    }

    public void setMaxSellVolume(double maxSellVolume) {
        this.maxSellVolume = maxSellVolume;
    }

    public double getMaxBuyVolume() {
        return maxBuyVolume;
    }

    public void setMaxBuyVolume(double maxBuyVolume) {
        this.maxBuyVolume = maxBuyVolume;
    }

    public int getEmergencyTimeout() {
        return emergencyTimeout;
    }

    public void setEmergencyTimeoutMinutes(int emergencyTimeoutMinutes) {
        this.emergencyTimeout = emergencyTimeoutMinutes;
    }

    public double getKeepProceeds() {
        return keepProceeds;
    }

    public void setKeepProceeds(double keepProceeds) {
        this.keepProceeds = keepProceeds;
    }

    public String getNubitAddress() {
        return nubitAddress;
    }

    public void setNubitAddress(String nubitAddress) {
        this.nubitAddress = nubitAddress;
    }

    public boolean isAggregate() {
        return aggregate;
    }

    public void setAggregate(boolean aggregate) {
        this.aggregate = aggregate;
    }

    public boolean isDistributeLiquidity() {
        return distributeLiquidity;
    }

    public void setDistributeLiquidity(boolean distributeLiquidity) {
        this.distributeLiquidity = distributeLiquidity;
    }

    public double getSpread() {
        return this.spread;
    }


    public String getMainFeed() {
        return mainFeed;
    }


    public void setMainFeed(String mainFeed) {
        this.mainFeed = mainFeed;
    }


    public void setWallchangeThreshold(double wallchangeThreshold) {
        this.wallchangeThreshold = wallchangeThreshold;
    }

    public double getWallchangeThreshold() {
        return this.wallchangeThreshold;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public ArrayList<String> getBackupFeedNames() {
        return backupFeedNames;
    }

    public void setBackupFeedNames(ArrayList<String> backupFeedNames) {
        this.backupFeedNames = backupFeedNames;
    }

    /**
     * @return
     */
    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    /**
     * @param distanceThreshold
     */
    public void setDistanceThreshold(double distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    @Override
    public String toString() {
        return "NuBotOptions{" + "dualside=" + dualSide + ", submitLiquidity=" + submitLiquidity + ", executeOrders=" + executeOrders + ", verbose=" + verbose + ", sendHipchat=" + sendHipchat + ", apikey=" + apiKey + ", apisecret=" + apiSecret + ", nubitAddress=" + nubitAddress + ", rpcUser=" + rpcUser + ", rpcPass=" + rpcPass + ", nudIp=" + nudIp + ", nudPort=" + nudPort + ", priceIncrement=" + priceIncrement + ", txFee=" + txFee + ", exchangename=" + exchangeName + ", pair=" + pair + ", executeStrategyInterval=" + executeStrategyInterval + ", sendLiquidityInterval=" + sendLiquidityInterval + ", sendMails=" + sendMails + ", mailRecipient=" + mailRecipient + "emergencyTimeoutMinutes " + emergencyTimeout + "keepProceeds=" + keepProceeds + "aggregate=" + aggregate + " , waitBeforeShift=" + multipleCustodians + " , distributeLiquidity=" + distributeLiquidity + '}';
    }

    //Same as above, without printing api secret key and RCP password (for logging purposes)

    /**
     * @return
     */
    public String toStringNoKeys() {
        return "Options : {<br>" + "dualSide=" + dualSide + "<br> submitLiquidity=" + submitLiquidity + "<br> executeOrders=" + executeOrders + "<br> verbose=" + verbose + "<br> sendHipchat=" + sendHipchat + "<br> apiKey=" + apiKey + "<br> nubitAddress=" + nubitAddress + "<br> rpcUser=" + rpcUser + "<br> nudIp=" + nudIp + "<br> nudPort=" + nudPort + "<br> priceIncrement=" + priceIncrement + "<br> txFee=" + txFee + "<br> exchangename=" + exchangeName + "<br> pair=" + pair + "<br> executeStrategyInterval=" + executeStrategyInterval + "<br> sendLiquidityInterval=" + sendLiquidityInterval + "<br> sendMails=" + sendMails + "<br> mailRecipient=" + mailRecipient + "<br> emergencyTimeoutMinutes " + emergencyTimeout + "<br> keepProceeds=" + keepProceeds + "<br> aggregate=" + aggregate + "<br> distributeLiquidity=" + distributeLiquidity + '}';
    }
}


