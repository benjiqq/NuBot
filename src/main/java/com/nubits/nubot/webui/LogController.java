package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.strategy.Secondary.StrategySecondaryPegTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;


public class LogController {

    /**
     * the log file to send to client
     */
    String logfile = Global.sessionPath + "/ui_standard.log";
    String verboselogfile = Global.sessionPath + "/ui_verbose.log";

    final static Logger LOG = LoggerFactory.getLogger(LogController.class);

    public LogController() {

        get("/logdump", "application/json", (request, response) -> {

            JsonObject object = new JsonObject();

            String f = logfile;
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

        get("/info", "application/json", (request, response) -> {

            Map opmap = new HashMap();
            int numbuys = 0;
            int numsells = 0;
            if (Global.sessionRunning) {
                try {
                    StrategySecondaryPegTask t = (StrategySecondaryPegTask) Global.taskManager.getSecondaryPegTask().getTask();
                    t.orderManager.logActiveOrders();
                    numbuys = t.orderManager.getNumActiveBuyOrders();
                    numsells = t.orderManager.getNumActiveSellOrders();
                    LOG.info("buys: " + numbuys);
                    LOG.info("sells: " + numsells);

                } catch (Exception e) {

                }
            }

            opmap.put("buys", numbuys);
            opmap.put("sells", numsells);


            String json = new Gson().toJson(opmap);
            return json;


        });


    }
}
