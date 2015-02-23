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
        root.addProperty("dualside", opt.getApiSecret());

        root.addProperty("submitliquidity", opt.isSendRPC());


        root.addProperty("pair", opt.getPair().toString());

        return root;
    }
}