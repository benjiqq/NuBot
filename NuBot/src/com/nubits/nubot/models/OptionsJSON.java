/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
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
package com.nubits.nubot.models;

import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author desrever <desrever at nubits.com>
 *
 *
 */
public class OptionsJSON {

    private static final Logger LOG = Logger.getLogger(OptionsJSON.class.getName());
    //Compulsory settings ----------------------------
    private String apiKey;
    private String apiSecret;
    private String mailRecipient;
    private String rpcUser;
    private String rpcPass;
    private String nubitAddress;
    private String exchangeName;
    private int nudPort;
    private boolean dualSide;
    private CurrencyPair pair;
    private SecondaryPegOptionsJSON secondaryPegOptions;
    //Optional settings with a default value  ----------------------------
    private String nudIp;
    private boolean sendMails;
    private boolean sendRPC;
    private boolean executeOrders;
    private boolean verbose;
    private boolean sendHipchat;
    private boolean aggregate;
    private int checkBalanceInterval;
    private int checkOrdersInteval;
    private double txFee;
    private double priceIncrement;
    private int emergencyTimeout;
    private double keepProceedings;
    private SecondaryPegOptionsJSON cpo;

    /**
     *
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
     * @param checkBalanceInterval
     * @param checkOrdersInteval
     * @param sendHipchat
     * @param sendMails
     * @param mailRecipient
     * @param emergencyTimeout
     * @param keepProceedings
     * @param secondaryPegOptions
     */
    public OptionsJSON(boolean dualSide, String apiKey, String apiSecret, String nubitAddress,
            String rpcUser, String rpcPass, String nudIp, int nudPort, double priceIncrement,
            double txFee, boolean sendRPC, String exchangeName, boolean executeOrders, boolean verbose, CurrencyPair pair,
            int checkBalanceInterval, int checkOrdersInteval, boolean sendHipchat,
            boolean sendMails, String mailRecipient, int emergencyTimeout, double keepProceedings, boolean aggregate,SecondaryPegOptionsJSON secondaryPegOptions) {
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
        this.sendRPC = sendRPC;
        this.exchangeName = exchangeName;
        this.verbose = verbose;
        this.executeOrders = executeOrders;
        this.pair = pair;
        this.checkOrdersInteval = checkOrdersInteval;
        this.checkBalanceInterval = checkBalanceInterval;
        this.sendHipchat = sendHipchat;
        this.sendMails = sendMails;
        this.mailRecipient = mailRecipient;
        this.emergencyTimeout = emergencyTimeout;
        this.keepProceedings = keepProceedings;
        this.secondaryPegOptions = secondaryPegOptions;
        this.aggregate = aggregate;
    }

    /**
     *
     * @return
     */
    public boolean isDualSide() {
        return dualSide;
    }

    /**
     *
     * @param dualSide
     */
    public void setDualSide(boolean dualSide) {
        this.dualSide = dualSide;
    }

    /**
     *
     * @return
     */
    public boolean isSendRPC() {
        return sendRPC;
    }

    /**
     *
     * @param sendRPC
     */
    public void setSendRPC(boolean sendRPC) {
        this.sendRPC = sendRPC;
    }

    /**
     *
     * @return
     */
    public boolean isExecuteOrders() {
        return executeOrders;
    }

    /**
     *
     * @param executeOrders
     */
    public void setExecuteOrders(boolean executeOrders) {
        this.executeOrders = executeOrders;
    }

    /**
     *
     * @return
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     *
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     *
     * @return
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     *
     * @param apiKey
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     *
     * @return
     */
    public String getApiSecret() {
        return apiSecret;
    }

    /**
     *
     * @param apiSecret
     */
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    /**
     *
     * @return
     */
    public String getNubitsAddress() {
        return nubitAddress;
    }

    /**
     *
     * @param nubitAddress
     */
    public void setNubitsAddress(String nubitAddress) {
        this.nubitAddress = nubitAddress;
    }

    /**
     *
     * @return
     */
    public String getRpcUser() {
        return rpcUser;
    }

    /**
     *
     * @param rpcUser
     */
    public void setRpcUser(String rpcUser) {
        this.rpcUser = rpcUser;
    }

    /**
     *
     * @return
     */
    public String getRpcPass() {
        return rpcPass;
    }

    /**
     *
     * @param rpcPass
     */
    public void setRpcPass(String rpcPass) {
        this.rpcPass = rpcPass;
    }

    /**
     *
     * @return
     */
    public String getNudIp() {
        return nudIp;
    }

    /**
     *
     * @param nudIp
     */
    public void setNudIp(String nudIp) {
        this.nudIp = nudIp;
    }

    /**
     *
     * @return
     */
    public int getNudPort() {
        return nudPort;
    }

    /**
     *
     * @param nudPort
     */
    public void setNudPort(int nudPort) {
        this.nudPort = nudPort;
    }

    /**
     *
     * @return
     */
    public double getPriceIncrement() {
        return priceIncrement;
    }

    /**
     *
     * @param priceIncrement
     */
    public void setPriceIncrement(double priceIncrement) {
        this.priceIncrement = priceIncrement;
    }

    /**
     *
     * @return
     */
    public double getTxFee() {
        return txFee;
    }

    /**
     *
     * @param txFee
     */
    public void setTxFee(double txFee) {
        this.txFee = txFee;
    }

    /**
     *
     * @return
     */
    public String getExchangeName() {
        return exchangeName;
    }

    /**
     *
     * @param exchangeName
     */
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    /**
     *
     * @return
     */
    public CurrencyPair getPair() {
        return pair;
    }

    /**
     *
     * @param pair
     */
    public void setPair(CurrencyPair pair) {
        this.pair = pair;
    }

    /**
     *
     * @return
     */
    public int getCheckBalanceInterval() {
        return checkBalanceInterval;
    }

    /**
     *
     * @param checkBalanceInterval
     */
    public void setCheckBalanceInterval(int checkBalanceInterval) {
        this.checkBalanceInterval = checkBalanceInterval;
    }

    /**
     *
     * @return
     */
    public int getCheckOrdersInteval() {
        return checkOrdersInteval;
    }

    /**
     *
     * @param checkOrdersInteval
     */
    public void setCheckOrdersInteval(int checkOrdersInteval) {
        this.checkOrdersInteval = checkOrdersInteval;
    }

    /**
     *
     * @return
     */
    public boolean isSendHipchat() {
        return sendHipchat;
    }

    /**
     *
     * @param sendHipchat
     */
    public void setSendHipchat(boolean sendHipchat) {
        this.sendHipchat = sendHipchat;
    }

    /**
     *
     * @return
     */
    public boolean isSendMails() {
        return sendMails;
    }

    /**
     *
     * @param sendMails
     */
    public void setSendMails(boolean sendMails) {
        this.sendMails = sendMails;
    }

    /**
     *
     * @return
     */
    public String getMailRecipient() {
        return mailRecipient;
    }

    /**
     *
     * @param mailRecipient
     */
    public void setMailRecipient(String mailRecipient) {
        this.mailRecipient = mailRecipient;
    }

    /**
     *
     * @return
     */
    public SecondaryPegOptionsJSON getSecondaryPegOptions() {
        return secondaryPegOptions;
    }

    /**
     *
     * @param secondaryPegOptions
     */
    public void setCryptoPegOptions(SecondaryPegOptionsJSON cryptoPegOptions) {
        this.secondaryPegOptions = cryptoPegOptions;
    }

    /**
     *
     * @param path
     * @return
     */
    public static OptionsJSON parseOptions(String path) {
        OptionsJSON options = null;
        JSONParser parser = new JSONParser();
        String optionsString = FileSystem.readFromFile(path);
        try {
            JSONObject fileJSON = (JSONObject) (parser.parse(optionsString));
            JSONObject optionsJSON = (JSONObject) fileJSON.get("options");


            //First try to parse compulsory parameters

            String apiKey = (String) optionsJSON.get("apikey");
            String apiSecret = (String) optionsJSON.get("apisecret");
            String nubitAddress = (String) optionsJSON.get("nubitaddress");
            String rpcPass = (String) optionsJSON.get("rpcpass");
            String rpcUser = (String) optionsJSON.get("rpcuser");
            String exchangeName = (String) optionsJSON.get("exchangename");
            String mailRecipient = (String) optionsJSON.get("mail-recipient");

            String pairStr = (String) optionsJSON.get("pair");
            CurrencyPair pair = CurrencyPair.getCurrencyPairFromString(pairStr, "_");
            boolean dualside = (boolean) optionsJSON.get("dualside");

            long nudPortlong = (long) optionsJSON.get("nudport");
            int nudPort = (int) nudPortlong;



            //Based on the pair, set a parameter do define whether setting SecondaryPegOptionsJSON i necessary or not
            boolean requireCryptoOptions = Utils.requiresSecondaryPegStrategy(pair);
            org.json.JSONObject pegOptionsJSON;
            SecondaryPegOptionsJSON cpo = null;
            if (requireCryptoOptions) {
                org.json.JSONObject jsonString = new org.json.JSONObject(optionsString);
                org.json.JSONObject optionsJSON2 = (org.json.JSONObject) jsonString.get("options");
                pegOptionsJSON = (org.json.JSONObject) optionsJSON2.get("secondary-peg-options");
                cpo = SecondaryPegOptionsJSON.create(pegOptionsJSON);
            }

            //Then parse optional settings. If not use the default value declared here

            String nudIp = "127.0.0.1";
            boolean sendMails = true;
            boolean sendRPC = true;
            boolean executeOrders = true;
            boolean verbose = false;
            boolean sendHipchat = true;
            boolean aggregate = true;

            int checkBalanceInterval = 45;
            int checkOrdersInteval = 60;

            double txFee = 0.2;
            double priceIncrement = 0.0003;
            double keepProceedings = 0;

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

            if (optionsJSON.containsKey("sendrpc")) {
                sendRPC = (boolean) optionsJSON.get("sendrpc");
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
                sendMails = (boolean) optionsJSON.get("mail-notifications");
            }
            
            if (optionsJSON.containsKey("aggregate")) {
                aggregate = (boolean) optionsJSON.get("aggregate");
            }
            
            
            if (optionsJSON.containsKey("check-balance-interval")) {
                long checkBalanceIntervallong = (long) optionsJSON.get("check-balance-interval");
                checkBalanceInterval = (int) checkBalanceIntervallong;
            }
            

            if (optionsJSON.containsKey("check-orders-interval")) {
                long checkOrdersIntevallong = (long) optionsJSON.get("check-orders-interval");
                checkOrdersInteval = (int) checkOrdersIntevallong;
            }

            if (optionsJSON.containsKey("emergency-timeout")) {
                long emergencyTimeoutLong = (long) optionsJSON.get("emergency-timeout");
                emergencyTimeout = (int) emergencyTimeoutLong;
            }

            if (optionsJSON.containsKey("keep-proceedings")) {
                keepProceedings = Utils.getDouble((optionsJSON.get("keep-proceedings")));
            }

            //Create a new Instance
            options = new OptionsJSON(dualside, apiKey, apiSecret, nubitAddress, rpcUser,
                    rpcPass, nudIp, nudPort, priceIncrement, txFee, sendRPC, exchangeName,
                    executeOrders, verbose, pair, checkBalanceInterval,
                    checkOrdersInteval, sendHipchat, sendMails, mailRecipient,
                    emergencyTimeout, keepProceedings,aggregate, cpo);



        } catch (ParseException | NumberFormatException | JSONException e) {
            LOG.severe("Error while parsing the options file : " + e);
        }
        return options;
    }

    /**
     *
     * @return
     */
    public int getEmergencyTimeout() {
        return emergencyTimeout;
    }

    /**
     *
     * @param emergencyTimeoutMinutes
     */
    public void setEmergencyTimeoutMinutes(int emergencyTimeoutMinutes) {
        this.emergencyTimeout = emergencyTimeoutMinutes;
    }

    public double getKeepProceedings() {
        return keepProceedings;
    }

    public void setKeepProceedings(double keepProceedings) {
        this.keepProceedings = keepProceedings;
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

    
    
    @Override
    public String toString() {
        String cryptoOptions = "";
        if (secondaryPegOptions != null) {
            cryptoOptions = secondaryPegOptions.toString();
        }
        return "OptionsJSON{" + "dualSide=" + dualSide + ", sendRPC=" + sendRPC + ", executeOrders=" + executeOrders + ", verbose=" + verbose + ", sendHipchat=" + sendHipchat + ", apiKey=" + apiKey + ", apiSecret=" + apiSecret + ", nubitAddress=" + nubitAddress + ", rpcUser=" + rpcUser + ", rpcPass=" + rpcPass + ", nudIp=" + nudIp + ", nudPort=" + nudPort + ", priceIncrement=" + priceIncrement + ", txFee=" + txFee + ", exchangeName=" + exchangeName + ", pair=" + pair + ", checkBalanceInterval=" + checkBalanceInterval + ", checkOrdersInteval=" + checkOrdersInteval + ", sendMails=" + sendMails + ", mailRecipient=" + mailRecipient + "emergencyTimeoutMinutes " + emergencyTimeout + "keepProceedings=" + keepProceedings + "aggregate=" + aggregate + " , cryptoPegOptions=" + cryptoOptions + '}';
    }

    //Same as above, without printing api secret key and RCP password (for logging purposes)
    /**
     *
     * @return
     */
    public String toStringNoKeys() {
        String cryptoOptions = "";
        if (secondaryPegOptions != null) {
            cryptoOptions = secondaryPegOptions.toHtmlString();
        }
        return "Options : {<br>" + "dualSide=" + dualSide + "<br> sendRPC=" + sendRPC + "<br> executeOrders=" + executeOrders + "<br> verbose=" + verbose + "<br> sendHipchat=" + sendHipchat + "<br> apiKey=" + apiKey + "<br> nubitAddress=" + nubitAddress + "<br> rpcUser=" + rpcUser + "<br> nudIp=" + nudIp + "<br> nudPort=" + nudPort + "<br> priceIncrement=" + priceIncrement + "<br> txFee=" + txFee + "<br> exchangeName=" + exchangeName + "<br> pair=" + pair + "<br> checkBalanceInterval=" + checkBalanceInterval + "<br> checkOrdersInteval=" + checkOrdersInteval + "<br> sendMails=" + sendMails + "<br> mailRecipient=" + mailRecipient + "<br> emergencyTimeoutMinutes " + emergencyTimeout + "<br> keepProceedings=" + keepProceedings + "<br> aggregate=" + aggregate + " <br><br>" + cryptoOptions + '}';
    }
}
