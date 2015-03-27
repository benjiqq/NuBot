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

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;


/**
 * the main launcher class. either start bot through commandline
 * or a GUI is launched where user starts the Bot himself
 */
public class MainLaunch {

    static {
        System.setProperty("logback.configurationFile", Settings.LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static boolean runui = false;

    private static File sessionFile;

    private static String appFolder;

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

        sessionLaunch(configfile, false);

    }

    /**
     * main launch of a bot
     *
     * @param configfile
     * @param runui
     */
    public static void sessionLaunch(String configfile, boolean runui) {

        NuBotOptions nuopt = null;

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            nuopt = ParseOptions.parseOptionsSingle(configfile);
        } catch (NuBotConfigException e) {
            MainLaunch.exitWithNotice("" + e);
        }

        launchBot(nuopt);

    }


    /**
     * execute a NuBot based on valid options
     *
     * @param opt
     */
    public static void launchBot(NuBotOptions opt) {

        Global.mainThread = Thread.currentThread();

        Global.createShutDownHook();

        Global.sessionLogFolders = "session_" + System.currentTimeMillis();

        MDC.put("session", Global.sessionLogFolders);

        //LOG.debug("session launch called from " + caller);
        //LOG.debug("launch bot session. with configfile " + configfile + " " + " runui " + runui);


        //TODO!
        // check if other bots are running
        boolean otherSessions = isSessionActive();

        if (otherSessions) {
            LOG.info("NuBot is already running");
            //handle different cases
        }
        else {
            createSessionFile();
        }

        //execute bot

        if (opt.requiresSecondaryPegStrategy()) {
            LOG.debug("creating secondary bot object");
            Global.bot = new NuBotSecondary();
            Global.bot.execute(opt);
        } else {
            LOG.debug("creating simple bot object");
            Global.bot = new NuBotSimple();
            Global.bot.execute(opt);


        }

    }


    /**
     * check whether other sessions are active via a temp file
     *
     * @return
     */
    public static boolean isSessionActive() {

        appFolder = System.getProperty("user.home") + "/" + Settings.APP_FOLDER;

        sessionFile = new File
                (appFolder, Settings.APP_NAME + Settings.SESSION_FILE);
        LOG.info("checking " + sessionFile.getAbsolutePath() + " " + sessionFile.exists());
        return sessionFile.exists();
    }

    public static void createSessionFile() {
        try {
            File appdir = new File(System.getProperty("user.home") + "/" + Settings.APP_FOLDER);
            if (!appdir.exists())
                appdir.mkdir();

            sessionFile = new File
                    (appFolder, Settings.APP_NAME + Settings.SESSION_FILE);
            sessionFile.createNewFile();
        } catch (Exception e) {

        }

        //delete the file on exit
        sessionFile.deleteOnExit();
    }


    /**
     * exit application and notify user
     *
     * @param msg
     */
    public static void exitWithNotice(String msg) {
        LOG.error(msg);
        System.exit(0);
    }


}

