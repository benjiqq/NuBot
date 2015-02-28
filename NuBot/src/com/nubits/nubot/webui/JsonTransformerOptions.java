package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import spark.ResponseTransformer;

/**
 * Render NuBotOptions to json
 */
public class JsonTransformerOptions implements ResponseTransformer {

    private Gson gson = new Gson();

    @Override
    public String render(Object model) {
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(model);

        //return gson.toJson(model);
        return js;
    }

}