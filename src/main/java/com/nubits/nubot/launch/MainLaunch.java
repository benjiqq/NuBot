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
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.global.Settings;

import com.nubits.nubot.webui.UiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
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

    private static boolean runui = false;

    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json> [runui]";


    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        Global.sessionPath = "logs" + "/" + Settings.SESSION_LOG + System.currentTimeMillis();
        MDC.put("session", Global.sessionPath);
        LOG.info("defined session path " + Global.sessionPath);

        //MDC.put("session", Settings.GLOBAL_SESSION_NAME);

        if (args.length == 2) {
            LOG.info("args0 " + args[0]);
            LOG.info("args1 " + args[1]);

            try {
                runui = args[1].equals("runui");
            } catch (Exception e) {
                exitWithNotice("can't parse runui flag: run nubot with \n" + USAGE_STRING);
            }
        }

        String configFile = args[0];

        if (runui) {
            LOG.info("* run with ui *");
            String workingdir = ".";
            try{
                UiServer.startUIserver(workingdir, configFile);
            }catch(Exception e){
                LOG.error("error setting up UI server " + e);
            }

        } else {
            LOG.info("** run command line **");
            SessionManager.sessionLaunch(configFile);
        }





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

