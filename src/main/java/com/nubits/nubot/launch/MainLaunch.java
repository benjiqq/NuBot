package com.nubits.nubot.launch;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;


/**
 * the main launcher class. either start bot through commandline
 * or a GUI is launched where user starts the Bot himself
 */
public class MainLaunch {


    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static boolean runui = false;

    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json> [runui]";

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {
        LOG.info("main. with args " + args.length);
        if (args.length > 2 || args.length == 0) {
            exitWithNotice("wrong argument number : run nubot with \n" + USAGE_STRING);
        }
        String configfile = args[0];

        if (args.length == 2) {
            LOG.info("args0 " + args[0]);
            LOG.info("args1 " + args[1]);

            try {
                runui = args[1].equals("runui");
            } catch (Exception e) {
                exitWithNotice("can't parse runui flag: run nubot with \n" + USAGE_STRING);
            }
        }

        try {
            Utils.loadProperties("settings.properties");
        } catch (IOException e) {
            exitWithNotice("could not load settings");
        }
        LOG.info("settings loaded");

        mainLaunch(configfile, runui);

    }

    /**
     * main launch of a bot
     * @param configfile
     * @param runui
     */
    public static void mainLaunch(String configfile, boolean runui) {
        LOG.info("main launch. with configfile " + configfile + " " + runui);

        NuBotOptions nuopt = null;

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            nuopt = ParseOptions.parseOptionsSingle(configfile);
        } catch (NuBotConfigException e) {
            exitWithNotice("" + e);
        }

        CurrencyList.init();

        LOG.info("-- new main launched --");
        if (runui) {
            LOG.info("* run with ui *");
            String workingdir = ".";
            UILaunch.UIlauncher(workingdir, configfile);
        } else {
            LOG.info("** run command line **");
            executeBot(nuopt);
        }
    }


    /**
     * exit application and notify user
     *
     * @param msg
     */
    private static void exitWithNotice(String msg) {
        LOG.error(msg);
        System.exit(0);
    }


    /**
     * execute a NuBot based on valid options. Also make sure only one NuBot is running
     *
     * @param opt
     */
    public static void executeBot(NuBotOptions opt) {

        Global.mainThread = Thread.currentThread();

        Global.createShutDownHook();

        //exit if already running or show info to user
        if (Global.running) {
            exitWithNotice("NuBot is already running. Make sure to terminate other instances.");
        } else {
            if (opt.secondarypeg) {
                LOG.info("creating secondary bot");
                NuBotSecondary bot = new NuBotSecondary();
                bot.execute(opt);
            } else {
                LOG.info("creating simple bot");
                NuBotSimple bot = new NuBotSimple();
                bot.execute(opt);
            }
        }

    }


}

