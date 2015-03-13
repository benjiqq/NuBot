package com.nubits.nubot.launch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.webui.UiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UILaunch {

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static String configFile = "poloniex.json";
    private static String configdir = "testconfig";


    public static String opttoJson(NuBotOptions opt) {
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);
        LOG.info("using options " + js);
        return js;
    }


    private static void printlogall(){
        LOG.trace("test log trace");
        LOG.debug("test log debug");
        LOG.info("test log info");
        LOG.warn("test log warn ");
        LOG.error("test log error");
    }

    public static void main(String[] args) {

        LOG.info("starting UI server");

        printlogall();

        try {
            String configpath = configdir + "/" + configFile;

            NuBotOptions opt = ParseOptions.parseOptionsSingle(configpath);
            Global.options = opt;

            UiServer.startUIserver(opt, configdir, configFile);

        } catch (Exception ex) {
            LOG.error("error configuring " + ex);
        }


    }
}
