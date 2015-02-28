package com.nubits.nubot.tests;


import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestOptions extends TestCase {

    private static String testconfigFile = "test.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    @Override
    public void setUp() {
        Utils.loadProperties("settings.properties");
    }

    @Test
    public void testConfigExists() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        final String wdir = System.getProperty("user.dir");
        System.out.println("wdir: " + wdir);

        File f = new File(testconfig);
        System.out.println(">>> " + f.getAbsolutePath() + " exists : " + f.exists());
        assertTrue(f.exists());
    }

    @Test
    public void testLoadconfig() {
        try {
            JSONObject inputJSON = ParseOptions.parseSingleJsonFile(testconfig);
            System.out.println(inputJSON);
            assertTrue(inputJSON.keySet().size() > 0);
        } catch (ParseException e) {
            // e.printStackTrace();
        }
    }

    @Test
    public void testJson() {
        try {
            JSONObject inputJSON = ParseOptions.parseSingleJsonFile(testconfig);

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
                    .parseSingleJsonFile(testconfig);

            assertTrue((boolean) j.get("verbose") == false);
            assertTrue(((int) new Long((Long) j.get("emergencytimeout"))
                    .intValue()) == 60);

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadConfig() {
        try {
            NuBotOptions nuo = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(nuo != null);

            assertTrue(nuo.getExchangeName().equals("peatio"));

            //assertTrue(nuo.getPair() != null);

            //assertTrue(nuo.getSecondaryPegOptions() != null);
            //.getSpread())

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadComplete() {

        String testconfigFile = "test.json";
        String testconfig = "testconfig/" + testconfigFile;
        boolean catched = false;

        try {
            JSONObject inputJSON = ParseOptions.parseSingleJsonFile(testconfig);
            //assertTrue(inputJSON.containsKey("options"));
            //JSONObject optionsJSON = ParseOptions.getOptionsKey(inputJSON);
            assertTrue(inputJSON.containsKey("exchangename"));
        } catch (Exception e) {

        }
        assertTrue(!catched);
    }

    @Test
    public void testLoadOptions(){

        String testconfigFile = "test.json";
        String testconfig = "testconfig/" + testconfigFile;
        boolean catched = false;
        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig);

            assertTrue(opt.getNubitAddress().equals("xxx"));

        } catch (NuBotConfigException e) {
            System.out.println("could not parse config");
            System.out.println(e);
            catched = true;
        }

        assertTrue(!catched);


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