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

package com.nubits.nubot.testsmanual;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.launch.UILaunch;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * the test launcher class with UI
 */
public class TestLaunchWithUI {


    static String configfile = "config/myconfig/bitspark.json";

    private static final Logger LOG = LoggerFactory.getLogger(TestLaunchWithUI.class.getName());

    private static boolean runui = true;


    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        //TestWrappers.runTests();

        Global.sessionStarted = System.currentTimeMillis();


        LOG.info("main. with args " + args.length);

        mainLaunch(configfile, runui);

    }

    /**
     * main launch of a bot
     *
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
            if (opt.requiresSecondaryPegStrategy()) {
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

