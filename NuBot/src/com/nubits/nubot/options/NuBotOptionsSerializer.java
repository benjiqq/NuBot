package com.nubits.nubot.options;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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
        root.addProperty("secondarypeg", opt.secondarypeg);
        root.addProperty("nubitaddress", opt.nubitAddress);
        root.addProperty("nudIp", opt.nudIp);
        root.addProperty("rpcPass", opt.rpcPass);
        root.addProperty("rpcUser", opt.rpcUser);
        root.addProperty("mainFeed", opt.mainFeed);
        root.addProperty("wallchangeThreshold", opt.wallchangeThreshold);
        //TODO: rename
        root.addProperty("mailnotifications", opt.sendMails);
        root.addProperty("txFee", opt.txFee);
        root.addProperty("emergencyTimeout", opt.emergencyTimeout);

        root.addProperty("pair", opt.pair.toStringSep());
        //root.addProperty("pair", opt.pair.toStringSep("_"));

        return root;
    }
}