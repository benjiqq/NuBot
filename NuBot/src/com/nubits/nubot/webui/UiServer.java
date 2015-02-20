package com.nubits.nubot.webui;

import static spark.Spark.get;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
 
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
        map.put("name", "Yo");


        get("/", (rq, rs) -> new ModelAndView(map, htmlFolder + "config.mustache"), new LayoutTemplateEngine());

        get("/log", (rq, rs) -> new ModelAndView(map, htmlFolder + "log.mustache"), new LayoutTemplateEngine());


        Msg keyMsg = new Msg("key");

        get("/hello", "application/json", (request, response) -> {
            return keyMsg;
        }, new JsonTransformer());

    }
}