package com.nubits.nubot.webui;


import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.options.SaveOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * controller for changing Configurations
 */
public class ConfigController {

    private static String testconfigFile = "test.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    public ConfigController() {

        Msg keyMsg = new Msg("key");

        get("/keys", "application/json", (request, response) -> {
            return keyMsg;
        }, new JsonTransformer());

        post("/keys", "application/json", (request, response) -> {

            //not working. put is in request body
            //request.queryParams("apikey");

            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = (JSONObject) (parser.parse(json_body));

            String newapikey = "" + postJson.get("apikey");

            SaveOptions.backupOptions(testconfig);
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig);
            opt.setApiKey(newapikey);
            SaveOptions.saveOptions(opt, testconfig);

            //TODO: return success(?)

            System.out.println("got post key " + newapikey);


            return "try put new " + newapikey;
        });

    }
}
