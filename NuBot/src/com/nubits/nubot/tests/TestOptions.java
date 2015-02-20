package com.nubits.nubot.tests;


import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestOptions extends TestCase {

    private static String testconfig = "testconfig/test.config";

    @Test
    public void testLoadconfig() {
        // Utils.loadProperties("settings.properties");
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        final String wdir = System.getProperty("user.dir");
        // System.out.println("working dir : " + wdir);
        // System.out.println("Current relative path is: " + s);
        // String p = System.getProperty("java.class.path");
        // System.out.println(p);

        File f = new File(testconfig);
        assertTrue(f.getAbsolutePath().contains("test.config"));
        assertTrue(f.exists());

    }

    @Test
    public void testJson() {
        try {
            JSONObject inputJSON = ParseOptions.parseSingleFileToJson(testconfig);

            assertTrue(inputJSON.containsKey("exchangename"));
        } catch (ParseException e) {
            // e.printStackTrace();
        }
    }

    @Test
    public void testLoadConfigJson() {
        // used file
        // {"options":
        // {
        // "dualside": true,
        // "multiple-custodians":false,
        // "submit-liquidity":true,
        // "executeorders":false,
        // "verbose":false,
        // "hipchat":true,
        // "mail-notifications":false,
        // "mail-recipient":"xxx@xxx.xxx",
        // "emergency-timeout":60,
        // "keep-proceeds":0,
        // "max-sell-order-volume" : 0,
        // "max-buy-order-volume" : 0,
        // "priceincrement": 0.1,
        // }
        // }

        try {
            // System.out.println(System.getProperty("));
            // Global.options =
            JSONObject j = ParseOptions
                    .parseSingleFileToJson("testconfig/test.config");

            assertTrue((boolean) j.get("verbose") == false);
            assertTrue(((int) new Long((Long) j.get("emergency-timeout"))
                    .intValue()) == 60);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadConfig() {
        try {
            NuBotOptions nuo = ParseOptions
                    .parseOptionsSingle("testconfig/test.config");
            assertTrue(nuo != null);

            assertTrue(nuo.getExchangeName().equals("example"));

            assertTrue(nuo.getPair() != null);

            assertTrue(nuo.getSecondaryPegOptions() != null);
            //.getSpread())

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }
    }

    // @Test
    // public void testDefaultOptions() {
    // // TODO
    // assert (false);
    // }
    //
    // @Test
    // public void testcompulsory() {
    // assert (false);
    // }
    //
    // @Test
    // public void testWrongConfig() {
    // // TODO
    // // if wrongly configured throws ParseError
    // assert (false);
    // }
}