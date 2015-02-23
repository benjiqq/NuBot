package com.nubits.nubot.webui;


import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.SaveOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * controller for changing Configurations
 */
public class ConfigController {


    private String testconfigfile;
    private NuBotOptions opt;

    public ConfigController(NuBotOptions opt, String testconfigfile) {
        this.testconfigfile = testconfigfile;

        Msg keyMsg = new Msg(opt.getApiKey(), opt.getApiSecret());

        get("/keys", "application/json", (request, response) -> {
            return keyMsg;
        }, new JsonTransformer());

        post("/keys", "application/json", (request, response) -> {

            //not working. put is in request body
            //request.queryParams("apikey");

            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = (JSONObject) (parser.parse(json_body));
            //System.out.println(">>>> " + postJson);
            String variableset = "none";

            if (postJson.containsKey("apikey")) {
                String newapikey = "" + postJson.get("apikey");
                opt.setApiKey(newapikey);
                variableset = "apikey";
            }

            if (postJson.containsKey("apisecret")) {
                String newsecret = "" + postJson.get("apisecret");
                opt.setApiSecret(newsecret);
                variableset = "apisecret";
            }

            if (postJson.containsKey("dualside")) {
                boolean newv = (boolean) postJson.get("dualside");
                opt.setDualSide(newv);
                variableset = "dualside";
            }

            if (postJson.containsKey("multiplecustodians")) {
                boolean newv = (boolean) postJson.get("multiplecustodians");
                opt.setDualSide(newv);
                variableset = "multiplecustodians";
            }

            if (postJson.containsKey("submitliquidity")) {
                boolean newv = (boolean) postJson.get("submitliquidity");
                opt.setDualSide(newv);
                variableset = "submitliquidity";
            }

            if (postJson.containsKey("executeorders")) {
                boolean newv = (boolean) postJson.get("executeorders");
                opt.setDualSide(newv);
                variableset = "executeorders";
            }

            if (postJson.containsKey("verbose")) {
                boolean newv = (boolean) postJson.get("verbose");
                opt.setDualSide(newv);
                variableset = "verbose";
            }

            if (postJson.containsKey("hipchat")) {
                boolean newv = (boolean) postJson.get("hipchat");
                opt.setDualSide(newv);
                variableset = "hipchat";
            }



            SaveOptions.backupOptions(this.testconfigfile);
            //NuBotOptions opt = ParseOptions.parseOptionsSingle(thils.testconfigfile);

            //SaveOptions.saveOptions(opt, testconfig);
            SaveOptions.saveOptionsPretty(opt, "testconfig/new.json");

            //TODO: as json
            boolean success = true;
            return "success: " + success + " variableset" + variableset;
        });

    }
}
