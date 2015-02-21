package com.nubits.nubot.webui;


import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.options.SaveOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import static spark.Spark.get;
import static spark.Spark.post;

public class KeyController {

    private static String testconfigFile = "test.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    public KeyController() {

        Msg keyMsg = new Msg("key");

        get("/keys", "application/json", (request, response) -> {
            return keyMsg;
        }, new JsonTransformer());

        post("/keys", "application/json", (request, response) -> {


            //not working. put is in request body
            //request.queryParams("apikey");
            //Map<String, String> rmap = request.params();
            //Iterator<String> it = rmap.keySet().iterator();
            //while (it.hasNext()) {
            //    String c = it.next();
            //    resp += c;
            //
            // }

            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = (JSONObject) (parser.parse(json_body));

            String newapikey = "" + postJson.get("apikey");

            SaveOptions.backupOptions(testconfig);
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig);
            opt.setApiKey(newapikey);
            SaveOptions.saveOptions(opt,testconfig);

            //TODO: return success

            System.out.println("got post key " + newapikey);

            //response.type("application/json");
            //response.body("try put: " + apikey);
            return "try put new " + newapikey;
        });

        /*after((req, res) -> {

        });*/
    }
}
