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

        root.addProperty("mail-notifications", opt.sendMailsLevel());
        root.addProperty("mailrecipient", opt.getMailRecipient());
        root.addProperty("emergency-timeout", opt.getEmergencyTimeout());
        root.addProperty("keep-proceeds", opt.getKeepProceeds());
        root.addProperty("max-sell-order-volume", opt.getMaxSellVolume());
        root.addProperty("max-buy-order-volume", opt.getMaxBuyVolume());
        root.addProperty("priceincrement", opt.getPriceIncrement());

        root.addProperty("pair", opt.getPair().toString("_"));

        return root;
    }
}