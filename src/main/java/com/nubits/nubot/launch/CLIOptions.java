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

import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.global.Settings;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse CLI arguments and provide a help menu using Apache Commons CLI
 */
public class CLIOptions {
    private static final Logger LOG = LoggerFactory.getLogger(CLIOptions.class.getName());

    private static final String GUI = "GUI";
    private static final String CFG = "cfg";

    private static final String USAGE_STRING = "java - jar NuBot -" + CFG + "=<path/to/options.json> [-" + GUI + "]";

    /**
     * Construct and provide GNU-compatible Options.
     *
     * @return Options expected from command-line of GNU form.
     */
    public Options constructGnuOptions() {
        final Options gnuOptions = new Options();

        Option UIOption = new Option(GUI, "graphic user interface", false, "Run with GUI");
        gnuOptions.addOption(UIOption);

        Option CfgFileOption = new Option(CFG, "configuration file", true, "Specify Configuration file");
        CfgFileOption.setRequired(true);
        gnuOptions.addOption(CfgFileOption);

        return gnuOptions;
    }

    /**
     * Apply Apache Commons CLI GnuParser to command-line arguments.
     *
     * @param commandLineArguments Command-line arguments to be processed with
     *                             Gnu-style parser.
     */
    public void parseCommandLineArguments(final String[] commandLineArguments, Options gnuOptions) {
        final CommandLineParser cmdLineGnuParser = new GnuParser();

        CommandLine commandLine;
        try {
            commandLine = cmdLineGnuParser.parse(gnuOptions, commandLineArguments);
            boolean runGUI = false;
            String configFile;
            if (commandLine.hasOption(GUI)) {
                runGUI = true;
                LOG.info("Running " + Settings.APP_NAME + " with GUI");
            }

            if (commandLine.hasOption(CFG)) {
                configFile = commandLine.getOptionValue(CFG);
                SessionManager.sessionLaunch(configFile, runGUI);
            } else {
                MainLaunch.exitWithNotice("Missing " + CFG + ". run nubot with \n" + USAGE_STRING);
            }


        } catch (ParseException parseException)  // checked exception
        {
            LOG.error("Encountered exception while parsing using GnuParser:\n"
                    + parseException.getMessage());
            MainLaunch.exitWithNotice("run nubot with \n" + USAGE_STRING);
        }
    }
}
