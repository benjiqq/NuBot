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
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.launch.MainLaunch;
import com.nubits.nubot.utils.VersionInfo;
import com.nubits.nubot.webui.UiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * the test launcher class for all functions
 */
public class TestLaunch {

    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    static String configFile = "config/poloniex.json";
    //static String configFile = "config/myconfig/bitspark.json";

    private static final Logger LOG = LoggerFactory.getLogger(TestLaunch.class.getName());

    private static final Logger sessionLOG = LoggerFactory.getLogger(Settings.SESSION_LOGGER_NAME);

    private static boolean runui = true;


    public static void testlaunchWithFile() {

        LOG.info("test");

        Global.sessionPath = "testlaunch" + "/" + Settings.SESSION_LOG + System.currentTimeMillis();
        MDC.put("session", Global.sessionPath);

        LOG.info("defined session path " + Global.sessionPath);

        LOG.info("commit info " + VersionInfo.getBranchCommitInfo());

        sessionLOG.debug("test launch");

        LOG.info("set global config");
        SessionManager.setConfigGlobal(configFile, false);


        if (runui) {

            try {
                UiServer.startUIserver(configFile);
            } catch (Exception e) {
                LOG.error("error setting up UI server " + e);
            }
        }
    }

    public static void testlaunchNoFile() {

        LOG.info("test");

        Global.sessionPath = "testlaunch" + "/" + Settings.SESSION_LOG + System.currentTimeMillis();
        MDC.put("session", Global.sessionPath);
        LOG.info("defined session path " + Global.sessionPath);
        LOG.info("commit info " + VersionInfo.getBranchCommitInfo());

        sessionLOG.debug("test launch");

        LOG.info("set global config");
        SessionManager.setConfigDefault();

        if (runui) {

            try {
                UiServer.startUIserver(Settings.DEFAULT_CONFIG_FILE_PATH);
            } catch (Exception e) {
                LOG.error("error setting up UI server " + e);
            }
        }
    }


    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        //testlaunchWithFile();
        //testlaunchNoFile();
        String[] args2 = {"-cfg=" + configFile, "-GUI"};
        MainLaunch.main(args2);

    }


}

