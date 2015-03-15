package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.nubits.nubot.bot.Global;
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

        post(endpoint, "application/json", (request, response) -> {

            Map opmap = new HashMap();
            boolean success = false;

            // we expect options are set

            String s= request.params().get("start");
            LOG.info("bot >> " + s);

            //MainLaunch.executeBot(Global.options);

            opmap.put("success", success);

            String json = new Gson().toJson(opmap);

            return json;

        });


    }
}

