package com.nubits.nubot.utils;

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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Date;

public class DateUtils {

    private static ILoggerFactory lfac = LoggerFactory.getILoggerFactory();
    private static LoggerContext lctx = (LoggerContext) lfac;
    private static Logger log = lctx.getLogger(DateUtils.class);
    private static String NAME_OF_CURRENT_APPLICATION = "NuBot";
    private static String customxml = "./config/custom.xml";

    public static void main(String[] args) {

        JoranConfigurator jc = new JoranConfigurator();
        jc.setContext(lctx);
        lctx.reset(); // override default configuration
        // inject the name of the current application as "application-name"
        // property of the LoggerContext
        lctx.putProperty("application-name", NAME_OF_CURRENT_APPLICATION);
        try {
            jc.doConfigure(customxml);
        } catch (Exception e) {

        }

        log.info("class log");


        LoggerContext loggerContext = ((ch.qos.logback.classic.Logger) log).getLoggerContext();
        URL mainURL = ConfigurationWatchListUtil.getMainWatchURL(loggerContext);
        log.info("url:  " + mainURL);
        log.info("Logback used '{}' as the configuration file.", mainURL);
		log.debug("[MAIN] Current Date : {}", getCurrentDate());

    }

    private static Date getCurrentDate() {

        return new Date();

    }

}
