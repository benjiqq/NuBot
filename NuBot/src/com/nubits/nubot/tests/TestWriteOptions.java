package com.nubits.nubot.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.options.NuBotOptions;
import junit.framework.TestCase;
import org.junit.Test;

public class TestWriteOptions extends TestCase {

    private static String testconfigFile = "test.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    @Override
    public void setUp() {

    }


    @Test
    public void testCreate() {
        NuBotOptions opt = new NuBotOptions();

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String j =gson.toJson(opt);
        System.out.println(j);
        assertTrue(j.startsWith("{"));
        assertTrue(j.endsWith("}"));
    }


}