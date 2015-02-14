package com.nubits.nubot.webui;

import static spark.Spark.get;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
 
public class Spark {
    
 
    public static void main(String[] args) {
        get("/", (req, res) -> "Hello World");
        
        Map map = new HashMap();
        map.put("name", "Yo");

        // hello.mustache file is in resources/templates directory
        get("/hello", (rq, rs) -> new ModelAndView(map, "./html/tmpl/hello.mustache"), new MustacheTemplateEngine());
    }
}