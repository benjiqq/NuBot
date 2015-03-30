package com.nubits.nubot.webui;

import com.nubits.nubot.trading.TradeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class UiServer {

    final static Logger LOG = LoggerFactory.getLogger(UiServer.class);

    //TODO via settings
    private static String htmlFolder = "./UI/templates/";

    private static TradeInterface ti;

    private static int port = 4567; //standard port, can't change

    /**
     * start the UI server
     */
    public static void startUIserver(String configdir, String configFile) {

        //set up all endpoints. currently not very pretty code.

        LOG.info("launching on http://localhost:" + port);


        //binds GET and POST
        new ConfigController("/config", configdir, configFile);

        new LogController("/logdump");

        LayoutTemplateEngine tmpl = new LayoutTemplateEngine(htmlFolder);

        Map configmap = new HashMap();
        configmap.put("configfile", configFile);

        Map opmap = new HashMap();

        get("/", (request, response) -> new ModelAndView(opmap, htmlFolder + "operation.mustache"), tmpl);


        get("/configui", (request, response) -> new ModelAndView(configmap, htmlFolder + "config.mustache"), tmpl);

        get("/about", (request, response) -> new ModelAndView(configmap, htmlFolder + "about.mustache"), tmpl);


        new BotController("/startstop");


    }



}