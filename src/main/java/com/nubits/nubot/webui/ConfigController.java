package com.nubits.nubot.webui;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;
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
 * controller for changing Configurations
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
        String js = SaveOptions.jsonPretty(Global.options);
        LOG.info("new opt: " + js);

        try {
            SaveOptions.saveOptionsPretty(Global.options, saveTo);
        }catch(Exception e){
            LOG.info("error saving " + e);
        }
    }

    public ConfigController(String endpoint, String configDir, String configfile) {

        this.configDir = configDir;
        this.configfile = configfile;

        //Msg keyMsg = new Msg(opt.getApiKey(), opt.getApiSecret());

        get(endpoint, "application/json", (request, response) -> {


            GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
            gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
            Gson parser = gson.create();
            String js = parser.toJson(Global.options);
            return js;
        });


        post(endpoint, "application/json", (request, response) -> {

            //TODO: check if bot is running
            //TODO: if bot is running needs to handled safely


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
            try{
                newopt = ParseOptions.parsePost(postJson);
            }catch(Exception e){
                //handle errors
                success = false;
                opmap.put("error", e);
            }


            opmap.put("success", success);

            if (success) {
                saveConfig(newopt);
                opmap.put("error", "none");
            }

            String json = new Gson().toJson(opmap);

            return json;

        });

    }
}
