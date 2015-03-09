package com.nubits.nubot.utils;

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
