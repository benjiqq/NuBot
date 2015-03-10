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

    private static String configFile = "poloniex.json";
    private static String configdir = "testconfig";

    //TODO path
    private static String htmlFolder = "./UI/templates/";

    private static String configpath = configdir + "/" + configFile;

    private static TradeInterface ti;

    /**
     * start the UI server
     */
    public static void startUIserver(NuBotOptions opt) {

        //TODO: only load if in testmode and this is not set elsewhere
        try{
            Utils.loadProperties("settings.properties");
        }catch(Exception e){

        }


        //binds GET and POST
        new ConfigController("/config", opt, configdir, configFile);

        new LogController("/logdump");

        Map opmap = new HashMap();
        opmap.put("exchange", opt.getExchangeName());

        opmap.put("btc_balance", ExchangeFacade.getBalance(ti, CurrencyList.BTC));
        opmap.put("nbt_balance", ExchangeFacade.getBalance(ti, CurrencyList.NBT));
        String json = new Gson().toJson(ExchangeFacade.getOpenOrders(ti));
        opmap.put("orders", json);

        get("/", (request, response) -> new ModelAndView(opmap, htmlFolder + "operation.mustache"), new LayoutTemplateEngine(htmlFolder));

        Map feedsmap = new HashMap();
        //TODO: pegging price
        feedsmap.put("watchcurrency", opt.pair.toString());
        get("/feeds", (request, response) -> new ModelAndView(feedsmap, htmlFolder + "feeds.mustache"), new LayoutTemplateEngine(htmlFolder));

        Map configmap = new HashMap();
        configmap.put("configfile", configFile);

        new DataController("/status",ti);

        get("/configui", (request, response) -> new ModelAndView(configmap, htmlFolder + "config.mustache"), new LayoutTemplateEngine(htmlFolder));


        //get("/tools", (request, response) -> new ModelAndView(configmap, htmlFolder + "tools.mustache"), new LayoutTemplateEngine(htmlFolder));



    }

    public static String opttoJson(NuBotOptions opt) {
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);
        LOG.info("using options " + js);
        return js;
    }


    public static void main(String[] args) {

        LOG.debug("test debug");
        LOG.warn("test warn ");
        LOG.error("test error");

        LOG.info("starting UI server");


        /*try {
            new StockServiceServer().run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }*/

        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(configpath);
            Global.options = opt;

            //TODO: use global object and update
            ti = ExchangeFacade.exchangeInterfaceSetup(Global.options);

            startUIserver(opt);

        } catch (Exception ex) {
            LOG.error("error configuring " + ex);
        }


    }
}