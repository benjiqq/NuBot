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
import com.nubits.nubot.options.*;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
                "maxsellvolume",
                "maxbuyvolume",
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

        for (int i = 0; i < fields.length; i++) {
            String f = fields[i];
            System.out.println(f);
            assertTrue(optionJson.containsKey(f));
        }

        Object o = optionJson.get("backupfeeds");
        String s = "" + o;
        assertTrue(s.contains("btce"));
        assertTrue(s.contains("blockchain"));
        assertTrue(s.contains("coinmarketcap_no"));

    }

    @Test
    public void testRoundTrip() {

        String configString = "{\n" +
                "  \"exchangename\":\"Poloniex\",\n" +
                "  \"apikey\": \"def\",\n" +
                "  \"apisecret\": \"abc\",\n" +
                "  \"executeorders\":true,\n" +
                "  \"txfee\": 0.0,\n" +
                "  \"pair\":\"nbt_btc\",\n" +
                "  \"submitliquidity\":false,\n" +
                "  \"nubitaddress\": \"xxx\",\n" +
                "  \"nudip\": \"127.0.0.1\",\n" +
                "  \"nudport\": 9091,\n" +
                "  \"rpcpass\": \"xxx\",\n" +
                "  \"rpcuser\": \"xxx\",\n" +
                "  \"mainfeed\":\"blockchain\",\n" +
                "  \"backupfeeds\": [\"coinbase\", \"btce\"],\n" +
                "  \"wallchangeThreshold\": 0.1,\n" +
                "  \"dualside\": true,\n" +
                "  \"multiplecustodians\":false,\n" +
                "  \"verbose\":true,\n" +
                "  \"hipchat\":true,\n" +
                "  \"mailnotifications\":\"ALL\",\n" +
                "  \"mailrecipient\":\"test@gmail.com\",\n" +
                "  \"emergencytimeout\":60,\n" +
                "  \"keepproceeds\":0,\n" +
                "  \"maxsellvolume\" : 10.0,\n" +
                "  \"maxbuyvolume\" : 10.0,\n" +
                "  \"priceincrement\": 0.1,\n" +
                "  \"spread\":0.0,\n" +
                "  \"wallchangeThreshold\": 0.1\n" +
                "}\n";

        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) (parser.parse(configString));
        } catch (Exception e) {

        }

        NuBotOptions opt = null;
        Map opmap = new HashMap();
        try {
            opt = ParseOptions.parseOptionsFromJson(json);
        } catch (Exception e) {
            //handle errors

        }

        assertTrue(opt.getExchangeName().equals("Poloniex"));
        assertTrue(opt.getApiKey().equals("def"));
        assertTrue(opt.getApiSecret().equals("abc"));
        assertTrue(opt.isExecuteOrders() == true);
        assertTrue(opt.getTxFee() == 0.0);
        assertTrue(opt.isSubmitliquidity() == false);
        assertTrue(opt.nubitAddress.equals("xxx"));
        assertTrue(opt.rpcUser.equals("xxx"));
        assertTrue(opt.mainFeed.equals("blockchain"));
        assertTrue(opt.isDualSide() == true);
        assertTrue(opt.isVerbose() == true);
        assertTrue(opt.isSendHipchat() == true);
        assertTrue(opt.sendMailsLevel().equals("ALL"));
        assertTrue(opt.getMailRecipient().equals("test@gmail.com"));
        assertTrue(opt.getEmergencyTimeout() == 60);
        assertTrue(opt.getMaxBuyVolume() == 10.0);
        assertTrue(opt.getMaxSellVolume() == 10.0);
        assertTrue(opt.getPriceIncrement()==0.1);
        assertTrue(opt.getSpread()==0.0);
        assertTrue(opt.getWallchangeThreshold()==0.1);
        assertTrue(opt.getBackupFeedNames().get(0).equals("coinbase"));
        assertTrue(opt.getBackupFeedNames().get(1).equals("btce"));


        String jsonString = SerializeOptions.optionsToJson(opt);

        NuBotOptions reopt = null;

        boolean ncatch = true;
        try {
            reopt = ParseOptions.parseOptionsFromJson(json);
        } catch (Exception e) {
            //handle errors
            ncatch = false;
        }

        assertTrue(ncatch);

        // String arrayListToJson = gson.toJson(navigation);

//        logger.info(arrayListToJson);
//
//        assertEquals(
//                "[{\"key\":\"examples\",\"url\":\"http://leveluplunch.com/java/examples\"},"
//                        + "{\"key\":\"exercises\",\"url\":\"http://leveluplunch.com/java/exercises\"}]",
//                arrayListToJson);
    }
}
