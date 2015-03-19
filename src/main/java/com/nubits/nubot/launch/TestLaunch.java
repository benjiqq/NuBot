/*
 * Copyright (C) 2014-2015 Nu Development Team
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
