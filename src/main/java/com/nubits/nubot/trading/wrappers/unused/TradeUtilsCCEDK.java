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

package com.nubits.nubot.trading.wrappers.unused;


import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class TradeUtilsCCEDK {

    private static final Logger LOG = LoggerFactory.getLogger(TradeUtilsCCEDK.class.getName());

    public static String getCCDKEvalidNonce() {
        //It tries to send a wrong nonce, get the allowed window, and use it for the actual call
        String wrongNonce = "1234567891";
        String lastdigits;
        //LOG.info("Offset = " + Objects.toStringSep(offset));
        String validNonce;
        if (TradeUtils.offset == 0) {
            try {
                String htmlString = Utils.getHTML("https://www.ccedk.com/api/v1/currency/list?nonce=" + wrongNonce, false);
                //LOG.info(htmlString);
                //LOG.info(Objects.toStringSep(System.currentTimeMillis() / 1000L));
                validNonce = getCCDKEvalidNonce(htmlString);
                TradeUtils.offset = Integer.parseInt(validNonce) - (int) (System.currentTimeMillis() / 1000L);
                //LOG.info("Offset = " + Objects.toStringSep(offset));
            } catch (IOException io) {
                //LOG.info(io.toStringSep());
                validNonce = "";
            }
        } else {
            validNonce = Objects.toString(((int) (System.currentTimeMillis() / 1000L) + TradeUtils.offset) - 1);
        }
        if (!validNonce.equals("")) {
            lastdigits = validNonce.substring(validNonce.length() - 2);
            if (lastdigits.equals("98") || lastdigits.equals("99")) {
                TradeUtils.offset = 0;
                validNonce = getCCDKEvalidNonce();
            }
        } else {
            TradeUtils.offset = 0;
            validNonce = getCCDKEvalidNonce();
        }
        //LOG.info("Last digits = " + lastdigits + "\nvalidNonce = " + validNonce);
        return validNonce;
    }

    public static String getCCDKEvalidNonce(String htmlString) {
        //used by ccedkqueryservice

        JSONParser parser = new JSONParser();
        try {
            //{"errors":{"nonce":"incorrect range `nonce`=`1234567891`, must be from `1411036100` till `1411036141`"}
            JSONObject httpAnswerJson = (JSONObject) (parser.parse(htmlString));
            JSONObject errors = (JSONObject) httpAnswerJson.get("errors");
            String nonceError = (String) errors.get("nonce");

            //String startStr = " must be from";
            //int indexStart = nonceError.lastIndexOf(startStr) + startStr.length() + 2;
            //String from = nonceError.substring(indexStart, indexStart + 10);


            String startStr2 = " till";
            int indexStart2 = nonceError.lastIndexOf(startStr2) + startStr2.length() + 2;
            String to = nonceError.substring(indexStart2, indexStart2 + 10);

            //if (to.equals(from)) {
            //    LOG.info("Detected ! " + to + " = " + from);
            //    return "retry";
            //}

            return to;
        } catch (ParseException ex) {
            LOG.error(htmlString + " " + ex.toString());
            return "1234567891";
        }
    }

    public static int getCCDKECurrencyId(String currencyCode) {
        /*
         * LAST UPDATED : 17 september
         * FROM : https://www.ccedk.com/api/v1/currency/list?nonce=1410950600
         * 1,LTC
         * 2,BTC
         * 3,USD
         * 4, EUR
         * 8,PPC
         * 15, NBT

         */
        final String BTC = CurrencyList.BTC.getCode();
        String USD = CurrencyList.USD.getCode();
        String PPC = CurrencyList.PPC.getCode();

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
            case "NBT":
                toRet = 15;
                break;
            case "EUR":
                toRet = 4;
                break;
            default:
                LOG.error("Currency " + currencyCode + "not available");
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
         * 47, NBT BTC
         * 48, NBT PPC
         * 46, NBT USD
         * 49, NBT EUR
         */

        int toRet = -1;

        if (pair.equals(CurrencyList.BTC_USD)) {
            return 2;
        } else if (pair.equals(CurrencyList.PPC_USD)) {
            return 14;
        } else if (pair.equals(CurrencyList.PPC_BTC)) {
            return 13;
        } else if (pair.equals(CurrencyList.PPC_LTC)) {
            return 12;
        } else if (pair.equals(CurrencyList.NBT_BTC)) {
            return 47;
        } else if (pair.equals(CurrencyList.NBT_PPC)) {
            return 48;
        } else if (pair.equals(CurrencyList.NBT_USD)) {
            return 46;
        } else if (pair.equals(CurrencyList.NBT_EUR)) {
            return 49;
        } else {
            LOG.error("Pair " + pair.toString() + " not available");
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
         * 47, NBT BTC
         * 48, NBT PPC
         * 46, NBT USD
         * 49, NBT EUR
         */
        CurrencyPair toRet = new CurrencyPair(CurrencyList.BTC, CurrencyList.BTC);

        switch (id) {
            case 2:
                toRet = CurrencyList.BTC_USD;
                break;
            case 12:
                toRet = CurrencyList.PPC_LTC;
                break;
            case 13:
                toRet = CurrencyList.PPC_BTC;
                break;
            case 14:
                toRet = CurrencyList.PPC_USD;
                break;
            case 46:
                toRet = CurrencyList.NBT_USD;
                break;
            case 47:
                toRet = CurrencyList.NBT_BTC;
                break;
            case 48:
                toRet = CurrencyList.NBT_PPC;
                break;
            case 49:
                toRet = CurrencyList.NBT_EUR;
                break;
            default:
                LOG.error("Pair with id = " + id + " not available");

        }
        return toRet;

    }

    public static String getCCEDKTickerUrl(CurrencyPair pair) {
        String nonce = getCCDKEvalidNonce();
        return "https://www.ccedk.com/api/v1/stats/marketdepth?nonce=" + nonce + "&pair_id=" + getCCDKECurrencyPairId(pair);
    }
}
