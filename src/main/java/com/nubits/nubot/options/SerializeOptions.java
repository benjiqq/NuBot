package com.nubits.nubot.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.bot.Global;

/**
 * Utility for serializing options to JSON
 */
public class SerializeOptions {

    public static String optionsToJson(NuBotOptions opt){
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);
        return js;
    }
}
