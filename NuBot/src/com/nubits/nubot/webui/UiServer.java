package com.nubits.nubot.webui;

import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.options.SaveOptions;
import com.nubits.nubot.webui.service.StockServiceServer;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.post;

public class UiServer {

    //TODO path
    private static String htmlFolder = "./html/tmpl/";

    private static String testconfigFile = "test.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    public static void startService() {
        try {
            new StockServiceServer().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * start the UI
     *
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
            String resp = request.body();

            //Map<String, String> rmap = request.params();
            //Iterator<String> it = rmap.keySet().iterator();
            //while (it.hasNext()) {
            //    String c = it.next();
            //    resp += c;
            //
            // }
            String apikey = request.queryParams("apikey");


            //SaveOptions.backupOptions(testconfig);
            //NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig);
            //opt.setApiKey(apikey);
            //SaveOptions.saveOptions(opt,testconfig);

            //TODO: return success


            return "try put: " + apikey;
        });

    }
}