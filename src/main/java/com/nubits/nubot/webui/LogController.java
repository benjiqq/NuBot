package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.get;


public class LogController {

    /**
     * the log file to send to client
     */
    String logfile = "./logs/main.log";

    final static Logger LOG = LoggerFactory.getLogger(LogController.class);

    public LogController(String endpoint) {

        get(endpoint, "application/json", (request, response) -> {

            JsonObject object = new JsonObject();

            try {
                String l = new String(Files.readAllBytes(Paths.get(logfile)));
                LOG.info("log fetched " + l.substring(0,50));

                object.addProperty("log", l);
                return object;
            }catch(Exception e){

            }

            return "error fetching log";

        });


    }
}
