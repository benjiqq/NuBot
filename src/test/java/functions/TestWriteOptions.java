/*
 * Copyright (C) 2014-2015 Nu Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package functions;

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
import java.io.IOException;

public class TestWriteOptions extends TestCase {


    private static String testOutconfigFile = "testout.json";
    private static String testconfigdir = "config/testconfig";
    private static String testconfig = testconfigdir + "/" + testOutconfigFile;
    private static String testconfigFile = "peatio.json";
    private static String testinconfig = testconfigdir + "/" + testconfigFile;

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
        boolean success;
        File newbak;

        try {
            success = SaveOptions.backupOptions(testinconfig);
            assertTrue(success);
            newbak = new File(testconfigdir + "/" + testconfigFile + "_0.bak");
            assertTrue(newbak.exists());
        } catch (IOException e) {

        }


        try {
            //backup again. increases counter

            success = SaveOptions.backupOptions(testinconfig);
            assertTrue(success);
            newbak = new File(testconfigdir + "/" + testconfigFile + "_1.bak");
            assertTrue(newbak.exists());
        } catch (IOException e) {

        }

    }


    /**
     * create a class and then write it to file
     */
    @Test
    public void testCreateObject() {
        NuBotOptions opt = new NuBotOptions();
        opt.apiKey = "test";
        opt.setExchangeName("testexchange");
        Currency c = Currency.createCurrency("NBT");
        Currency usd = Currency.createCurrency("USD");
        opt.setPair(new CurrencyPair(c, usd));
        String testout = testconfigdir + "/"  + "test_out.json";
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