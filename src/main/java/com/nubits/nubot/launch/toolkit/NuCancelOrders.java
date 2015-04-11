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

package com.nubits.nubot.launch.toolkit;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.PeatioWrapper;
import com.nubits.nubot.utils.FilesystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NuCancelOrders {

    private static final Logger LOG = LoggerFactory.getLogger(NuCancelOrders.class.getName());
    private String api;
    private String secret;
    private String exchangename;
    private CurrencyPair pair;
    private ApiKeys keys;
    public static final String USAGE_STRING = "java -jar NuCancelOrders <apikey> <secretkey> <exchange-name> <pair>";

    public static void main(String[] args) {


        NuCancelOrders app = new NuCancelOrders();
        String folderName = "NuCancelOrders_" + System.currentTimeMillis() + "/";
        String logsFolder = Settings.LOGS_PATH + "/" + folderName;
        //Create log dir
        FilesystemUtils.mkdir(logsFolder);
        if (app.readParams(args)) {

            LOG.info("Launching CancellAllOrders ");
            app.prepareForExecution();
            app.cancelAllOrders(app.pair);
            LOG.info("Done");
            System.exit(0);

        } else {
            System.exit(0);
        }
    }

    private void cancelAllOrders(CurrencyPair pair) {

        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(pair);
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

            if (deleted) {
                LOG.info("Clear request succesfully");
            } else {
                LOG.info("Could not submit request to clear orders");
            }

        } else {
            LOG.error(deleteOrdersResponse.getError().toString());
        }

        System.exit(0);


    }

    private void prepareForExecution() {
        //Wrap the keys into a new ApiKeys object
        keys = new ApiKeys(secret, api);

        Global.exchange = new Exchange(exchangename);

        //Switch the ip of exchange
        String apibase = "";
        if (exchangename.equalsIgnoreCase(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
            apibase = ExchangeFacade.INTERNAL_EXCHANGE_PEATIO_API_BASE;
        } else {
            LOG.error("Exchange name not accepted : " + exchangename);
            System.exit(0);
        }

        //Create e ExchangeLiveData object to accomodate liveData from the exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        //Create a new TradeInterface object using the PeatioWrapper implementation
        //Assign the TradeInterface to the PeatioExchange
        Global.exchange.setTrade(new PeatioWrapper(keys, Global.exchange, apibase));
        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());


        //Create a TaskManager and
        Global.taskManager = new TaskManager();
        Global.taskManager.setTasks();
        //Start checking for connection
        Global.taskManager.getCheckConnectionTask().start();


        //Wait a couple of seconds for the connectionThread to get live
        LOG.info("Exchange setup complete. Now checking connection ...");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

    }

    private boolean readParams(String[] args) {
        boolean ok = false;

        if (args.length != 4) {
            LOG.error("wrong argument number : call it with \n" + USAGE_STRING);
            System.exit(0);
        }


        api = args[0];
        secret = args[1];
        exchangename = args[2];
        pair = CurrencyPair.getCurrencyPairFromString(args[3], "_");

        ok = true;
        return ok;
    }
}
