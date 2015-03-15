package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class UiServer {

    final static Logger LOG = LoggerFactory.getLogger(UiServer.class);

    //TODO via resources
    private static String htmlFolder = "./UI/templates/";

    private static TradeInterface ti;

    private static int port = 4567; //standard port, can't change

    /**
     * start the UI server
     */
    public static void startUIserver(String configdir, String configFile) {

        //set up all endpoints. currently not very pretty code.

        LOG.info("launching on http://localhost:" + port);

        //TODO: only load if in testmode and this is not set elsewhere
        try{
            Utils.loadProperties("settings.properties");
        }catch(Exception e){
            LOG.error("error loading properties");
        }

        //binds GET and POST
        new ConfigController("/config", configdir, configFile);

        new LogController("/logdump");

        LayoutTemplateEngine tmpl = new LayoutTemplateEngine(htmlFolder);

        Map feedsmap = new HashMap();
        //TODO: pegging price
        feedsmap.put("watchcurrency", Global.options.pair.toString());
        get("/feeds", (request, response) -> new ModelAndView(feedsmap, htmlFolder + "feeds.mustache"), tmpl);

        Map configmap = new HashMap();
        configmap.put("configfile", configFile);

        //TODO
        //ti = Global.exchange.getTradeInterface();

        Map opmap = new HashMap();
        opmap.put("exchange", Global.options.getExchangeName());
        opmap.put("btc_balance", 0);
        opmap.put("nbt_balance", 0);

        //String json = new Gson().toJson(ExchangeFacade.getOpenOrders(ti));
        opmap.put("orders", "");

        get("/", (request, response) -> new ModelAndView(opmap, htmlFolder + "operation.mustache"), tmpl);


        new DataController("/status");

        get("/configui", (request, response) -> new ModelAndView(configmap, htmlFolder + "config.mustache"), tmpl);

        get("/about", (request, response) -> new ModelAndView(configmap, htmlFolder + "about.mustache"), tmpl);



        new BotController("/startstop");

        //get("/tools", (request, response) -> new ModelAndView(configmap, htmlFolder + "tools.mustache"), new LayoutTemplateEngine(htmlFolder));


    }



}