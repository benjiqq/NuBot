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

import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsDefault;
import com.nubits.nubot.options.ParseOptions;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

public class TestOptionsJSON extends TestCase {


    @Test
    public void testToJson() {

        NuBotOptions opt = NuBotOptionsDefault.defaultFactory();
        assertTrue(opt != null);

        String jsonString = NuBotOptions.optionsToJson(opt);

        assertTrue(jsonString.length() > 0);

        JSONParser jsonparser = new JSONParser();
        JSONObject optionJson = null;
        try {
            optionJson = (JSONObject) (jsonparser.parse(jsonString));
        } catch (Exception e) {

        }

        System.out.println(optionJson);

        for (int i = 0; i < ParseOptions.allkeys.length; i++) {
            String f = ParseOptions.allkeys[i];
            System.out.println(f);
            //assertTrue(optionJson.containsKey(f));
            assertTrue(ParseOptions.containsIgnoreCase(optionJson, f));
        }


    }

    @Test
    public void testRoundTrip() {


        String jsonString = "{\n" +
                "  \"exchangename\":\"Poloniex\",\n" +
                "  \"apiKey\": \"def\",\n" +
                "  \"apiSecret\": \"abc\",\n" +
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

        JSONParser jsonparser = new JSONParser();
        JSONObject optionJson = null;
        try {
            optionJson = (JSONObject) (jsonparser.parse(jsonString));
        } catch (Exception e) {

        }
        NuBotOptions opt = null;
        try{
            opt = ParseOptions.parseOptionsFromJson(optionJson);
        }catch(Exception e){

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
        assertTrue(opt.isHipchat() == true);
        assertTrue(opt.sendMailsLevel().equals("ALL"));
        assertTrue(opt.getMailRecipient().equals("test@gmail.com"));
        assertTrue(opt.getEmergencyTimeout() == 60);
        assertTrue(opt.getMaxBuyVolume() == 10.0);
        assertTrue(opt.getMaxSellVolume() == 10.0);
        assertTrue(opt.getPriceIncrement() == 0.1);
        assertTrue(opt.getSpread() == 0.0);
        assertTrue(opt.getWallchangeThreshold() == 0.1);
        assertTrue(opt.getBackupFeeds().get(0).equals("coinbase"));
        assertTrue(opt.getBackupFeeds().get(1).equals("btce"));


    }

    @Test
    public void testFromFile() {

        String configFile = "config/myconfig/poloniex.json";

        NuBotOptions newopt = null;
        try {
            newopt = ParseOptions.parseOptionsSingle(configFile);
        } catch (Exception e) {

        }

        assertTrue(newopt.getExchangeName().equals(ExchangeFacade.POLONIEX));
    }
}
