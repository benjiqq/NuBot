package com.nubits.nubot.webui;

import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import spark.ModelAndView;
import spark.SparkBase;
import spark.webserver.SparkServer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static spark.Spark.get;

public class UiServer {

    private static final Logger LOG = Logger.getLogger(UiServer.class.getName());

    //TODO path
    private static String htmlFolder = "./html/tmpl/";

    private static String testconfigFile = "test.json";
    private static String configdir = "testconfig";
    private static String testconfigpath = "testconfig/" + testconfigFile;

    /**
     * start the UI server
     */
    public static void startUIserver(NuBotOptions opt) {

        //TODO: only load if in testmode and this is not set elsewhere
        //should better read: load global settings
        Utils.loadProperties("settings.properties");

        new ConfigController(opt, configdir, testconfigFile);

        Map map = new HashMap();

        get("/", (rq, rs) -> new ModelAndView(map, htmlFolder + "config.mustache"), new LayoutTemplateEngine());

        get("/log", (rq, rs) -> new ModelAndView(map, htmlFolder + "log.mustache"), new LayoutTemplateEngine());


    }


    public static void main(String[] args) {

        LOG.info("starting UI server");

        try{
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfigpath);
            LOG.info("using options " + opt);
            startUIserver(opt);
        }catch(Exception ex){
            LOG.severe("error configuring " + ex);
        }



        /*if (args.length == 1) {
            try {
                String configFilePath = args[0]; //testconfigpath
                NuBotOptions opt = ParseOptions.parseOptionsSingle(configFilePath);

                startUIserver(opt);

            } catch (NuBotConfigException e) {
                System.out.println("could not parse config");
                System.out.println(e);
                System.exit(0);
            }
        } else if ((args.length > 1)|| (args.length ==0)){
            System.out.println("single file config only");
            System.exit(0);
        }*/


    }
}