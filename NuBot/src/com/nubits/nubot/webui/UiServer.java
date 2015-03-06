package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Currency;
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
    private static String htmlFolder = "./html/templates/";

    private static String configpath = configdir + "/" + configFile;

    private static TradeInterface ti;

    /**
     * simplified balance query. returns -1 on error
     * @param currency
     * @return
     */
    private static double getBalance(Currency currency) {
        ApiResponse balancesResponse = ti.getAvailableBalance(currency);
        if (balancesResponse.isPositive()) {
            Object o = balancesResponse.getResponseObject();
            try {
                Amount a = (Amount) o;
                return a.getQuantity();
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * start the UI server
     */
    public static void startUIserver(NuBotOptions opt) {

        //TODO: only load if in testmode and this is not set elsewhere
        Utils.loadProperties("settings.properties");

        //binds GET and POST
        new ConfigController("/config", opt, configdir, configFile);

        new LogController("/logdump");

        Map opmap = new HashMap();
        opmap.put("exchange", opt.getExchangeName());

        opmap.put("btc_balance", getBalance(CurrencyList.BTC));
        opmap.put("nbt_balance", getBalance(CurrencyList.NBT));

        get("/", (request, response) -> new ModelAndView(opmap, htmlFolder + "operation.mustache"), new LayoutTemplateEngine(htmlFolder));

        Map feedsmap = new HashMap();
        get("/feeds", (request, response) -> new ModelAndView(feedsmap, htmlFolder + "feeds.mustache"), new LayoutTemplateEngine(htmlFolder));


        Map configmap = new HashMap();
        configmap.put("configfile", configFile);


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

        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(configpath);
            Global.options = opt;

            //TODO: use global object and update
            ti = ExchangeFacade.exchangeInterfaceSetup(Global.options);

            startUIserver(opt);

        } catch (Exception ex) {
            LOG.error("error configuring " + ex);
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