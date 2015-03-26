/*
 * Copyright (C) 2015 Nu Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.nubits.nubot.launch;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;


/**
 * the main launcher class. either start bot through commandline
 * or a GUI is launched where user starts the Bot himself
 */
public class MainLaunch {

    static {

        System.out.println("static block");
        File f = new File(Settings.LOGXML);
        System.out.println("f: " + f.getAbsolutePath());
        System.out.println("f: " + f.exists());

        System.setProperty("logback.configurationFile", f.getAbsolutePath());

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.doConfigure(f.getAbsoluteFile()); // loads logback file
        } catch (JoranException je) {
            // StatusPrinter will handle this
        } catch (Exception ex) {

            ex.printStackTrace(); // Just in case, so we see a stacktrace

        }
    }


    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    /**
     * Logger for session data. called only once per session
     */
    private static final Logger sessionLOG = LoggerFactory.getLogger("SessionLOG");

    private static boolean runui = false;

    //private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json> [runui]";
    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json>";

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        if (args.length != 1) {
            exitWithNotice("wrong argument number : run nubot with \n" + USAGE_STRING);
        }

        String configfile = args[0];

        mainLaunch(configfile, false);

    }


    /**
     * main launch of a bot
     *
     * @param configfile
     * @param runui
     */
    public static void mainLaunch(String configfile, boolean runui) {

        LOG.debug("main launch. with configfile " + configfile + " " + " runui " + runui);

        logSetup();

        NuBotOptions nuopt = null;

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            nuopt = ParseOptions.parseOptionsSingle(configfile);
        } catch (NuBotConfigException e) {
            exitWithNotice("" + e);
        }

        LOG.debug("-- new main launched --");

        LOG.debug("** run command line **");
        executeBot(nuopt);

    }

    private static void logSetup() {

        //log info
        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) LOG).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);

        LOG.info("Logback used '{}' as the configuration file.", mainURL);

        List<ch.qos.logback.classic.Logger> llist = loggerContext.getLoggerList();

        Iterator<ch.qos.logback.classic.Logger> it = llist.iterator();
        while (it.hasNext()) {
            ch.qos.logback.classic.Logger l = it.next();
            LOG.debug("" + l);
        }


        //set up session dir
        String wdir = System.getProperty("user.dir");

        File f = new File(wdir + "/" + Settings.LOGS_PATH + Settings.CURRENT_LOGS_FOLDER); // current directory

        if (!f.exists()) {
            f.mkdir();
            Global.sessionLogFolders = f.getAbsolutePath();
            Global.sessionStarted = System.currentTimeMillis();
            sessionLOG.debug("session start;" + Global.sessionLogFolders + ";" + Global.sessionStarted);
        } else {
            //should have been moved
        }

        /*File[] files = f.listFiles();
        String currentLogfoldername = "";
        for (File file : files) {
            if (file.isDirectory()) {
                currentLogfoldername = file.getName();
                LOG.debug("directory:" + currentLogfoldername);
                Global.sessionLogFolders = Settings.LOGS_PATH+Settings.CURRENT_LOGS_FOLDER + currentLogfoldername;
                LOG.debug("set session log folder: " + Global.sessionLogFolders);
            }
        }*/
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
            if (opt.requiresSecondaryPegStrategy()) {
                LOG.debug("creating secondary bot object");
                NuBotSecondary bot = new NuBotSecondary();
                bot.execute(opt);
            } else {
                LOG.debug("creating simple bot object");
                NuBotSimple bot = new NuBotSimple();
                bot.execute(opt);
            }
        }

    }


}

