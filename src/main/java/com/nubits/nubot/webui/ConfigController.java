package com.nubits.nubot.webui;


import com.google.gson.Gson;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.options.SaveOptions;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * controller for changing Configurations.
 * GET from predefined file (which is the same as global.options)
 * POST user options. is loaded to global.options also
 */
public class ConfigController {

    private String configDir;
    private String configfile;

    final static Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    private void saveConfig(NuBotOptions newopt){

        LOG.info("parsed: new opt: " + newopt);

        try {
            SaveOptions.backupOptions(this.configDir + File.separator + this.configfile);
        } catch(IOException e){
            LOG.info("error with backup " + e);
        }

        String saveTo = this.configDir + File.separator + this.configfile;
        String js = SaveOptions.jsonPretty(newopt);
        LOG.info("new opt: " + js);

        boolean savesuccess = true;
        try {
            SaveOptions.saveOptionsPretty(newopt, saveTo);
        }catch(Exception e){
            LOG.info("error saving " + e);
            savesuccess = false;
        }

        if (savesuccess)
            Global.options = newopt;

    }

    public ConfigController(String endpoint, String configDir, String configfile) {

        this.configDir = configDir;
        this.configfile = configfile;

        get(endpoint, "application/json", (request, response) -> {

            //get from memory. any change in the file is reflected in the global options
            String jsonString = NuBotOptions.optionsToJson(Global.options);
            return jsonString;
        });


        post(endpoint, "application/json", (request, response) -> {

            //check if bot is running
            boolean active = SessionManager.isSessionActive();
            LOG.info("session currently active " + active);

            if (active){
                //if bot is running needs show an error
                Map opmap = new HashMap();
                opmap.put("success", false);
                opmap.put("error", "Session running: can't save config");
                String json = new Gson().toJson(opmap);
                return json;
            }


            LOG.info("config received post" + request);
            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = null;
            try {
                postJson = (JSONObject) (parser.parse(json_body));
            } catch (ParseException e) {

            }

            LOG.info("the JSON of the post " + postJson);

            boolean success = true;

            NuBotOptions newopt = null;
            Map opmap = new HashMap();
            String error = "none";
            try{
                newopt = ParseOptions.parsePost(postJson);
            }catch(Exception e){
                LOG.error("error parsing " + postJson + "\n" + e);
                //handle errors
                success = false;
                error = "" + e;
            }

            opmap.put("success", success);

            if (success) {
                saveConfig(newopt);
            }

            opmap.put("error", error);

            String json = new Gson().toJson(opmap);

            return json;

        });

    }
}
