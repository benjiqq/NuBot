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
package com.nubits.nubot.tasks;

//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.utils.FileSystem;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class SubmitLiquidityinfoTask extends TimerTask {

    private static final Logger LOG = Logger.getLogger(SubmitLiquidityinfoTask.class.getName());
    private boolean verbose;
    private String outputFile;
    private String jsonFile;
    private boolean wallsBeingShifted = false;

    public SubmitLiquidityinfoTask(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void run() {
        LOG.fine("Executing task : CheckOrdersTask ");
        checkOrders();
    }
    //Taken the input exchange, updates it and returns it.

    private void checkOrders() {
        if (!isWallsBeingShifted()) { //Do not report liquidity info during wall shifts (issue #23)
            ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
            if (activeOrdersResponse.isPositive()) {
                ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

                LOG.fine("Active orders : " + orderList.size());

                if (verbose) {
                    LOG.info(Global.exchange.getName() + "OLD NBTonbuy  : " + Global.exchange.getLiveData().getNBTonbuy());
                    LOG.info(Global.exchange.getName() + "OLD NBTonsell  : " + Global.exchange.getLiveData().getNBTonsell());
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
                        LOG.fine(tempOrder.toString());
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

                //Some exchanges return the buy orders amount expressed in payment currency, need conversion
                if (Global.conversion != 1
                        && !Global.swappedPair //for swapped pair already converted above
                        && !Global.options.getExchangeName().equals(Constant.CCEDK)
                        && !Global.options.getExchangeName().equals(Constant.POLONIEX)
                        && !Global.options.getExchangeName().equals(Constant.CCEX)
                        && !Global.options.getExchangeName().equals(Constant.ALLCOIN)) {
                    //if the bot is running on Strategy Secondary Peg, we need to convert this value
                    nbt_onbuy = nbt_onbuy * Global.conversion;
                }

                Global.exchange.getLiveData().setNBTonbuy(nbt_onbuy);
                Global.exchange.getLiveData().setNBTonsell(nbt_onsell);

                //Write to file timestamp,activeOrders, sells,buys, digest
                Date timeStamp = new Date();
                String timeStampString = timeStamp.toString();
                Long timeStampLong = timeStamp.getTime();
                String toWrite = timeStampString + " , " + orderList.size() + " , " + sells + " , " + buys + " , " + digest;
                FileSystem.writeToFile(toWrite, outputFile, true);

                //Also update a json version of the output file
                //build the latest data into a JSONObject
                JSONObject latestOrders = new JSONObject();
                latestOrders.put("time_stamp", timeStampLong);
                latestOrders.put("active_orders", orderList.size());
                JSONArray jsonDigest = new JSONArray();
                for (Iterator<Order> order = orderList.iterator(); order.hasNext();) {
                    JSONObject thisOrder = new JSONObject();
                    Order _order = order.next();
                    thisOrder.put("order_id", _order.getId());
                    thisOrder.put("time", _order.getInsertedDate().getTime());
                    thisOrder.put("order_type", _order.getType());
                    thisOrder.put("order_currency", _order.getPair().getOrderCurrency().getCode());
                    thisOrder.put("amount", _order.getAmount().getQuantity());
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
                    orderHistory = (JSONObject) parser.parse(FileSystem.readFromFile(this.jsonFile));
                    orders = (JSONArray) orderHistory.get("orders");
                } catch (ParseException pe) {
                    LOG.severe("Unable to parse order_history.json");
                }
                //add the latest orders to the orders array
                orders.add(latestOrders);
                //then save
                FileSystem.writeToFile(orderHistory.toJSONString(), jsonFile, false);

                if (verbose) {
                    LOG.info(Global.exchange.getName() + "Updated NBTonbuy  : " + nbt_onbuy);
                    LOG.info(Global.exchange.getName() + "Updated NBTonsell  : " + nbt_onsell);
                }
                if (Global.options.isSendRPC()) {
                    //Call RPC
                    sendLiquidityInfo(Global.exchange);
                }

            } else {
                LOG.severe(activeOrdersResponse.getError().toString());
            }
        } else {
            if (isWallsBeingShifted()) {
                LOG.warning("Liquidity is not being sent, a wall shift is happening. Will send on next execution.");
            }
        }
    }

    private void sendLiquidityInfo(Exchange exchange) {
        if (Global.rpcClient.isConnected()) {
            JSONObject responseObject;
            if (!Global.swappedPair) {
                responseObject = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
                        Global.exchange.getLiveData().getNBTonbuy(), Global.exchange.getLiveData().getNBTonsell());
            } else {
                responseObject = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
                        Global.exchange.getLiveData().getNBTonsell(), Global.exchange.getLiveData().getNBTonbuy());
            }
            LOG.fine("sending Liquidity Info :\n" + Global.exchange.getLiveData().getNBTonbuy() + "\n" + Global.exchange.getLiveData().getNBTonsell() + "\n" + responseObject.toJSONString());
            if (null == responseObject) {
                LOG.severe("Something went wrong while sending liquidityinfo");
            } else {
                LOG.fine(responseObject.toJSONString());
                if ((boolean) responseObject.get("submitted")) {
                    LOG.fine("RPC Liquidityinfo sent : "
                            + "\nbuyside : " + exchange.getLiveData().getNBTonbuy()
                            + "\nsellside : " + exchange.getLiveData().getNBTonsell());
                    if (verbose) {
                        JSONObject infoObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
                        LOG.info("getliquidityinfo result : ");
                        LOG.info(infoObject.toJSONString());
                    }
                }
            }
        } else {
            LOG.severe("Client offline. ");

        }
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
        this.jsonFile = this.outputFile.replace(".csv", ".json");
        //create json file if it doesn't already exist
        File json = new File(this.jsonFile);
        if (!json.exists()) {
            JSONObject history = new JSONObject();
            JSONArray orders = new JSONArray();
            history.put("orders", orders);
            FileSystem.writeToFile(history.toJSONString(), this.jsonFile, true);
        }
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
}
