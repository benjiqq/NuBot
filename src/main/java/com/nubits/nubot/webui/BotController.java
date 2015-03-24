package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.launch.MainLaunch;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class BotController {

    final static Logger LOG = LoggerFactory.getLogger(BotController.class);

    public BotController(String endpoint) {

        get(endpoint, "application/json", (request, response) -> {

            Map opmap = new HashMap();

            opmap.put("running", Global.running);

            String json = new Gson().toJson(opmap);

            return json;
        });

        // we expect options are set
        post(endpoint, "application/json", (request, response) -> {

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

            if (startstop.equals("start")) {
                boolean success = false;

                opmap.put("success", "" + false);
                LOG.info("bot start >> " + startstop);

                try {
                    MainLaunch.executeBot(Global.options);
                } catch (Exception e) {
                    success = true;
                }
                opmap.put("success", success);

                json = new Gson().toJson(opmap);

                return json;
            }

            if (startstop.equals("stop")) {
                boolean success = true;
                try {
                    LOG.info("try interrupt bot");
                    Global.mainThread.interrupt();
                } catch (Exception e) {
                    success = false;
                }

                MainLaunch.executeBot(Global.options);
                opmap.put("success", success);

                json = new Gson().toJson(opmap);

                return json;
            }

            return json;
        });


    }
}

