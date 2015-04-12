package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.NuBotRunException;
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.options.ParseOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class BotController {

    final static Logger LOG = LoggerFactory.getLogger(BotController.class);

    private final static String START = "start";
    private final static String STOP = "stop";

    public BotController() {

        get("/opstatus", "application/json", (request, response) -> {

            Map opmap = new HashMap();
            boolean active = SessionManager.isSessionActive();
            if (active) {
                opmap.put("status", "running");

                opmap.put("sessionstart", SessionManager.startedString());
                opmap.put("duration", SessionManager.durationString());

            } else {
                if (SessionManager.wasRunOnce())
                    opmap.put("status", "halted");
                else
                    opmap.put("status", "not started");

                opmap.put("sessionstart", "");
            }

            opmap.put("stopped", Global.sessionStopped);
            String json = new Gson().toJson(opmap);
            return json;
        });


        // we expect options are set
        post("/startstop", "application/json", (request, response) -> {

            Map opmap = new HashMap();

            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = null;
            try {
                postJson = (JSONObject) (parser.parse(json_body));
            } catch (ParseException e) {

            }

            String startstop = "" + postJson.get("operation");
            LOG.info("bot start/stop  >> " + startstop);

            String json = new Gson().toJson(opmap);

            if (startstop.equals(START)) {
                boolean success = true;

                LOG.info("testing if global options are valid");

                boolean active = SessionManager.isSessionActive();
                if (active) {
                    success = false;
                    String errmsg = "could not start bot. session already running";
                    LOG.error(errmsg);
                    opmap.put("error", errmsg);
                }

                if (ParseOptions.isValidOptions(Global.options)) {
                    LOG.info("trying to start bot");
                    try {
                        SessionManager.launchBot(Global.options);
                    } catch (NuBotRunException e) {
                        success = false;
                        LOG.error("could not start bot " + e);
                        opmap.put("error", "" + e);
                    }
                } else {
                    success = false;
                    LOG.error("could not start bot. invalid options");
                }

                LOG.info("start bot success? " + success);
                opmap.put("success", success);

                json = new Gson().toJson(opmap);
                return json;
            }

            if (startstop.equals(STOP)) {
                boolean active = SessionManager.isSessionActive();

                boolean success = true;
                if (active) {
                    try {
                        LOG.info("try interrupt bot");

                        Global.bot.shutdownBot();

                        Global.mainThread.interrupt();

                    } catch (Exception e) {
                        success = false;
                        opmap.put("error", "can't interrupt");
                    }
                } else {
                    success = false;
                    String errmsg = "no session running";
                    LOG.info(errmsg);
                    opmap.put("error", errmsg);
                }

                opmap.put("success", success);

                json = new Gson().toJson(opmap);

                return json;
            }

            return json;
        });


    }
}

