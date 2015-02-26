package com.nubits.nubot.webui;


import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.SaveOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * controller for changing Configurations
 */
public class ConfigController {


    private String configDir;
    private String testconfigfile;
    private NuBotOptions opt;

    public ConfigController(NuBotOptions opt, String configDir, String testconfigfile) {
        this.opt = opt;
        this.configDir = configDir;
        this.testconfigfile = testconfigfile;

        //Msg keyMsg = new Msg(opt.getApiKey(), opt.getApiSecret());

        get("/config", "application/json", (request, response) -> {
            return opt;
        }, new JsonTransformer());

        post("/config", "application/json", (request, response) -> {

            //not working. put is in request body
            //request.queryParams("apikey");

            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = (JSONObject) (parser.parse(json_body));
            //System.out.println(">>>> " + postJson);
            String variableset = "none";

            if (postJson.containsKey("exchangename")) {
                String newvalue = "" + postJson.get("exchangename");
                this.opt.setExchangeName(newvalue);
                variableset = "exchangename";
            }

            if (postJson.containsKey("apikey")) {
                String newapikey = "" + postJson.get("apikey");
                this.opt.setApiKey(newapikey);
                variableset = "apikey";
            }

            if (postJson.containsKey("apisecret")) {
                String newsecret = "" + postJson.get("apisecret");
                this.opt.setApiSecret(newsecret);
                variableset = "apisecret";
            }

            if (postJson.containsKey("dualside")) {
                boolean newv = (boolean) postJson.get("dualside");
                this.opt.setDualSide(newv);
                variableset = "dualside";
            }

            if (postJson.containsKey("multiplecustodians")) {
                boolean newv = (boolean) postJson.get("multiplecustodians");
                this.opt.setDualSide(newv);
                variableset = "multiplecustodians";
            }

            if (postJson.containsKey("submitliquidity")) {
                boolean newv = (boolean) postJson.get("submitliquidity");
                this.opt.setSubmitLiquidity(newv);
                variableset = "submitliquidity";
            }

            if (postJson.containsKey("executeorders")) {
                boolean newv = (boolean) postJson.get("executeorders");
                this.opt.setExecuteOrders(newv);
                variableset = "executeorders";
            }

            if (postJson.containsKey("verbose")) {
                boolean newv = (boolean) postJson.get("verbose");
                this.opt.setVerbose(newv);
                variableset = "verbose";
            }

            if (postJson.containsKey("hipchat")) {
                boolean newv = (boolean) postJson.get("hipchat");
                this.opt.setSendHipchat(newv);
                variableset = "hipchat";
            }

            if (postJson.containsKey("nubitaddress")) {
                String newv = "" + postJson.get("nubitaddress");
                this.opt.setNubitAddress(newv);
                variableset = "nubitaddress";
            }


            if (postJson.containsKey("nudport")) {
                int newv = (Integer) postJson.get("nudport");
                this.opt.setNudPort(newv);
                variableset = "nudport";
            }


            //NuBotOptions opt = ParseOptions.parseOptionsSingle(thils.testconfigfile);

            //SaveOptions.saveOptions(opt, testconfig);
            SaveOptions.backupOptions(this.configDir + File.separator + this.testconfigfile);

            String saveTo = this.configDir + File.separator + this.testconfigfile;
            String js = SaveOptions.jsonPretty(this.opt);
            System.out.println("new opt: " + js);
            SaveOptions.saveOptionsPretty(this.opt, saveTo);

            //TODO: as json
            boolean success = true;
            return "success: " + success + " variableset" + variableset;
        });

    }
}
