package com.nubits.nubot.webui;


import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.SaveOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * controller for changing Configurations
 */
public class ConfigController {


    private String configDir;
    private String configfile;
    private NuBotOptions opt;

    final static Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    public ConfigController(String endpoint, NuBotOptions opt, String configDir, String configfile) {
        this.opt = opt;
        this.configDir = configDir;
        this.configfile = configfile;

        //Msg keyMsg = new Msg(opt.getApiKey(), opt.getApiSecret());

        get(endpoint, "application/json", (request, response) -> {
            String resp = "" + this.opt;
            System.out.println("response " + resp);
            LOG.warn(resp);
            System.out.println(this.opt.getNubitAddress());
            return resp;
        }, new JsonTransformer());

        post(endpoint, "application/json", (request, response) -> {

            //not working. put is in request body
            //request.queryParams("apikey");

            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = null;
            try {
                postJson = (JSONObject) (parser.parse(json_body));
            } catch (ParseException e) {

            }

            NuBotOptions newopt = new NuBotOptions();

            //System.out.println(">>>> " + postJson);
            String variableset = "none";

            if (postJson.containsKey("exchangename")) {
                String newvalue = "" + postJson.get("exchangename");
                newopt.setExchangeName(newvalue);
                variableset = "exchangename";
            }

            if (postJson.containsKey("apikey")) {
                String newapikey = "" + postJson.get("apikey");
                newopt.setApiKey(newapikey);
                variableset = "apikey";
            }

            if (postJson.containsKey("apisecret")) {
                String newsecret = "" + postJson.get("apisecret");
                newopt.setApiSecret(newsecret);
                variableset = "apisecret";
            }

            if (postJson.containsKey("dualside")) {
                boolean newv = (boolean) postJson.get("dualside");
                newopt.setDualSide(newv);
                variableset = "dualside";
            }

            if (postJson.containsKey("multiplecustodians")) {
                boolean newv = (boolean) postJson.get("multiplecustodians");
                newopt.setDualSide(newv);
                variableset = "multiplecustodians";
            }

            if (postJson.containsKey("submitliquidity")) {
                boolean newv = (boolean) postJson.get("submitliquidity");
                newopt.setSubmitLiquidity(newv);
                variableset = "submitliquidity";
            }

            if (postJson.containsKey("executeorders")) {
                boolean newv = (boolean) postJson.get("executeorders");
                newopt.setExecuteOrders(newv);
                variableset = "executeorders";
            }

            if (postJson.containsKey("verbose")) {
                boolean newv = (boolean) postJson.get("verbose");
                newopt.setVerbose(newv);
                variableset = "verbose";
            }

            if (postJson.containsKey("hipchat")) {
                boolean newv = (boolean) postJson.get("hipchat");
                newopt.setSendHipchat(newv);
                variableset = "hipchat";
            }

            if (postJson.containsKey("nubitaddress")) {
                String newv = "" + postJson.get("nubitaddress");
                newopt.setNubitAddress(newv);
                variableset = "nubitaddress";
            }


            if (postJson.containsKey("nudport")) {
                int newv = (Integer) postJson.get("nudport");
                newopt.setNudPort(newv);
                variableset = "nudport";
            }


            SaveOptions.backupOptions(this.configDir + File.separator + this.configfile);

            String saveTo = this.configDir + File.separator + this.configfile;
            String js = SaveOptions.jsonPretty(this.opt);
            System.out.println("new opt: " + js);

            //TODO: test validity of posted options
            boolean valid = true;

            this.opt = newopt;

            SaveOptions.saveOptionsPretty(this.opt, saveTo);

            //TODO: return as json for Frontend
            boolean success = true;
            return "success: " + success + " variableset" + variableset;
        });

    }
}
