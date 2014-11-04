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

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.utils.FileSystem;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class SendLiquidityinfoTask extends TimerTask {

    private static final Logger LOG = Logger.getLogger(SendLiquidityinfoTask.class.getName());
    private boolean verbose;
    private String outputFile;
    private boolean wallsBeingShifted = false;

    public SendLiquidityinfoTask(boolean verbose) {
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
                        && !Global.options.getExchangeName().equals(Constant.CCEDK)
                        && !Global.options.getExchangeName().equals(Constant.POLONIEX)
                        && !Global.options.getExchangeName().equals(Constant.CCEX)) {
                    //if the bot is running on Strategy Secondary Peg, we need to convert this value
                    nbt_onbuy = nbt_onbuy * Global.conversion;
                }
                Global.exchange.getLiveData().setNBTonbuy(nbt_onbuy);
                Global.exchange.getLiveData().setNBTonsell(nbt_onsell);


                //Write to file timestamp,activeOrders, sells,buys, digest
                String toWrite = new Date().toString() + " , " + orderList.size() + " , " + sells + " , " + buys + " , " + digest;
                FileSystem.writeToFile(toWrite, outputFile, true);

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

            JSONObject responseObject = Global.rpcClient.submitLiquidityInfo(Global.rpcClient.USDchar,
                    Global.exchange.getLiveData().getNBTonbuy(), Global.exchange.getLiveData().getNBTonsell());
            if (null == responseObject) {
                LOG.severe("Something went wrong while sending liquidityinfo");
            } else {
                LOG.fine(responseObject.toJSONString());
                if ((boolean) responseObject.get("submitted")) {
                    LOG.fine("RPC Liquidityinfo sent : "
                            + "\nbuyside : " + exchange.getLiveData().getNBTonbuy()
                            + "\nsellside : " + exchange.getLiveData().getNBTonsell());
                    JSONObject infoObject = Global.rpcClient.getLiquidityInfo(NuRPCClient.USDchar);
                    if (verbose) {
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
