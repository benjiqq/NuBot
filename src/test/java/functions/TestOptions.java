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
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsDefault;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.FilesystemUtils;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestOptions extends TestCase {

    private static String testconfigFile = "peatio.json";
    private static String testconfig = Settings.TESTS_CONFIG_PATH + "/" + testconfigFile;

    @Override
    public void setUp() {

    }

    @Test
    public void testConfigExists() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        final String wdir = FilesystemUtils.getBotAbsolutePath();

        File f = new File(testconfig);
        assertTrue(f.exists());
    }

    @Test
    public void testLoadconfig() {
        try {
            JSONObject inputJSON = ParseOptions.parseSingleJsonFile(testconfig);
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
                    .parseOptionsSingle(testconfig, false);

            assertTrue(nuo != null);

            assertTrue(nuo.exchangeName.equals("peatio"));

            //assertTrue(nuo.getPair() != null);

            //assertTrue(nuo.getSecondaryPegOptions() != null);
            //.getSpread())

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadComplete() {

        String testconfig = Settings.TESTS_CONFIG_PATH + "/" + testconfigFile;
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
    public void testLoadOptions() {

        String testconfig = Settings.TESTS_CONFIG_PATH + "/" + testconfigFile;

        boolean catched = false;
        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig, false);

            assertTrue(opt.isSubmitliquidity() == false);

        } catch (NuBotConfigException e) {
            System.out.println("could not parse config");
            System.out.println(e);
            catched = true;
        }

        assertTrue(!catched);


    }

    @Test
    public void testLoadOptionsAll() {

        String testconfig = Settings.TESTS_CONFIG_PATH + "/" + testconfigFile;

        boolean catched = false;
        NuBotOptions opt = null;
        try {
            opt = ParseOptions.parseOptionsSingle(testconfig, false);

        } catch (NuBotConfigException e) {
            System.out.println("error " + e);
            catched = true;
        }

        assertTrue(!catched);

        assertTrue(opt.getExchangeName().equals("peatio"));
        assertTrue(opt.getApiKey().equals("testapikey"));
        assertTrue(opt.getApiSecret().equals("testapisecret"));
        assertTrue(opt.getTxFee() == 0.2);
        assertTrue(opt.getPair().getPaymentCurrency().equals(CurrencyList.BTC));
        assertTrue(opt.getPair().getOrderCurrency().equals(CurrencyList.NBT));
        assertTrue(opt.nubitAddress.equals("xxx"));
        assertTrue(opt.nudIp.equals("127.0.0.1"));
        assertTrue(opt.nudPort == 9091);
        assertTrue(opt.rpcPass.equals("xxx"));
        assertTrue(opt.rpcUser.equals("xxx"));
        assertTrue(opt.mainFeed.equals("btce"));

        assertTrue(opt.backupFeeds.get(0).equals("coinbase"));
        assertTrue(opt.backupFeeds.get(1).equals("blockchain"));
        assertTrue(opt.wallchangeThreshold == 0.1);
        assertTrue(opt.getSendMailsLevel().equals(MailNotifications.MAIL_LEVEL_ALL));
        assertTrue(opt.sendMails() == true);
        assertTrue(opt.isDualSide() == true);
        assertTrue(opt.isSubmitliquidity() == false);
        assertTrue(opt.isMultipleCustodians() == false);
        assertTrue(opt.isExecuteOrders() == false);
        assertTrue(opt.isVerbose() == false);
        assertTrue(opt.isHipchat() == true);
        assertTrue(opt.emergencyTimeout == 60);
        assertTrue(opt.keepProceeds == 0);
        assertTrue(opt.maxBuyVolume == 0.0);
        assertTrue(opt.maxSellVolume == 0.0);
        assertTrue(opt.priceIncrement == 0.1);
        assertTrue(opt.spread == 0.0);
        assertTrue(opt.wallchangeThreshold == 0.1);

        /*String nudIp = NuBotOptionsDefault.nudIp;
        String mailnotifications = NuBotOptionsDefault.mailnotifications;
        boolean submitLiquidity = NuBotOptionsDefault.submitLiquidity;
        boolean executeOrders = NuBotOptionsDefault.executeOrders;
        boolean verbose = NuBotOptionsDefault.verbose;
        boolean hipchat = NuBotOptionsDefault.hipchat;
        boolean multipleCustodians = NuBotOptionsDefault.multipleCustodians;
        int executeStrategyInterval = NuBotOptionsDefault.executeStrategyInterval;
        double txFee = NuBotOptionsDefault.txFee;
        double priceIncrement = NuBotOptionsDefault.priceIncrement;
        double keepProceeds = NuBotOptionsDefault.keepProceeds;
        double maxSellVolume = NuBotOptionsDefault.maxSellVolume;
        double maxBuyVolume = NuBotOptionsDefault.maxBuyVolume;
        int emergencyTimeout = NuBotOptionsDefault.emergencyTimeout;
        boolean distributeLiquidity = NuBotOptionsDefault.distributeLiquidity;*/

        assertTrue(opt.getPair() != null);
        //assertTrue(opt.getPair().equals("nbt_btc"));


    }

    @Test
    public void testParseFeeds() {

        String testconfig = Settings.TESTS_CONFIG_PATH + "/" + testconfigFile;
        boolean catched = false;
        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig, false);
            assertTrue(opt.mainFeed != null);
            assertTrue(opt.mainFeed.equals("btce"));
            assertTrue(opt.backupFeeds.size() == 2);
            assertTrue(opt.backupFeeds.get(0).equals("coinbase"));
            assertTrue(opt.backupFeeds.get(1).equals("blockchain"));


        } catch (NuBotConfigException e) {
            catched = true;
            System.out.println("error parsing " + e);
        }

        assertTrue(!catched);


    }

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
        try {
            opt = ParseOptions.parseOptionsFromJson(optionJson, false);
        } catch (Exception e) {

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
        assertTrue(opt.getSendMailsLevel().equals("ALL"));
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
            newopt = ParseOptions.parseOptionsSingle(configFile, false);
        } catch (Exception e) {

        }

        assertTrue(newopt.getExchangeName().equals(ExchangeFacade.POLONIEX));

    }

}