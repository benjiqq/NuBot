package com.nubits.nubot.webui;

import com.nubits.nubot.webui.service.StockServiceServer;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;
 
public class UiServer {

    //TODO path
    private static String htmlFolder = "./html/tmpl/";

    public static void startService(){
        try {
            new StockServiceServer().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start the UI
     * @param args
     */
    public static void main(String[] args) {

        Map map = new HashMap();

        get("/", (rq, rs) -> new ModelAndView(map, htmlFolder + "config.mustache"), new LayoutTemplateEngine());

        get("/log", (rq, rs) -> new ModelAndView(map, htmlFolder + "log.mustache"), new LayoutTemplateEngine());


        Msg keyMsg = new Msg("key");

        get("/keys", "application/json", (request, response) -> {
            return keyMsg;
        }, new JsonTransformer());

        post("/keys", "application/json", (request, response) -> {
            return "try put: " + request.body();
        });

    }
}