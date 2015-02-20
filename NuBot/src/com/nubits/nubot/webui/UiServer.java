package com.nubits.nubot.webui;

import static spark.Spark.get;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
 
public class UiServer {
    
 
    public static void main(String[] args) {
        
        try {
            new StockServiceServer().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Map map = new HashMap();
        map.put("name", "Yo");

        String htmlFolder = ""

        get("/", (rq, rs) -> new ModelAndView(map, "./html/tmpl/config.mustache"), new LayoutTemplateEngine());

        get("/log", (rq, rs) -> new ModelAndView(map, "./html/tmpl/log.mustache"), new LayoutTemplateEngine());
    }
}