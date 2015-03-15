package com.nubits.nubot.testsmanual;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.launch.MainLaunch;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * a utility for launch a bot based on predefined config
 */
public class TestLaunch {

    private static Thread mainThread;
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
        boolean runui = false;
        MainLaunch.mainLaunch(configfile, runui);
    }





}
