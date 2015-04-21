package com.nubits.nubot.webui;

import com.nubits.nubot.global.Settings;
import com.nubits.nubot.utils.FilesystemUtils;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Spark;

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

        String urlStr = "http://localhost:" + port;
        LOG.info("launching on " + urlStr);

        //Set folder for static resources
        Spark.externalStaticFileLocation(FilesystemUtils.getBotAbsolutePath() + "/res/UI");

        //binds GET and POST
        LayoutTemplateEngine tmpl = new LayoutTemplateEngine(Settings.HTML_FOLDER);


        new ConfigController(configFile);

        new LogController();

        //controller wants some map. perhaps this can be removed
        Map empty = new HashMap();

        get("/", (request, response) -> new ModelAndView(empty, Settings.HTML_FOLDER + "operation.mustache"), tmpl);

        Map configmap = new HashMap();
        configmap.put("configfile", configFile);
        get("/configui", (request, response) -> new ModelAndView(configmap, Settings.HTML_FOLDER + "config.mustache"), tmpl);

        get("/docu", (request, response) -> new ModelAndView(empty, Settings.HTML_FOLDER + "docu.mustache"), tmpl);

        get("/disclaimer", (request, response) -> new ModelAndView(empty, Settings.HTML_FOLDER + "disclaimer.mustache"), tmpl);

        new BotController();

        LOG.debug("Opening the system default browser :");
        Utils.launchBrowser(urlStr);

    }


}