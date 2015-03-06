package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.get;


public class LogController {

    /**
     * the log file to send to client
     */
    String logfile = "logs/info.log";

    public LogController(String endpoint) {

        get(endpoint, "application/json", (request, response) -> {

            JsonObject object = new JsonObject();

            try {
                String l = new String(Files.readAllBytes(Paths.get(logfile)));
                object.addProperty("log", l);
                return object;
            }catch(Exception e){

            }

            return "error fetching log";

        });


    }
}
