package com.nubits.nubot.testsmanual;

import com.nubits.nubot.global.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Simple Test class for the manual logging
 */
public class TestLogging {

    //define Logging by using predefined Settings which points to an XML
    static {
        System.setProperty("logback.configurationFile", Settings.TEST_LOGXML);

    }

    private static final Logger LOG = LoggerFactory.getLogger(TestWrappers.class.getName());


    public static void main(String[] args) {
        MDC.put("testID", "TestExample");
        LOG.info("<<<<<<<<<<<<<<<<");

        MDC.put("testID", "TestABCXYZ");
        LOG.info("*****************");
    }


}
