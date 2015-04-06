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
import com.google.gson.reflect.TypeToken;
import com.nubits.nubot.models.CurrencyPair;

import java.lang.reflect.Type;
import java.util.ArrayList;


/*public class NuBotOptionsSerializer implements JsonSerializer<NuBotOptions> {

    @Override
    public JsonElement serialize(NuBotOptions opt, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();

        root.addProperty(ParseOptions.exchangename, opt.exchangeName);
        root.addProperty(ParseOptions.apikey, opt.apiKey);
        root.addProperty(ParseOptions.apisecret, opt.apiSecret);
        root.addProperty(ParseOptions.txfee, opt.txFee);
        root.addProperty(ParseOptions.pair, opt.pair);
        root.addProperty(ParseOptions.dualside, opt.dualSide);
        root.addProperty(ParseOptions.multiplecustodians, opt.multipleCustodians);
        root.addProperty(ParseOptions.executeorders, opt.executeOrders);
        root.addProperty(ParseOptions.verbose, opt.verbose);
        root.addProperty(ParseOptions.hipchat, opt.hipchat);

        root.addProperty(ParseOptions.mailnotifications, opt.mailnotifications);
        root.addProperty(ParseOptions.mailrecipient, opt.mailRecipient);
        root.addProperty(ParseOptions.emergencytimeout, opt.emergencyTimeout);
        root.addProperty(ParseOptions.keepproceeds, opt.keepProceeds);
        root.addProperty(ParseOptions.maxsellvolume, opt.maxSellVolume);
        root.addProperty(ParseOptions.maxbuyvolume, opt.maxBuyVolume);
        root.addProperty(ParseOptions.priceincrement, opt.priceIncrement);
        root.addProperty(ParseOptions.submitliquidity, opt.submitLiquidity);
        root.addProperty(ParseOptions.nubitaddress, opt.nubitAddress);
        root.addProperty(ParseOptions.nudip, opt.nudIp);
        root.addProperty(ParseOptions.nudport, opt.nudPort);
        root.addProperty(ParseOptions.rpcpass, opt.rpcPass);
        root.addProperty(ParseOptions.rpcuser, opt.rpcUser);
        root.addProperty(ParseOptions.wallchangethreshold, opt.wallchangeThreshold);
        root.addProperty(ParseOptions.spread, opt.spread);
        root.addProperty(ParseOptions.mainfeed, opt.mainFeed);

        String bfs = new Gson().toJson(opt.backupFeeds,new TypeToken<ArrayList<String>>(){}.getType());
        root.addProperty(ParseOptions.backupfeeds, bfs);

        return root;
    }
}*/