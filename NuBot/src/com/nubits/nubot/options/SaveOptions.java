package com.nubits.nubot.options;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.utils.FileSystem;

public class SaveOptions {

    public static boolean saveOptions(NuBotOptions opt, String filepath) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String jsonOpt = gson.toJson(opt);
        FileSystem.writeToFile(jsonOpt, filepath, false);
        //TODO: success?
        return true;
    }
}
