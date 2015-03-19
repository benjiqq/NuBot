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

import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.Trade;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;
import com.nubits.nubot.trading.wrappers.BterWrapper;
import com.nubits.nubot.trading.wrappers.CcedkWrapper;
import com.nubits.nubot.trading.wrappers.ExcoinWrapper;
import com.nubits.nubot.utils.FileSystem;
import com.nubits.nubot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

public class NuLastTrades {

    private static final Logger LOG = LoggerFactory.getLogger(NuLastTrades.class.getName());
    private final String USAGE_STRING = "java -jar NuLastTrades <exchange-name> <apikey> <apisecret> <currency_pair> [<date_from>]";
    private final String HEADER = "id,order_id,pair,type,price,amount,date";
    private String output;
    private String api;
    private String secret;
    private String exchangename;
    private long dateFrom;
    private CurrencyPair pair;
    private ApiKeys keys;

    public static void main(String[] args) {
        //Load settings
        try{
            Utils.loadProperties("settings.properties");
        }catch(IOException e){

        }

        NuLastTrades app = new NuLastTrades();

        String folderName = "NuLastTrades_" + System.currentTimeMillis() + "/";
        String logsFolder = Global.settings.getProperty("log_path") + folderName;

        //Create log dir
        FileSystem.mkdir(logsFolder);
        if (app.readParams(args)) {

            LOG.info("Launching NuLastTrades on " + app.exchangename);
            app.prepareForExecution();
            app.execute();
            LOG.info("Done");
            System.exit(0);

        } else {
            System.exit(0);
        }
    }

    private void prepareForExecution() {
        //Wrap the keys into a new ApiKeys object
        keys = new ApiKeys(secret, api);

        Global.exchange = new Exchange(exchangename);

        //Create e ExchangeLiveData object to accomodate liveData from the exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);


        if (exchangename.equals(ExchangeFacade.BTCE)) {
            Global.exchange.setTrade(new BtceWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
            Global.exchange.setTrade(new PeatioWrapper(keys, Global.exchange, ExchangeFacade.INTERNAL_EXCHANGE_PEATIO_API_BASE));
        } else if (exchangename.equals(ExchangeFacade.CCEDK)) {
            Global.exchange.setTrade(new CcedkWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.BTER)) {
            Global.exchange.setTrade(new BterWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.ALLCOIN)) {
            Global.exchange.setTrade(new AllCoinWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.BITSPARK_PEATIO)) {
            Global.exchange.setTrade(new BitSparkWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.POLONIEX)) {
            Global.exchange.setTrade(new PoloniexWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.CCEX)) {
            Global.exchange.setTrade(new CcexWrapper(keys, Global.exchange));
        } else if (exchangename.equals(ExchangeFacade.EXCOIN)) {
            Global.exchange.setTrade(new ExcoinWrapper(keys, Global.exchange));
        } else {
            LOG.error("Exchange " + exchangename + " not supported");
            System.exit(0);
        }

        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());


        //Create a TaskManager and
        Global.taskManager = new TaskManager();
        //Start checking for connection
        Global.taskManager.getCheckConnectionTask().start();

        //Wait a couple of seconds for the connectionThread to get live
        LOG.info("Exchange setup complete. Now checking connection ...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

    }

    private boolean readParams(String[] args) {
        boolean ok = false;

        if (args.length != 4 && args.length != 5) {
            LOG.error("wrong argument number : call it with \n" + USAGE_STRING);
            System.exit(0);
        }

        exchangename = args[0];
        api = args[1];
        secret = args[2];
        pair = CurrencyPair.getCurrencyPairFromString(args[3], "_");

        if (args.length == 5) {
            dateFrom = Long.parseLong(args[4]);
        }

        output = "last_trades_" + exchangename + "_" + pair.toString() + ".json";
        ok = true;
        return ok;
    }

    private void execute() {
        //FileSystem.writeToFile(HEADER, output, false); //uncomment for csv outputs
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair, dateFrom);

        if (pair.getPaymentCurrency().equals(CurrencyList.NBT)) {
            Global.swappedPair = true;
        } else {
            Global.swappedPair = false;
        }

        int count = 0;
        int countSell = 0, countBuy = 0;
        int totalAmountPEG = 0;
        int totalAmountNBT = 0;
        int threshold = 1000; //NBT
        int countLargeOrders = 0;
        int paidInFees = 0;

        if (activeOrdersResponse.isPositive()) {
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            FileSystem.writeToFile("{\n", output, false);
            //FileSystem.writeToFile("\"exchange\":\"" + exchangename + "\",\n", output, true);
            //FileSystem.writeToFile("\"pair\":\"" + pair.toStringSep("_") + "\",\n", output, true);
            LOG.info("Last trades : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);

                //Added for excoin only

                if (tempTrade.getType().equalsIgnoreCase(Constant.SELL)) {
                    countSell++;
                } else {
                    countBuy++;
                }

                count++;

                double amountNBT;

                if (Global.swappedPair) {
                    amountNBT = tempTrade.getAmount().getQuantity() * tempTrade.getPrice().getQuantity();
                    totalAmountNBT += amountNBT;
                    totalAmountPEG += tempTrade.getAmount().getQuantity();
                } else {
                    amountNBT = tempTrade.getAmount().getQuantity();
                    totalAmountPEG += amountNBT * tempTrade.getPrice().getQuantity();
                    totalAmountNBT += tempTrade.getAmount().getQuantity();
                }

                if (amountNBT >= threshold) {
                    countLargeOrders++;
                }

                paidInFees += tempTrade.getFee().getQuantity();
                LOG.info(tempTrade.toString());
                String comma = ",\n";
                if (i == tradeList.size() - 1) {
                    comma = "";
                }
                FileSystem.writeToFile(tempTrade.toJSONString() + comma, output, true);
            }
            FileSystem.writeToFile("}", output, true);
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }

        //Report :
        String currencyCode = "";
        if (Global.swappedPair) {
            currencyCode = pair.getOrderCurrency().getCode();
        } else {
            currencyCode = pair.getPaymentCurrency().getCode();
        }

        String report = "Executed orders : " + count + " (sells : " + countSell + ";  buys : " + countBuy + ")"
                + "\nTotal volume transacted : " + totalAmountPEG + " BTC ; " + totalAmountNBT + " NBT )"
                + "\nOrders > " + threshold + " NBT : " + countLargeOrders
                + "\nPaid in fees : " + paidInFees;
        LOG.info(report);
        FileSystem.writeToFile(report, output + "_report.txt", false);
    }
}
