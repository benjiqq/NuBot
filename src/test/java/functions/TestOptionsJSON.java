/*
 * Copyright (C) 2015 Nu Development Team
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
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsDefault;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import com.nubits.nubot.utils.FileSystem;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

public class TestOptionsJSON extends TestCase {

    @Test
    public void testToJson() {
        NuBotOptions opt = NuBotOptionsDefault.defaultFactory();
        assertTrue(opt != null);
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        assertTrue(parser != null);

        String jsonString = "";
        try {
            jsonString = parser.toJson(opt);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }

        assertTrue(jsonString.length() > 0);

        JSONParser jsonparser = new JSONParser();
        JSONObject optionJson = null;
        try {
            optionJson = (JSONObject) (jsonparser.parse(jsonString));
        } catch (Exception e) {

        }

        String[] fields = {
                "exchangename",
                "apikey",
                "apisecret",
                "txfee",
                "pair",
                "dualside",
                "multiplecustodians",
                "executeorders",
                "verbose",
                "hipchat",
                "mailnotifications",
                "mailrecipient",
                "emergencytimeout",
                "keepproceeds",
                "maxsellordervolume",
                "maxbuyordervolume",
                "priceincrement",
                "submitliquidity",
                "nubitaddress",
                "nudip",
                "nudport",
                "rpcpass",
                "rpcuser",
                "wallchangethreshold",
                "spread",
                "mainfeed",
                "backupfeeds"};

        for (int i = 0; i < fields.length; i++){
            String f = fields[i];
            System.out.println(f);
            assertTrue(optionJson.containsKey(f));
        }


    }
}
