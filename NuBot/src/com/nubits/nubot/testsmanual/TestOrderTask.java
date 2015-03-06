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
package com.nubits.nubot.testsmanual;

import com.nubits.nubot.RPC.NuRPCClient;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.global.Passwords;
import com.nubits.nubot.options.NuBotOptionsDefault;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.PeatioWrapper;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class TestOrderTask {

    private static final Logger LOG = LoggerFactory.getLogger(TestOrderTask.class.getName());
    private static Exchange exchange;
    private static String nudip = "127.0.0.1";
    private static int nudport = 9091;

    public static void main(String[] args) {
        setup();
        Global.taskManager.getSendLiquidityTask().start();
    }

    private static void setup() {

        String folderName = "tests_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;


        Utils.installKeystore(true);

        Global.options = NuBotOptionsDefault.defaultFactory();


        //Check local filesystem for API keys
        LOG.info("Checking existance of API keys on local filesystem");

        ApiKeys keys;

        String secret = PasswordsTest.INTERNAL_PEATIO_SECRET;
        String apikey = PasswordsTest.INTERNAL_PEATIO_KEY;

        //Wrap the keys into a new ApiKeys object
        keys = new ApiKeys(secret, apikey);

        //Create a new Exchange


        exchange = new Exchange(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO);

        //Create e ExchangeLiveData object to accomodate liveData from the exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        exchange.setLiveData(liveData);


        //Create a new TradeInterface object using the BtceWrapper implementation
        //Assign the TradeInterface to the btceExchange
        exchange.setTrade(new PeatioWrapper(keys, exchange, ExchangeFacade.INTERNAL_EXCHANGE_PEATIO_API_BASE));
        exchange.getLiveData().setUrlConnectionCheck(exchange.getTrade().getUrlConnectionCheck());

        //Create a TaskManager and
        Global.taskManager = new TaskManager();
        //Start checking for connection
        Global.taskManager.getCheckConnectionTask().start();


        Global.publicAddress = PasswordsTest.CUSTODIAN_PUBLIC_ADDRESS;

        LOG.info("Setting up (verbose) RPC client on " + nudip + ":" + nudport);
        Global.rpcClient = new NuRPCClient(nudip, nudport, PasswordsTest.NUD_RPC_USER,
                PasswordsTest.NUD_RPC_PASS, true, true, Global.publicAddress, Constant.NBT_PPC, "testid");

        Utils.printSeparator();


        Global.taskManager.getCheckNudTask().start();
        //Wait a couple of seconds for the connectionThread to get live
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }


        LOG.info("Check connection with nud");
        if (Global.rpcClient.isConnected()) {
            LOG.info("OK!");
        } else {
            LOG.error("Problem while connecting with nud");
            System.exit(0);
        }





        /* Setup (end) ------------------------------------------------------------------------------------------------------ */

    }
}
