package com.nubits.nubot.webui;

import com.google.gson.JsonObject;
import com.nubits.nubot.bot.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.get;


public class LogController {

    /**
     * the log file to send to client
     */
    String logfile = Global.sessionPath + "/ui_standard.log";
    String verboselogfile = Global.sessionPath + "/ui_verbose.log";

    final static Logger LOG = LoggerFactory.getLogger(LogController.class);

    public LogController(String endpoint) {

        get(endpoint, "application/json", (request, response) -> {

            JsonObject object = new JsonObject();

            String f  = logfile;
            if (Global.options.isVerbose())
                f = verboselogfile;

            try {
                String l = new String(Files.readAllBytes(Paths.get(f)));

                object.addProperty("log", l);
                return object;

            } catch (Exception e) {

            }

            return "error fetching log";

        });


    }
}
