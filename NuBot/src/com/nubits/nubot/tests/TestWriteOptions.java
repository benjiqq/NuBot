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

        SaveOptions.saveOptions(opt,testOutconfigFile);
        File f = new File(testOutconfigFile);
        assertTrue(f.exists());
    }


}