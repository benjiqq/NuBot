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


import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import global.TestGlobal;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestOptions extends TestCase {

    private static String testconfigFile = "peatio.json";
    private static String testconfig = TestGlobal.testconfigdir + "/" + testconfigFile;

    @Override
    public void setUp() {

    }

    @Test
    public void testConfigExists() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        final String wdir = System.getProperty("user.dir");

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
                    .parseOptionsSingle(testconfig);

            assertTrue(nuo != null);

            assertTrue(nuo.exchangeName.equals("Peatio"));

            //assertTrue(nuo.getPair() != null);

            //assertTrue(nuo.getSecondaryPegOptions() != null);
            //.getSpread())

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadComplete() {

        String testconfig = TestGlobal.testconfigdir + "/" + testconfigFile;
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

        String testconfig = TestGlobal.testconfigdir + "/" + testconfigFile;

        boolean catched = false;
        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig);

            assertTrue(opt.isSubmitliquidity()==false);

        } catch (NuBotConfigException e) {
            System.out.println("could not parse config");
            System.out.println(e);
            catched = true;
        }

        assertTrue(!catched);


    }

    @Test
    public void testLoadOptionsAll(){

        String testconfig = TestGlobal.testconfigdir + "/" + testconfigFile;

        boolean catched = false;
        NuBotOptions opt = null;
        try {
             opt = ParseOptions.parseOptionsSingle(testconfig);

        } catch (NuBotConfigException e) {
            catched = true;
        }

        assertTrue(!catched);

        assertTrue(opt.getExchangeName().equals("Peatio"));
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
        assertTrue(opt.backupFeedNames.get(0).equals("coinbase"));
        assertTrue(opt.backupFeedNames.get(1).equals("blockchain"));
        assertTrue(opt.wallchangeThreshold==0.1);
        assertTrue(opt.sendMailsLevel().equals(MailNotifications.MAIL_LEVEL_ALL));
        assertTrue(opt.sendMails()==true);
        assertTrue(opt.isDualSide()==true);
        assertTrue(opt.isSubmitliquidity()==false);
        assertTrue(opt.isMultipleCustodians()==false);
        assertTrue(opt.isExecuteOrders()==false);
        assertTrue(opt.isVerbose()==false);
        assertTrue(opt.isSendHipchat()==true);
        assertTrue(opt.emergencyTimeout==60);
        assertTrue(opt.keepProceeds==0);
        assertTrue(opt.maxBuyVolume==0.0);
        assertTrue(opt.maxSellVolume==0.0);
        assertTrue(opt.priceIncrement==0.1);
        assertTrue(opt.spread==0.0);
        assertTrue(opt.wallchangeThreshold==0.1);



        /*String nudIp = NuBotOptionsDefault.nudIp;
        String sendMails = NuBotOptionsDefault.sendMails;
        boolean submitLiquidity = NuBotOptionsDefault.submitLiquidity;
        boolean executeOrders = NuBotOptionsDefault.executeOrders;
        boolean verbose = NuBotOptionsDefault.verbose;
        boolean sendHipchat = NuBotOptionsDefault.sendHipchat;
        boolean multipleCustodians = NuBotOptionsDefault.multipleCustodians;
        int executeStrategyInterval = NuBotOptionsDefault.executeStrategyInterval;
        double txFee = NuBotOptionsDefault.txFee;
        double priceIncrement = NuBotOptionsDefault.priceIncrement;
        double keepProceeds = NuBotOptionsDefault.keepProceeds;
        double maxSellVolume = NuBotOptionsDefault.maxSellVolume;
        double maxBuyVolume = NuBotOptionsDefault.maxBuyVolume;
        int emergencyTimeout = NuBotOptionsDefault.emergencyTimeout;
        boolean distributeLiquidity = NuBotOptionsDefault.distributeLiquidity;*/

        assertTrue(opt.getPair()!=null);
        //assertTrue(opt.getPair().equals("nbt_btc"));



    }

    @Test
    public void testParseFeeds(){

        String testconfig = TestGlobal.testconfigdir + "/" + testconfigFile;
        boolean catched = false;
        try {
            NuBotOptions opt = ParseOptions.parseOptionsSingle(testconfig);
            assertTrue(opt.mainFeed!=null);
            assertTrue(opt.mainFeed.equals("btce"));

            assertTrue(opt.backupFeedNames.size()==2);
            assertTrue(opt.backupFeedNames.get(0).equals("coinbase"));
            assertTrue(opt.backupFeedNames.get(1).equals("blockchain"));

        } catch (NuBotConfigException e) {

            catched = true;
        }

        assertTrue(!catched);


    }

}