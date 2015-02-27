package com.nubits.nubot.options;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class OptionsSerializer implements JsonSerializer<NuBotOptions> {

    @Override
    public JsonElement serialize(NuBotOptions opt, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject root = new JsonObject();


        root.addProperty("exchangename", opt.getExchangeName());
        root.addProperty("apikey", opt.getApiKey());
        root.addProperty("apisecret", opt.getApiSecret());
        root.addProperty("dualside", opt.isDualSide());
        root.addProperty("submitliquidity", opt.isSubmitliquidity());
        root.addProperty("multiplecustodians", opt.isMultipleCustodians());
        root.addProperty("executeorders", opt.isExecuteOrders());
        root.addProperty("verbose", opt.isVerbose());
        root.addProperty("hipchat", opt.isSendHipchat());

        root.addProperty("mailnotifications", opt.sendMailsLevel());
        root.addProperty("mailrecipient", opt.getMailRecipient());
        root.addProperty("emergencytimeout", opt.getEmergencyTimeout());
        root.addProperty("keepproceeds", opt.getKeepProceeds());
        root.addProperty("maxsellordervolume", opt.getMaxSellVolume());
        root.addProperty("maxbuyordervolume", opt.getMaxBuyVolume());
        root.addProperty("priceincrement", opt.getPriceIncrement());

        root.addProperty("pair", opt.getPair().toString("_"));

        return root;
    }
}