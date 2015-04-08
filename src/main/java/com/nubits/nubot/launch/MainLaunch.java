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
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.webui.UiServer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;


/**
 * the main launcher class. either start bot through commandline
 * or a GUI is launched where user starts the Bot himself
 */
public class MainLaunch {

    static {
        System.setProperty("logback.configurationFile", Settings.LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static final Logger sessionLOG = LoggerFactory.getLogger(Settings.SESSION_LOGGER_NAME);


    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        Global.sessionPath = "logs" + "/" + Settings.SESSION_LOG + System.currentTimeMillis();
        MDC.put("session", Global.sessionPath);
        LOG.info("defined session path " + Global.sessionPath);

        CommandLine cli = parseArgs(args);

        boolean runGUI = false;
        String configFile;
        if (cli.hasOption(CLIOptions.GUI)) {
            runGUI = true;
            LOG.info("Running " + Settings.APP_NAME + " with GUI");
        }
        if (cli.hasOption(CLIOptions.CFG)) {
            configFile = cli.getOptionValue(CLIOptions.CFG);

            if (runGUI) {
                LOG.info("Running NuBot GUI");

                try {
                    UiServer.startUIserver(configFile);
                } catch (Exception e) {
                    LOG.error("error setting up UI server " + e);
                }

            } else {
                LOG.info("Run NuBot from CLI");
                //set global config
                SessionManager.setConfigGlobal(configFile);
                sessionLOG.debug("launch bot");
                SessionManager.launchBot(Global.options);
            }


        } else {
            exitWithNotice("Missing " + CLIOptions.CFG + ". run nubot with \n" + CLIOptions.USAGE_STRING);
        }

    }

    public static CommandLine parseArgs(String args[]) {
        CLIOptions argsParser = new CLIOptions();
        Options options = argsParser.constructGnuOptions();
        return argsParser.parseCommandLineArguments(args, options);
    }


    /**
     * exit application and notify user
     *
     * @param msg
     */
    public static void exitWithNotice(String msg) {
        sessionLOG.error(msg);
        System.exit(0);
    }


}

