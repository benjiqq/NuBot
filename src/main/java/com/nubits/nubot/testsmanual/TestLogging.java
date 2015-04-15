package com.nubits.nubot.testsmanual;

import com.nubits.nubot.global.Settings;
import com.nubits.nubot.utils.InitTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Test class for the manual logging
 */
public class TestLogging {


    static {
        //define Logging by using predefined Settings which points to an XML
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestWrappers.class.getName());

    public static void main(String[] args) {

        InitTests.setLoggingFilename(LOG);
        LOG.info("abcd" + LOG.getName());
    }

}
