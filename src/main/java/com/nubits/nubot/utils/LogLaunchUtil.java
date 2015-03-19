package com.nubits.nubot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class LogLaunchUtil {

    private static final Logger logger = LoggerFactory.getLogger(LogLaunchUtil.class);

    public static void main(String[] args) {

        logger.debug("[MAIN] Current Date : {}", getCurrentDate());
        System.out.println(getCurrentDate());
    }

    private static Date getCurrentDate() {

        return new Date();

    }

}
