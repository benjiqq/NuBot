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

package com.nubits.nubot.tasks;

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.models.PairBalance;
import com.nubits.nubot.utils.FilesystemUtils;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;

/**
 * Submit info via NuWalletRPC
 */
public class SubmitLiquidityinfoTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(SubmitLiquidityinfoTask.class.getName());
    private boolean verbose;

    private boolean wallsBeingShifted = false;
    private boolean firstOrdersPlaced = false;
    private boolean firstExecution = true;

    private String outputFile_orders;
    private String jsonFile_orders;
    private String jsonFile_balances;

    public SubmitLiquidityinfoTask(boolean verbose) {
        this.verbose = verbose;
    }

    private void initFiles() {

        this.outputFile_orders = Global.sessionLogFolder + "/" + Settings.ORDERS_FILENAME + ".csv";
        this.jsonFile_orders = Global.sessionLogFolder + "/" + Settings.ORDERS_FILENAME + ".json";
        this.jsonFile_balances = Global.sessionLogFolder + "/" + Settings.BALANCES_FILEAME + ".json";

        //create json file if it doesn't already exist
        LOG.debug("init files");
        File jsonF1 = new File(this.jsonFile_orders);
        if (!jsonF1.exists()) {
            try {
                jsonF1.createNewFile();
                LOG.debug("created " + jsonF1);
            } catch (Exception e) {
                LOG.error("error creating file " + jsonF1 + " " + e);
            }

            JSONObject history = new JSONObject();
            JSONArray orders = new JSONArray();
            history.put("orders", orders);
            FilesystemUtils.writeToFile(history.toJSONString(), this.jsonFile_orders, true);
        }

        //create json file if it doesn't already exist
        File jsonF2 = new File(this.jsonFile_balances);
        if (!jsonF2.exists()) {
            try {
                jsonF2.createNewFile();
                LOG.debug("created " + jsonF2);
            } catch (Exception e) {
                LOG.error("error creating file " + jsonF1 + " " + e);
            }

            JSONObject history = new JSONObject();
            JSONArray balances = new JSONArray();
            history.put("balances", balances);
            FilesystemUtils.writeToFile(history.toJSONString(), this.jsonFile_balances, true);
        }

        File of = new File(this.outputFile_orders);
        if (!of.exists()) {
            try {
                of.createNewFile();
                LOG.debug("created " + of);
            } catch (Exception e) {
                LOG.error("error creating file " + of + "  " + e);
            }
        }

        FilesystemUtils.writeToFile("timestamp,activeOrders, sells,buys, digest\n", this.outputFile_orders, false);

    }

    @Override
    public void run() {

        LOG.info("Executing " + this.getClass());

        if (firstExecution) {
            initFiles();
            firstExecution = false;
        }
        checkOrders();

    }

    private void checkOrders() {
        if (!isWallsBeingShifted()) { //Do not report liquidity info during wall shifts (issue #23)
            if (isFirstOrdersPlaced()) {
                String response1 = reportTier1(); //active orders
                String response2 = reportTier2(); //balance
                if (Global.options.isSubmitliquidity()) {
                    LOG.info("Liquidity info submitted:\n\t" + response1 + "\n\t" + response2);
                }
            } else {
                LOG.warn("Liquidity is not being sent : orders are not yet initialized");

            }
        } else {
            if (Global.options.isSubmitliquidity()) {
                LOG.warn("Liquidity is not being sent, a wall shift is happening. Will send on next execution.");
            }
        }
    }

    private String reportTier1() {
        String toReturn = "";

        Global.orderManager.fetch();
        ArrayList<Order> orderList = Global.orderManager.getOrderList();

        LOG.debug("Active orders : " + orderList.size());

        Iterator<Order> it = orderList.iterator();
        while (it.hasNext()) {
            Order o = it.next();
            LOG.debug("order: " + o.getDigest());
        }

        if (verbose) {
            DecimalFormat nf = new DecimalFormat("0");
            nf.setMinimumFractionDigits(8);
            LOG.info(Global.exchange.getName() + "OLD NBTonbuy  : " + nf.format(Global.exchange.getLiveData().getNBTonbuy()));
            LOG.info(Global.exchange.getName() + "OLD NBTonsell  : " + nf.format(Global.exchange.getLiveData().getNBTonsell()));
        }

        double nbt_onsell = 0;
        double nbt_onbuy = 0;
        int sells = 0;
        int buys = 0;
        String digest = "";
        for (int i = 0; i < orderList.size(); i++) {
            Order tempOrder = orderList.get(i);
            digest = digest + tempOrder.getDigest();
            double toAdd = tempOrder.getAmount().getQuantity();
            if (verbose) {
                LOG.info(tempOrder.toString());
            }

            if (tempOrder.getType().equalsIgnoreCase(Constant.SELL)) {
                //Start summing up amounts of NBT
                nbt_onsell += toAdd;
                sells++;
            } else if (tempOrder.getType().equalsIgnoreCase(Constant.BUY)) {
                //Start summing up amounts of NBT
                nbt_onbuy += toAdd;
                buys++;
            }
        }
        //Update the order
        Global.exchange.getLiveData().setOrdersList(orderList);

        if (Global.conversion != 1
                && Global.swappedPair) {  //For swapped pair, need to convert the amounts to NBT
            nbt_onbuy = nbt_onbuy * Global.conversion;
            nbt_onsell = nbt_onsell * Global.conversion;
        }


        Global.exchange.getLiveData().setNBTonbuy(nbt_onbuy);
        Global.exchange.getLiveData().setNBTonsell(nbt_onsell);

        //Write to file timestamp,activeOrders, sells,buys, digest
        Date timeStamp = new Date();
        String timeStampString = timeStamp.toString();
        Long timeStampLong = Utils.getTimestampLong();
        String toWrite = timeStampString + " , " + orderList.size() + " , " + sells + " , " + buys + " , " + digest;
        logOrderCSV(toWrite);

        //Also update a json version of the output file
        //build the latest data into a JSONObject
        JSONObject latestOrders = new JSONObject();
        latestOrders.put("time_stamp", timeStampLong);
        latestOrders.put("active_orders", orderList.size());
        JSONArray jsonDigest = new JSONArray();
        for (Iterator<Order> order = orderList.iterator(); order.hasNext(); ) {

            JSONObject thisOrder = new JSONObject();
            Order _order = order.next();

            //issue 160 - convert all amounts in NBT

            double amount = _order.getAmount().getQuantity();
            //special case: swapped pair
            if (Global.conversion != 1) {
                if (Global.swappedPair)//For swapped pair, need to convert the amounts to NBT
                {
                    amount = _order.getAmount().getQuantity() * Global.conversion;
                }
            }

            thisOrder.put("order_id", _order.getId());
            thisOrder.put("time", _order.getInsertedDate().getTime());
            thisOrder.put("order_type", _order.getType());
            thisOrder.put("order_currency", _order.getPair().getOrderCurrency().getCode());
            thisOrder.put("amount", amount);
            thisOrder.put("payment_currency", _order.getPair().getPaymentCurrency().getCode());
            thisOrder.put("price", _order.getPrice().getQuantity());
            jsonDigest.add(thisOrder);
        }
        latestOrders.put("digest", jsonDigest);


        //now read the existing object if one exists
        JSONParser parser = new JSONParser();
        JSONObject orderHistory = new JSONObject();
        JSONArray orders = new JSONArray();
        try { //object already exists in file
            orderHistory = (JSONObject) parser.parse(FilesystemUtils.readFromFile(this.jsonFile_orders));
            orders = (JSONArray) orderHistory.get("orders");
        } catch (ParseException pe) {
            LOG.error("Unable to parse " + this.jsonFile_orders);
        }
        //add the latest orders to the orders array
        orders.add(latestOrders);
        //then save
        logOrderJSON(orderHistory);

        if (verbose) {
            DecimalFormat nf = new DecimalFormat("0");
            nf.setMinimumFractionDigits(8);
            LOG.info(Global.exchange.getName() + "Updated NBTonbuy  : " + nf.format(nbt_onbuy));
            LOG.info(Global.exchange.getName() + "Updated NBTonsell  : " + nf.format(nbt_onsell));
        }

        if (Global.options.isSubmitliquidity()) {
            //Call RPC

            double buySide;
            double sellSide;

            if (!Global.swappedPair) {
                buySide = Global.exchange.getLiveData().getNBTonbuy();
                sellSide = Global.exchange.getLiveData().getNBTonsell();
            } else {
                buySide = Global.exchange.getLiveData().getNBTonsell();
                sellSide = Global.exchange.getLiveData().getNBTonbuy();
            }

            toReturn = sendLiquidityInfoImpl(buySide, sellSide, 1);
        }
        return toReturn;
    }

    private JSONObject getBalanceHistory() throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject balanceHistory = (JSONObject) parser.parse(FilesystemUtils.readFromFile(this.jsonFile_balances));
        return balanceHistory;
    }

    private String reportTier2() {
        String toReturn = "";
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(Global.options.getPair());
        if (balancesResponse.isPositive()) {
            PairBalance balance = (PairBalance) balancesResponse.getResponseObject();

            Amount NBTbalance = balance.getNBTAvailable();
            Amount PEGbalance = balance.getPEGAvailableBalance();

            double buyside = PEGbalance.getQuantity();
            double sellside = NBTbalance.getQuantity();

            //Log balances
            JSONObject latestBalances = new JSONObject();
            latestBalances.put("time_stamp", Utils.getTimestampLong());

            JSONArray availableBalancesArray = new JSONArray();
            JSONObject NBTBalanceJSON = new JSONObject();
            NBTBalanceJSON.put("amount", sellside);
            NBTBalanceJSON.put("currency", NBTbalance.getCurrency().getCode().toUpperCase());

            JSONObject PEGBalanceJSON = new JSONObject();
            PEGBalanceJSON.put("amount", buyside);
            PEGBalanceJSON.put("currency", PEGbalance.getCurrency().getCode().toUpperCase());

            availableBalancesArray.add(PEGBalanceJSON);
            availableBalancesArray.add(NBTBalanceJSON);

            latestBalances.put("balance-not-on-order", availableBalancesArray);

            //now read the existing object if one exists
            JSONObject balanceHistory = null;
            try {
                balanceHistory = getBalanceHistory();
            } catch (ParseException pe) {
                LOG.error("Unable to parse " + this.jsonFile_balances);
            }

            JSONArray balances = new JSONArray();
            try { //object already exists in file
                balances = (JSONArray) balanceHistory.get("balances");
            } catch (Exception e) {

            }

            //add the latest orders to the orders array
            balances.add(latestBalances);
            //then save
            logBalanceJSON(balanceHistory);

            buyside = Utils.round(buyside * Global.conversion, 2);

            if (Global.options.isSubmitliquidity()) {
                //Call RPC
                toReturn = sendLiquidityInfoImpl(buyside, sellside, 2);
            }


        } else {
            LOG.error(balancesResponse.getError().toString());
        }
        return toReturn;
    }

    private String sendLiquidityInfoImpl(double buySide, double sellSide, int tier) {
        String toReturn = "";
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject;


            responseObject = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
                    buySide, sellSide, tier);

            toReturn = "tier=" + tier
                    + " buy=" + buySide
                    + " sell=" + sellSide
                    + " identifier=" + Global.rpcClient.generateIdentifier(tier)
                    + " response=" + responseObject.toJSONString();
            if (null == responseObject) {
                LOG.error("Something went wrong while sending liquidityinfo");
            } else {
                LOG.debug(responseObject.toJSONString());
                if ((boolean) responseObject.get("submitted")) {
                    LOG.debug("RPC Liquidityinfo sent : "
                            + " buyside : " + buySide
                            + " sellside : " + sellSide);
                    if (verbose) {
                        JSONObject infoObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
                        LOG.info("getliquidityinfo result : ");
                        LOG.info(infoObject.toJSONString());
                    }
                }
            }
        } else {
            LOG.error("Can't reach Nud client. ");
        }
        return toReturn;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isWallsBeingShifted() {
        return wallsBeingShifted;
    }

    public void setWallsBeingShifted(boolean wallsBeingShifted) {
        this.wallsBeingShifted = wallsBeingShifted;
    }

    public boolean isFirstOrdersPlaced() {
        return firstOrdersPlaced;
    }

    public void setFirstOrdersPlaced(boolean firstOrdersPlaced) {
        this.firstOrdersPlaced = firstOrdersPlaced;
    }

    //---------------- storage related -----------------

    private void logOrderCSV(String toWrite) {
        FilesystemUtils.writeToFile(toWrite, outputFile_orders, true);
    }

    private void logOrderJSON(JSONObject orderHistory) {
        FilesystemUtils.writeToFile(orderHistory.toJSONString(), jsonFile_orders, false);
    }

    private void logBalanceJSON(JSONObject balanceHistory) {
        FilesystemUtils.writeToFile(balanceHistory.toJSONString(), jsonFile_balances, false);
    }


}
