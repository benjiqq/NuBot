package com.nubits.nubot.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.SaveOptions;
import junit.framework.TestCase;
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
        String jsonOpt =gson.toJson(opt);
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
        File newbak = new File("testconfig/testconfig.bak");
        assertTrue(newbak.exists());
    }


}