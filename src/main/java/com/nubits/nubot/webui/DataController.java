package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.pricefeeds.feedservices.BlockchainPriceFeed;
import com.nubits.nubot.pricefeeds.feedservices.BtcePriceFeed;
import com.nubits.nubot.trading.TradeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

/**
 * controller for data updates
 */
public class DataController {

    final static Logger LOG = LoggerFactory.getLogger(DataController.class);

    public DataController(String endpoint) {


        get(endpoint, "application/json", (request, response) -> {

            Map opmap = new HashMap();

            //TODO: handle updates in storagelayer/pricefeed
            //TODO: use config

            CurrencyPair pair = CurrencyList.BTC_USD;

            //if bot is running

            if (Global.exchange.getTradeInterface()!=null) {
                TradeInterface ti = Global.exchange.getTradeInterface();
                opmap.put("btc_balance", ExchangeFacade.getBalance(ti, CurrencyList.BTC));
                opmap.put("nbt_balance", ExchangeFacade.getBalance(ti, CurrencyList.NBT));
                opmap.put("orders", ExchangeFacade.getOpenOrders(ti));
                //TODO: use internal feeder
                BtcePriceFeed btce = new BtcePriceFeed();
                double lastbtce= btce.getLastPrice(pair).getPrice().getQuantity();

                BlockchainPriceFeed bci = new BlockchainPriceFeed();
                double lastbci= bci.getLastPrice(pair).getPrice().getQuantity();

                opmap.put("btce_lastprice",lastbtce);
                opmap.put("bci_lastprice",lastbci);
            } else{
                opmap.put("btc_balance", 0);
                opmap.put("nbt_balance", 0);
                opmap.put("orders", "");
                opmap.put("btce_lastprice",0);
                opmap.put("bci_lastprice",0);
            }


            String json = new Gson().toJson(opmap);

            return json;
        });


    }
}
