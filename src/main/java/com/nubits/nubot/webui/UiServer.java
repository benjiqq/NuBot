package com.nubits.nubot.webui;

import com.nubits.nubot.global.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

/**
 * the main UI server
 */
public class UiServer {

    final static Logger LOG = LoggerFactory.getLogger(UiServer.class);

    private static int port = 4567; //standard port, can't be changed

    /**
     * start the UI server
     */
    public static void startUIserver(String configFile) {

        LOG.info("start ui server. configfile " + configFile);

        //set up all endpoints

        LOG.info("launching on http://localhost:" + port);

        //binds GET and POST
        LayoutTemplateEngine tmpl = new LayoutTemplateEngine(Settings.HTML_FOLDER);

        new ConfigController(configFile);

        new LogController("/logdump");

        //controller wants some map. perhaps this can be removed
        Map empty = new HashMap();

        get("/", (request, response) -> new ModelAndView(empty, Settings.HTML_FOLDER + "operation.mustache"), tmpl);

        Map configmap = new HashMap();
        configmap.put("configfile", configFile);
        get("/configui", (request, response) -> new ModelAndView(configmap, Settings.HTML_FOLDER + "config.mustache"), tmpl);


        get("/about", (request, response) -> new ModelAndView(empty, Settings.HTML_FOLDER + "about.mustache"), tmpl);

        get("/setup", (request, response) -> new ModelAndView(empty, Settings.HTML_FOLDER + "setup.mustache"), tmpl);

        new BotController();


    }


}