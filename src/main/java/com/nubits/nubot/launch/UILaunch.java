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

    private static void printlogall() {
        LOG.trace("test log trace");
        LOG.debug("test log debug");
        LOG.info("test log info");
        LOG.warn("test log warn ");
        LOG.error("test log error");
    }

    public static void UIlauncher(String configdir, String configFile) {

        LOG.info("starting UI server");


        printlogall();

        NuBotOptions opt = null;

        try {
            String configpath = configdir + "/" + configFile;

            opt = ParseOptions.parseOptionsSingle(configpath);
            Global.options = opt;

        } catch (Exception ex) {
            LOG.error("error configuring UI server " + ex);
        }

        try{
            UiServer.startUIserver(configdir, configFile);
        }catch(Exception e){
            LOG.error("error setting up UI server " + e);
        }
    }


}
