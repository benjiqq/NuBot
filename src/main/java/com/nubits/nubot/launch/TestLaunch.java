package com.nubits.nubot.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a utility for launch a bot based on predefined config
 */
public class TestLaunch {


    private static final Logger LOG = LoggerFactory.getLogger(TestLaunch.class.getName());

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        /*NuBotOptions opt = null;

        //Load settings
        try {
            Utils.loadProperties("settings.properties");
        } catch (IOException e) {
            LOG.error("could not load settings");
            System.exit(0);
        }
        LOG.info("settings loaded");


        try {
            opt = ParseOptions.parseOptionsSingle("testconfig/poloniex.json");
        } catch (NuBotConfigException e) {
            exitWithNotice("" + e);
        }

        executeBot(opt);*/

        String configfile = "config/testconfig/poloniex_sec.json";
        boolean runui = true;
        MainLaunch.mainLaunch(configfile, runui);
    }





}
