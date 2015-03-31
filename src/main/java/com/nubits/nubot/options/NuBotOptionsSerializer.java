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


import com.google.gson.*;
import com.nubits.nubot.models.CurrencyPair;

import java.lang.reflect.Type;


public class NuBotOptionsSerializer implements JsonSerializer<NuBotOptions> {

    @Override
    public JsonElement serialize(NuBotOptions opt, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();

        root.addProperty("exchangename", opt.exchangeName);
        root.addProperty("apikey", opt.apiKey);
        root.addProperty("apisecret", opt.apiSecret);
        root.addProperty("dualside", opt.dualSide);
        root.addProperty("submitliquidity", opt.submitLiquidity);
        root.addProperty("multiplecustodians", opt.multipleCustodians);
        root.addProperty("executeorders", opt.executeOrders);
        root.addProperty("verbose", opt.verbose);
        root.addProperty("hipchat", opt.sendHipchat);

        root.addProperty("mailnotifications", opt.sendMails);
        root.addProperty("mailrecipient", opt.mailRecipient);
        root.addProperty("emergencytimeout", opt.emergencyTimeout);
        root.addProperty("keepproceeds", opt.keepProceeds);
        root.addProperty("maxsellordervolume", opt.maxSellVolume);
        root.addProperty("maxbuyordervolume", opt.maxBuyVolume);
        root.addProperty("priceincrement", opt.priceIncrement);
        root.addProperty("nubitaddress", opt.nubitAddress);
        root.addProperty("nudport", opt.nudPort);
        root.addProperty("nudip", opt.nudIp);
        root.addProperty("rpcpass", opt.rpcPass);
        root.addProperty("rpcuser", opt.rpcUser);
        root.addProperty("mainFeed", opt.mainFeed);
        root.addProperty("wallchangethreshold", opt.wallchangeThreshold);
        root.addProperty("txfee", opt.txFee);
        root.addProperty("emergencytimeout", opt.emergencyTimeout);
        root.addProperty("spread", opt.spread);
        root.addProperty("mainfeed", opt.mainFeed);
        root.addProperty("backupfeeds", new Gson().toJson(opt.backupFeedNames));

        root.addProperty("pair", opt.pair.toStringSep());

        //public ArrayList<String> backupFeedNames;
        //    public double priceIncrement;
        // public String sendMails;


        //public int executeStrategyInterval; //disabled
        //public int sendLiquidityInterval; //disabled
        //public boolean distributeLiquidity;
        //public boolean aggregate;

        return root;
    }
}