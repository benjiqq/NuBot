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

import com.nubits.nubot.models.CurrencyPair;

import java.util.logging.Logger;

/**
 * @author desrever <desrever at nubits.com>
 */
public class NuBotOptions {

    private static final Logger LOG = Logger.getLogger(NuBotOptions.class.getName());

    //Compulsory settings ----------------------------

    /**
     * Custodian's public key to access the exchange
     */
    private String apiKey;

    /**
     * Custodian's secret key to access the exchange
     */
    private String apiSecret;

    /**
     * the email to which emergency email are sent
     */
    private String mailRecipient;

    /**
     * Name of the exchange where the bots operates
     */
    private String exchangeName;

    /**
     * If set to true, the bot will behave as a dual side custodian, if false as a sell side custodian.
     */
    private boolean dualSide;

    /**
     * valid currency pair for the specified eg. "nbt_usd"
     */
    private CurrencyPair pair;

    private SecondaryPegOptionsJSON secondaryPegOptions;

    //Conditional settings with a default value

    /**
     * The RPC username of the Nu daemon
     */
    private String rpcUser;

    /**
     * The RPC password of the Nu daemon
     */
    private String rpcPass;

    /**
     * The public address where the custodial grant has been received
     */
    private String nubitAddress;

    /**
     * The RPC port of the Nu daemon
     */
    private int nudPort;

    //Optional settings with a default value  ----------------------------

    /**
     * The IP address of the machine that hosts the Nu Client
     */
    private String nudIp;

    /**
     * if set to false will disable email notifications
     * TODO: rename mailnotifications
     */
    private String sendMails;

    /**
     * if set to false, the bot will not try to submit liquidity info.
     * If set to false, it will also allow the custodian to omit the declaration of nubitaddress , nudport , rpcuser and rpcpass
     */
    private boolean submitLiquidity;

    /**
     * if set to false the bot will print a warning instead of executing orders
     */
    private boolean executeOrders;

    /**
     * if set to true, will print on screen additional debug messages
     */
    private boolean verbose;

    /**
     * if set to false will disable hipchat notifications
     */
    private boolean sendHipchat;

    private boolean aggregate;

    /**
     * if set to true, will sync with remote NPT and reset orders often
     */
    private boolean multipleCustodians;

    private int executeStrategyInterval; //disabled

    private int sendLiquidityInterval; //disabled

    /**
     * If transaction fee not available from the exchange via api, this value will be used
     */
    private double txFee;

    /**
     * if working in sell-side mode, this value (considered USD) will be added to the sell price
     */
    private double priceIncrement;

    /**
     * max amount of minutes of consecutive failure. After those minute elapse, emergency procedure starts
     */
    private int emergencyTimeout;

    /**
     * Specific setting for KTm's proposal. Will keep the specified proceeds from sales apart instead of putting 100% of balance on buy .
     */
    private double keepProceeds;

    /**
     * maximum volume to put on sell walls.
     */
    private double maxSellVolume;

    /**
     * maximum volume to put on buy walls.
     */
    private double maxBuyVolume;

    private boolean distributeLiquidity;

    private SecondaryPegOptionsJSON cpo;

    /**
     * empty constructor. assumes safe creation of valid options
     */
    public NuBotOptions() {

    }

    /**
     * @param dualSide
     * @param apiKey
     * @param apiSecret
     * @param nubitAddress
     * @param rpcUser
     * @param rpcPass
     * @param nudIp
     * @param nudPort
     * @param priceIncrement
     * @param txFee
     * @param sendRPC
     * @param exchangeName
     * @param executeOrders
     * @param verbose
     * @param pair
     * @param executeStrategyInterval
     * @param sendLiquidityInterval
     * @param sendHipchat
     * @param sendMails
     * @param mailRecipient
     * @param emergencyTimeout
     * @param keepProceeds
     * @param secondaryPegOptions
     */
    public NuBotOptions(boolean dualSide, String apiKey, String apiSecret, String nubitAddress,
                        String rpcUser, String rpcPass, String nudIp, int nudPort, double priceIncrement,
                        double txFee, boolean sendRPC, String exchangeName, boolean executeOrders, boolean verbose, CurrencyPair pair,
                        int executeStrategyInterval, int sendLiquidityInterval, boolean sendHipchat,
                        String sendMails, String mailRecipient, int emergencyTimeout, double keepProceeds, boolean aggregate,
                        boolean multipleCustodians, double maxSellVolume, double maxBuyVolume,
                        boolean distributeLiquidity, SecondaryPegOptionsJSON secondaryPegOptions) {
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
        this.sendLiquidityInterval = sendLiquidityInterval;
        this.executeStrategyInterval = executeStrategyInterval;
        this.sendHipchat = sendHipchat;
        this.sendMails = sendMails;
        this.mailRecipient = mailRecipient;
        this.emergencyTimeout = emergencyTimeout;
        this.keepProceeds = keepProceeds;
        this.secondaryPegOptions = secondaryPegOptions;
        this.aggregate = aggregate;
        this.multipleCustodians = multipleCustodians;
        this.maxSellVolume = maxSellVolume;
        this.maxBuyVolume = maxBuyVolume;
        this.distributeLiquidity = distributeLiquidity;

    }

    /**
     * @return
     */
    public boolean isDualSide() {
        return dualSide;
    }

    /**
     * @param dualSide
     */
    public void setDualSide(boolean dualSide) {
        this.dualSide = dualSide;
    }

    /**
     * TODO: rename
     * @return
     */
    public boolean isSendRPC() {
        return submitLiquidity;
    }

    /**
     * TODO: rename
     * @param sendRPC
     */
    public void setSendRPC(boolean sendRPC) {
        this.submitLiquidity = sendRPC;
    }

    /**
     * @return
     */
    public boolean isExecuteOrders() {
        return executeOrders;
    }

    /**
     * @param executeOrders
     */
    public void setExecuteOrders(boolean executeOrders) {
        this.executeOrders = executeOrders;
    }

    /**
     * @return
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @return
     */
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

    /**
     * @return
     */
    public SecondaryPegOptionsJSON getSecondaryPegOptions() {
        return secondaryPegOptions;
    }

    /**
     * @param secondaryPegOptions
     */
    public void setCryptoPegOptions(SecondaryPegOptionsJSON secondaryPegOptions) {
        this.secondaryPegOptions = secondaryPegOptions;
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

    /**
     * @return
     */
    public int getEmergencyTimeout() {
        return emergencyTimeout;
    }

    /**
     * @param emergencyTimeoutMinutes
     */
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

    @Override
    public String toString() {
        String cryptoOptions = "";
        if (secondaryPegOptions != null) {
            cryptoOptions = secondaryPegOptions.toString();
        }
        return "NuBotOptions{" + "dualside=" + dualSide + ", submitLiquidity=" + submitLiquidity + ", executeOrders=" + executeOrders + ", verbose=" + verbose + ", sendHipchat=" + sendHipchat + ", apikey=" + apiKey + ", apisecret=" + apiSecret + ", nubitAddress=" + nubitAddress + ", rpcUser=" + rpcUser + ", rpcPass=" + rpcPass + ", nudIp=" + nudIp + ", nudPort=" + nudPort + ", priceIncrement=" + priceIncrement + ", txFee=" + txFee + ", exchangename=" + exchangeName + ", pair=" + pair + ", executeStrategyInterval=" + executeStrategyInterval + ", sendLiquidityInterval=" + sendLiquidityInterval + ", sendMails=" + sendMails + ", mailRecipient=" + mailRecipient + "emergencyTimeoutMinutes " + emergencyTimeout + "keepProceeds=" + keepProceeds + "aggregate=" + aggregate + " , waitBeforeShift=" + multipleCustodians + " , distributeLiquidity=" + distributeLiquidity + " , cryptoPegOptions=" + cryptoOptions + '}';
    }

    //Same as above, without printing api secret key and RCP password (for logging purposes)

    /**
     * @return
     */
    public String toStringNoKeys() {
        String cryptoOptions = "";
        if (secondaryPegOptions != null) {
            cryptoOptions = secondaryPegOptions.toHtmlString();
        }
        return "Options : {<br>" + "dualSide=" + dualSide + "<br> submitLiquidity=" + submitLiquidity + "<br> executeOrders=" + executeOrders + "<br> verbose=" + verbose + "<br> sendHipchat=" + sendHipchat + "<br> apiKey=" + apiKey + "<br> nubitAddress=" + nubitAddress + "<br> rpcUser=" + rpcUser + "<br> nudIp=" + nudIp + "<br> nudPort=" + nudPort + "<br> priceIncrement=" + priceIncrement + "<br> txFee=" + txFee + "<br> exchangename=" + exchangeName + "<br> pair=" + pair + "<br> executeStrategyInterval=" + executeStrategyInterval + "<br> sendLiquidityInterval=" + sendLiquidityInterval + "<br> sendMails=" + sendMails + "<br> mailRecipient=" + mailRecipient + "<br> emergencyTimeoutMinutes " + emergencyTimeout + "<br> keepProceeds=" + keepProceeds + "<br> aggregate=" + aggregate + "<br> distributeLiquidity=" + distributeLiquidity + " <br><br>" + cryptoOptions + '}';
    }
}
