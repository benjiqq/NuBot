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

package com.nubits.nubot.utils;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.tasks.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.File;

/**
 *
 */
public class InitTests {

    static {
        System.setProperty("logback.configurationFile", "config/logging/test_logback.xml");
    }

    private static final Logger LOG = LoggerFactory.getLogger(InitTests.class.getName());


    public static void loadConfig(String path) {
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) {
            try {
                Global.options = ParseOptions.parseOptionsSingle(path, false);
            } catch (NuBotConfigException ex) {
                LOG.error(ex.toString());
            }
        } else {
            LOG.error("Config file not found in " + path);
            System.exit(0);
        }

    }

    public static void loadKeystore(boolean trustAll) {
        //Load keystore
        try {
            LOG.info("install keystore, trust all certificate : " + trustAll);
            Utils.installKeystore(trustAll);
        } catch (Exception ex) {
            LOG.error(ex.toString());
        }
    }

    public static void startConnectionCheck() {
        //Create a TaskManager
        Global.taskManager = new TaskManager();
        Global.taskManager.setTasks();

        //Start checking for connection with the exchange
        Global.taskManager.getCheckConnectionTask().start();
        //Wait a couple of seconds for the connectionThread to get live
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }
    }

    public static void setLoggingFilename(String name) {
        /*String name = log.getName();*/
        /*String fileName = fullName.substring(fullName.lastIndexOf(".") + 1) + "_"
                + Utils.getTimestampLong();*/
        //MDC.put("testFileName", fileName);
        LOG.info("Logging on " + Settings.TEST_LOGFOLDER + "/" + name);
        MDC.put("session", name);
    }
}
