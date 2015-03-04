package com.nubits.nubot.webui;


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
            return opt;
        }, new JsonTransformerOptions());

        post(endpoint, "application/json", (request, response) -> {

            //not working. put is in request body
            //request.queryParams("apikey");
            LOG.info("config received post" + request);
            String json_body = request.body();

            JSONParser parser = new JSONParser();
            JSONObject postJson = null;
            try {
                postJson = (JSONObject) (parser.parse(json_body));
            } catch (ParseException e) {

            }

            LOG.info("the JSON of the post " + postJson);

            //TODO: test validity of posted options
            boolean valid = true;

            NuBotOptions newopt = null;
            try{
                newopt = ParseOptions.parsePost(postJson);
            }catch(Exception e){
                //handle errors
            }

            LOG.info("parsed: new opt: " + newopt);

            try {
                SaveOptions.backupOptions(this.configDir + File.separator + this.configfile);
            } catch(IOException e){
                LOG.info("error with backup " + e);
            }

            this.opt = newopt;

            String saveTo = this.configDir + File.separator + this.configfile;
            String js = SaveOptions.jsonPretty(this.opt);
            LOG.info("new opt: " + js);

            try {
                SaveOptions.saveOptionsPretty(this.opt, saveTo);
            }catch(Exception e){
                LOG.info("error saving " + e);
            }

            //TODO: return as json for Frontend
            boolean success = true;
            LOG.info("success " + success);
            return "success: " + success;
        });

    }
}
