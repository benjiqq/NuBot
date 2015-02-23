package com.nubits.nubot.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class ExampleSerial {

    public static void main(String ... args){
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(NuBotOptions.class, new OptionsSerializer());
        Gson parser = gson.create();
        System.out.println(parser.toJson(new NuBotOptions()));
    }
}
