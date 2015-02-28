package com.nubits.nubot.options;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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

        root.addProperty("pair", opt.pair.toString("_"));

        root.addProperty("pair", opt.getPair().toString("_"));

        return root;
    }
}