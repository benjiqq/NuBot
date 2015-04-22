package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;


public class LogController {

    /**
     * the log file to send to client
     */
    String logfile = Global.sessionPath + "/ui_standard.log";
    String verboselogfile = Global.sessionPath + "/ui_verbose.log";

    final static Logger LOG = LoggerFactory.getLogger(LogController.class);

    public LogController() {

        get("/logdump", "application/json", (request, response) -> {
            LOG.trace("/logdump called");
            JsonObject object = new JsonObject();

            String f = logfile;
            if (Global.options.isVerbose())
                f = verboselogfile;

            try {
                String l = new String(Files.readAllBytes(Paths.get(f)));

                object.addProperty("log", l);
                return object;

            } catch (Exception e) {

            }

            return "error fetching log";

        });

        get("/info", "application/json", (request, response) -> {
            LOG.debug("/info called");
            Map opmap = new HashMap();
            int numbuys = 0;
            int numsells = 0;

            if (Global.sessionRunning) {
                try {

                    Global.orderManager.logActiveOrders();
                    numbuys = Global.orderManager.getNumActiveBuyOrders();
                    numsells = Global.orderManager.getNumActiveSellOrders();
                    LOG.debug("buys: " + numbuys);
                    LOG.debug("sells: " + numsells);

                    ArrayList<Order> ol = Global.orderManager.getOrderList();
                    opmap.put("orders", ol);
                    for (Order o : ol) {
                        LOG.debug("order: " + o);
                    }

                    //TODO: use global pair
                    try {
                        Global.balanceManager.fetchBalance(CurrencyList.BTC);
                        Amount balance = Global.balanceManager.getBalance();
                        opmap.put("BuyCurrency", balance);
                    } catch (Exception e) {
                    }

                    try {
                        Global.balanceManager.fetchBalance(CurrencyList.NBT);
                        Amount balance = Global.balanceManager.getBalance();
                        opmap.put("SellCurrency", balance);
                    } catch (Exception e) {
                    }


                } catch (Exception e) {

                }
            }

            opmap.put("buys", numbuys);
            opmap.put("sells", numsells);


            String json = new Gson().toJson(opmap);
            return json;

        });


    }
}
