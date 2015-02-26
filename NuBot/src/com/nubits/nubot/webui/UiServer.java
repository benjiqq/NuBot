package com.nubits.nubot.webui;

import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class UiServer {

    //TODO path
    private static String htmlFolder = "./html/tmpl/";

    private static String testconfigFile = "test.json";
    private static String testconfigpath = "testconfig/" + testconfigFile;

    /**
     * start the UI server
     */
    public static void startUIserver(String[] args) {

        //TODO: only load if in testmode and this is not set elsewhere
        //should better read: load global settings
        Utils.loadProperties("settings.properties");
        if (args.length == 1) {
            try {
                String configFilePath = args[0]; //testconfigpath
                NuBotOptions opt = ParseOptions.parseOptionsSingle(configFilePath);
                new ConfigController(opt, testconfigpath);
            } catch (NuBotConfigException e) {
                System.out.println("could not parse config");
                System.out.println(e);
                System.exit(0);
            }
        } else if ((args.length > 1)|| (args.length ==0)){
            System.out.println("single file config only");
            System.exit(0);
        }

        Map map = new HashMap();

        get("/", (rq, rs) -> new ModelAndView(map, htmlFolder + "config.mustache"), new LayoutTemplateEngine());

        get("/log", (rq, rs) -> new ModelAndView(map, htmlFolder + "log.mustache"), new LayoutTemplateEngine());


    }


    public static void main(String[] args) {

        startUIserver(args);
    }
}