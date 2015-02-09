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
package com.nubits.nubot.launch.toolkit;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.OptionsJSON;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.BterWrapper;
import com.nubits.nubot.trading.wrappers.CcedkWrapper;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import com.nubits.nubot.utils.logging.NuLogger;
import java.io.IOException;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.simple.parser.JSONParser;

public class NuCMC {

    private static final Logger LOG = Logger.getLogger(NuCMC.class.getName());
    private static final String USAGE_STRING = "java  -jar NuCMC <path/to/options.json>";
    public static final String HEADER = ".. , ..., ...,\n";
    private String optionsPath;
    private static Thread mainThread;
    //fields
    Exchange ccedk, bter;
    double threshold;
    CurrencyPair pair;

    public static void main(String[] args) {
        mainThread = Thread.currentThread();
        //Load settings
        Utils.loadProperties("settings.properties");

        String folderName = "NuCMC_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;
        //Create log dir
        FileSystem.mkdir(logsFolder);
        try {
            NuLogger.setup(false, logsFolder);
        } catch (IOException ex) {
            LOG.severe(ex.toString());
        }

        NuCMC app = new NuCMC();
        if (app.readParams(args)) {
            createShutDownHook();

            LOG.fine("Launching NuCheckPrice ");
            app.exec();
        } else {
            System.exit(0);
        }
    }

    private void exec() {
        //Creating options
        if (readOptions()) {
            init();





        } else {
            LOG.severe("Problem while reading options from " + optionsPath);
            System.exit(0);
        }



    }

    private boolean readOptions() {
        boolean ok = false;
        OptionsJSON options = null;
        JSONParser parser = new JSONParser();
        String optionsString = FileSystem.readFromFile(optionsPath);
        try {
            org.json.JSONObject jsonString = new org.json.JSONObject(optionsString);
            org.json.JSONObject optionsJSON = (org.json.JSONObject) jsonString.get("options");


            String ccedkKey = (String) optionsJSON.get("ccedk-key");
            String ccedkSecret = (String) optionsJSON.get("ccedk-secret");
            String bterKey = (String) optionsJSON.get("bter-key");
            String bterSecret = (String) optionsJSON.get("bter-secret");

            threshold = Utils.getDouble(optionsJSON.get("threshold"));

            Exchange ccedk = new Exchange("ccedk");
            Exchange bter = new Exchange("bter");
            //Create e ExchangeLiveData object to accomodate liveData from the Global.exchange
            ExchangeLiveData liveDataC = new ExchangeLiveData();
            ExchangeLiveData liveDataB = new ExchangeLiveData();
            ccedk.setLiveData(liveDataC);
            bter.setLiveData(liveDataB);



            ccedk.setTrade(new CcedkWrapper(new ApiKeys(ccedkSecret, ccedkKey), ccedk));
            bter.setTrade(new BterWrapper(new ApiKeys(bterSecret, bterKey), bter));

            String cp = (String) optionsJSON.get("pair");

            pair = CurrencyPair.getCurrencyPairFromString(cp, "_");

            ok = true;
        } catch (JSONException | NumberFormatException ex) {
            LOG.severe(ex.toString());
            ok = false;
        }
        return ok;
    }

    private boolean readParams(String[] args) {
        boolean ok = false;

        if (args.length != 1) {
            LOG.severe("wrong argument number : call it with \n" + USAGE_STRING);
            System.exit(0);
        }


        optionsPath = args[0];


        ok = true;
        return ok;
    }

    private static void createShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.fine("Exiting...");
                mainThread.interrupt();
                Global.taskManager.stopAll();

                Thread.currentThread().interrupt();
                return;
            }
        }));
    }

    private void init() {
        System.setProperty("javax.net.ssl.trustStore", Global.settings.getProperty("keystore_path"));
        System.setProperty("javax.net.ssl.trustStorePassword", Global.settings.getProperty("keystore_pass"));

        Global.taskManager = new TaskManager();

        clearAll(ccedk);
        clearAll(bter);

    }

    private void clearAll(Exchange exchange) {
        ApiResponse deleteOrdersResponse = exchange.getTrade().clearOrders(pair);
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();

            if (deleted) {
                LOG.info("Order clear request succesfully");
            } else {
                LOG.info("Could not submit request to clear orders");
            }

        } else {
            LOG.severe(deleteOrdersResponse.getError().toString());
        }
    }
}
