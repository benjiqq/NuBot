package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.NuBotRunException;
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.launch.ShutDownProcess;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;
import static spark.Spark.get;
import static spark.Spark.post;

public class BotController {

    final static Logger LOG = LoggerFactory.getLogger(BotController.class);

    private final static String START = "start";
    private final static String STOP = "stop";

    //workaround for double stop call
    private long lastStopCall;

    public BotController() {

        get("/opstatus", "application/json", (request, response) -> {
            LOG.trace("/opstatus called");

            Map opmap = new HashMap();
            opmap.put("status", SessionManager.getReadableMode());

            boolean active = SessionManager.isSessionRunning();
            if (active) {
                opmap.put("sessionstart", SessionManager.startedString());
                opmap.put("duration", Utils.getBotUptimeDate());
            } else {
                opmap.put("sessionstart", "");
            }

            //opmap.put("stopped", SessionManager.sessionStopped);
            String json = new Gson().toJson(opmap);
            return json;
        });


        // we expect options are set
        post("/startstop", "application/json", (request, response) -> {
            LOG.debug("/startstop called");

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

                boolean active = SessionManager.isSessionRunning();
                if (active) {
                    success = false;
                    String errmsg = "could not start bot. session already running";
                    LOG.error(errmsg);
                    opmap.put("error", errmsg);
                }

                if (ParseOptions.isValidOptions(Global.options)) {
                    LOG.info("trying to start bot");
                    try {
                        SessionManager.setModeStarting();
                        SessionManager.launchBot(Global.options);
                    } catch (NuBotRunException e) {
                        success = false;
                        LOG.error("could not start bot " + e);
                        opmap.put("error", "could not start bot: " + e);
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

                //workaround for double call, see issue #643
                long dif = System.currentTimeMillis() - lastStopCall;
                lastStopCall = System.currentTimeMillis();

                //prevent double calls
                if (dif < 5000) {
                    opmap.put("success", true);
                    json = new Gson().toJson(opmap);
                    return json;
                }

                boolean success = true;
                if (SessionManager.isSessionRunning()) {
                    try {
                        LOG.info("try interrupt bot. set halting mode");

                        SessionManager.setModeHalting();

                        Global.bot.shutdownBot();

                        Global.mainThread.interrupt();

                    } catch (Exception e) {
                        success = false;
                        opmap.put("error", "can't interrupt : " + e);
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


        get("/stopserver", "application/json", (request, response) -> {
            LOG.trace("/stopserver called");

            //Schedule a task to shut down in 1 second
            new Thread(new ShutDownProcess()).run();
            ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(1);

            final Runnable terminatorTask = new Runnable() {
                public void run() {
                    System.exit(0);
                }
            };

            final ScheduledFuture<?> terminatorHandle =
                    scheduler.schedule(terminatorTask, 1, SECONDS);
            
            return "stopped";
        });


    }
}

