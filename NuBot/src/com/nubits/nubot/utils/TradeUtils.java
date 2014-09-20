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
package com.nubits.nubot.utils;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Order;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TradeUtils {

    private static final Logger LOG = Logger.getLogger(TradeUtils.class.getName());

    public static boolean areAllOrdersCanceled() {
        boolean toRet = false;
        //get all orders
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders(Global.options.getPair());
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();

            if (orderList.size() == 0) {
                toRet = true;
            } else {
                LOG.severe("There are still : " + orderList.size() + " active orders");
            }
        } else {
            LOG.severe(activeOrdersResponse.getError().toString());
        }

        return toRet;
    }

    public static boolean takeDownAndWait(String orderID, long timeoutMS) {

        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(orderID);
        if (deleteOrderResponse.isPositive()) {
            boolean delRequested = (boolean) deleteOrderResponse.getResponseObject();

            if (delRequested) {
                LOG.warning("Order " + orderID + " delete request submitted");
            } else {
                LOG.severe("Could not submit request to delete order" + orderID);
            }

        } else {
            LOG.severe(deleteOrderResponse.getError().toString());
        }


        //Wait until the order is deleted or timeout
        boolean timedout = false;
        boolean deleted = false;
        long wait = 6 * 1000;
        long count = 0L;
        do {
            try {
                Thread.sleep(wait);
                count += wait;
                timedout = count > timeoutMS;

                ApiResponse orderDetailResponse = Global.exchange.getTrade().orderExists(orderID);
                if (orderDetailResponse.isPositive()) {
                    deleted = !((boolean) orderDetailResponse.getResponseObject());
                    LOG.fine("Does order " + orderID + "  still exist?" + !deleted);
                } else {
                    LOG.severe(orderDetailResponse.getError().toString());
                    return false;
                }
            } catch (InterruptedException ex) {
                LOG.severe(ex.getMessage());
                return false;
            }
        } while (!deleted && !timedout);

        if (timedout) {
            return false;
        }
        return true;
    }

    public static double getSellPrice(double txFee) {
        if (Global.isDualSide) {
            return 1 + (0.01 * txFee);
        } else {
            return 1 + (0.01 * txFee) + Global.options.getPriceIncrement();
        }

    }

    public static double getBuyPrice(double txFeeUSDNTB) {
        return 1 - (0.01 * txFeeUSDNTB);
    }

    public static ArrayList<Order> filterOrders(ArrayList<Order> originalList, String type) {
        ArrayList<Order> toRet = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i++) {
            Order temp = originalList.get(i);
            if (temp.getType().equalsIgnoreCase(type)) {
                toRet.add(temp);
            }
        }

        return toRet;
    }
    //Build the query string given a set of query parameters

    /**
     *
     * @param args
     * @param encoding
     * @return
     */
    public static String buildQueryString(HashMap<String, String> args, String encoding) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) {
                result += '&';
            }
            try {
                result += URLEncoder.encode(hashkey, encoding) + "="
                        + URLEncoder.encode(args.get(hashkey), encoding);
            } catch (Exception ex) {
                LOG.severe(ex.getMessage());
            }
        }
        return result;
    }

    public static String buildQueryString(TreeMap<String, String> args, String encoding) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) {
                result += '&';
            }
            try {
                result += URLEncoder.encode(hashkey, encoding) + "="
                        + URLEncoder.encode(args.get(hashkey), encoding);
            } catch (Exception ex) {
                LOG.severe(ex.getMessage());
            }
        }
        return result;
    }

    public static String getCCDKEvalidNonce() {
        //It tries to send a wrong nonce, get the allowed window, and use it for the actuall call
        //TODO fix it
        String wrongNonce = "1234567891";
        try {
            String htmlString = Utils.getHTML("https://www.ccedk.com/api/v1/currency/list?nonce=" + wrongNonce);
            JSONParser parser = new JSONParser();
            try {
                //{"errors":{"nonce":"incorrect range `nonce`=`1234567891`, must be from `1411036100` till `1411036141`"}
                JSONObject httpAnswerJson = (JSONObject) (parser.parse(htmlString));
                JSONObject errors = (JSONObject) httpAnswerJson.get("errors");
                String nonceError = (String) errors.get("nonce");

                String startStr = " must be from";
                int indexStart = nonceError.lastIndexOf(startStr) + startStr.length() + 2;
                String subStr = nonceError.substring(indexStart, indexStart + 10);

                return subStr;
            } catch (Exception ex) {
                LOG.severe(ex.getMessage());
                return wrongNonce;
            }
        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }
        return wrongNonce;
    }

    public static int getCCDKECurrencyId(String currencyCode) {
        /*
         * LAST UPDATED : 17 september
         * FROM : https://www.ccedk.com/api/v1/currency/list?nonce=1410950600
         * 1,LTC
         * 2,BTC
         * 3,USD
         * 8,PPC
         *

         */
        final String BTC = Constant.BTC.getCode();
        String USD = Constant.USD.getCode();
        String PPC = Constant.PPC.getCode();

        int toRet = -1;

        switch (currencyCode) {
            case "BTC":
                toRet = 2;
                break;
            case "USD":
                toRet = 3;
                break;
            case "PPC":
                toRet = 8;
                break;
            case "LTC":
                toRet = 1;
                break;
            default:
                LOG.severe("Currency " + currencyCode + "not available");
                break;
        }
        return toRet;
    }

    public static int getCCDKECurrencyPairId(CurrencyPair pair) {
        /*
         * LAST UPDATED : 20 september
         FROM : https://www.ccedk.com/api/v1/pair/list?nonce=1410950600
         * 2 ,BTC USD
         * 14 , PPC USD
         * 13 , PPC BTC
         * 12 , PPC LTC
         * TODO Add them here when they add support
         * NBT BTC
         * NBT PPC
         * NBT USD
         */

        int toRet = -1;

        if (pair.equals(Constant.BTC_USD)) {
            return 2;
        } else if (pair.equals(Constant.PPC_USD)) {
            return 14;
        } else if (pair.equals(Constant.PPC_BTC)) {
            return 13;
        } else if (pair.equals(Constant.PPC_LTC)) {
            return 12;
        } else {
            LOG.severe("Pair " + pair.toString() + " not available");
        }

        return toRet;
    }

    public static CurrencyPair getCCEDKPairFromID(int id) {
        /*
         * LAST UPDATED : 20 september
         FROM : https://www.ccedk.com/api/v1/pair/list?nonce=1410950600
         * 2 ,BTC USD
         * 14 , PPC USD
         * 13 , PPC BTC
         * 12 , PPC LTC
         * TODO Add them here when they add support
         * NBT BTC
         * NBT PPC
         * NBT USD
         */
        CurrencyPair toRet = new CurrencyPair(Constant.BTC, Constant.BTC);

        switch (id) {
            case 2:
                toRet = Constant.BTC_USD;
                break;
            case 12:
                toRet = Constant.PPC_LTC;
                break;
            case 13:
                toRet = Constant.PPC_BTC;
                break;
            case 14:
                toRet = Constant.PPC_USD;
                break;
            default:
                LOG.severe("Pair with id = " + id + " not available");

        }
        return toRet;

    }

    public static String getCCEDKTickerUrl(CurrencyPair pair) {
        String nonce = TradeUtils.getCCDKEvalidNonce();
        return "https://www.ccedk.com/api/v1/stats/marketdepth?nonce=" + nonce + "&pair_id=" + getCCDKECurrencyPairId(pair);
    }
}
