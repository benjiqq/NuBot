package com.nubits.nubot.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.options.SaveOptions;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.io.File;

public class TestWriteOptions extends TestCase {


    private static String testOutconfigFile = "testout.json";
    private static String testconfig = "testconfig/" + testOutconfigFile;
    private static String testconfigFile = "test.json";
    private static String testinconfig = "testconfig/" + testconfigFile;

    @Override
    public void setUp() {

    }


    @Test
    public void testCreate() {
        NuBotOptions opt = new NuBotOptions();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String jsonOpt = gson.toJson(opt);
        //System.out.println(jsonOpt);
        assertTrue(jsonOpt.startsWith("{"));
        assertTrue(jsonOpt.endsWith("}"));
        assertTrue(jsonOpt.contains("verbose"));
        assertTrue(jsonOpt.contains("dualSide\":false"));
    }

    @Test
    public void testSave() {
        NuBotOptions opt = new NuBotOptions();

        SaveOptions.saveOptions(opt, testconfig);
        File f = new File(testconfig);
        assertTrue(f.exists());
    }

    @Test
    public void testBackup() {
        NuBotOptions opt = new NuBotOptions();
        boolean success = SaveOptions.backupOptions(testinconfig);
        assertTrue(success);
        File newbak = new File("testconfig/test.json_1.bak");
        assertTrue(newbak.exists());

        //backup again. increases counter
        success = SaveOptions.backupOptions(testinconfig);
        assertTrue(success);
        newbak = new File("testconfig/test.json_2.bak");
        assertTrue(newbak.exists());
    }


    /**
     * create a class and then write it to file
     */
    @Test
    public void testCreateObject() {
        NuBotOptions opt = new NuBotOptions();
        opt.setApiKey("test");
        opt.setExchangeName("testexchange");
        Currency c = Currency.createCurrency("NBT");
        Currency usd = Currency.createCurrency("USD");
        opt.setPair(new CurrencyPair(c, usd));
        String testout = "testconfig/test_out.json";
        SaveOptions.saveOptionsPretty(opt, testout);
        File newout = new File(testout);
        assertTrue(newout.exists());

        try {
            JSONObject inputJSON = ParseOptions.parseSingleJsonFile(testout);
            assertTrue(inputJSON.containsKey("exchangename"));
        } catch (Exception e) {
            assertTrue(false);
        }

        //assertTrue(inputJSON.containsKey("options"));
        //JSONObject optionsJSON = ParseOptions.getOptionsKey(inputJSON);

    }

    @Test
    public void testLoadChangeSave() {
        NuBotOptions opt = new NuBotOptions();

        SaveOptions.saveOptions(opt, "testconfig/" + "test1.json");
        File f = new File(testconfig);
        assertTrue(f.exists());
    }


}