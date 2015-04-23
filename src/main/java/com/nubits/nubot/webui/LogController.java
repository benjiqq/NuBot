package com.nubits.nubot.webui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.Order;
import com.nubits.nubot.models.PairBalance;
import com.nubits.nubot.utils.Utils;
import org.json.simple.JSONObject;
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

    private String orderEndPoint = "orders";
    private String balanceEndPoint = "balances";

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
                LOG.trace("empty log cue" + e.toString());
            }
            return "error fetching log";
        });


        get("/" + orderEndPoint, "application/json", (request, response) -> {
            LOG.trace("/" + orderEndPoint + " called");
            Map opmap = new HashMap();
            int numbuys = 0;
            int numsells = 0;

            if (Global.sessionRunning) {
                try {

                    Global.orderManager.logActiveOrders();

                    numbuys = Global.orderManager.fetchBuyOrdersTimeBound(Settings.ORDER_MAX_INTERVAL);
                    numsells = Global.orderManager.fetchSellOrdersTimeBound(Settings.ORDER_MAX_INTERVAL);

                    LOG.trace("GET /info : buys: " + numbuys);
                    LOG.trace("GET /info : sells: " + numsells);

                    ArrayList<Order> ol = Global.orderManager.getOrderList();
                    opmap.put("orders", ol);
                    LOG.debug("orders: " + ol);

                } catch (Exception e) {
                    LOG.error(e.toString());
                }
            }

            opmap.put("buys", numbuys);
            opmap.put("sells", numsells);


            String json = new Gson().toJson(opmap);
            LOG.trace("info/ data: " + json);
            return json;
        });


        get("/" + balanceEndPoint, "application/json", (request, response) -> {
            LOG.trace("/" + balanceEndPoint + " called");
            Map opmap = new HashMap();

            if (Global.sessionRunning) {
                try {
                    try {
                        //query only up to every X msec, otherwise just get the last info
                        //this caps the maximum queries we can do, so to not overload the exchange
                        Global.balanceManager.fetchBalancePairTimeBound(Global.options.getPair(), Settings.BALANCE_MAX_INTERVAL);
                        PairBalance balance = Global.balanceManager.getPairBalance();
                        opmap.put("pegBalance", prepareBalanceObject("peg", balance));
                        opmap.put("nbtBalance", prepareBalanceObject("nbt", balance));
                    } catch (Exception e) {
                        LOG.error(e.toString());
                    }

                } catch (Exception e) {
                    LOG.error(e.toString());
                }
            }

            String json = new Gson().toJson(opmap);
            return json;
        });


    }

    private HashMap prepareBalanceObject(String type, PairBalance balance) {
        String toRet = "";
        HashMap balanceObj = new JSONObject();

        if (type.equalsIgnoreCase("peg")) {
            balanceObj.put("currencyCode",
                    balance.getPEGAvailableBalance().getCurrency().getCode().toUpperCase());

            balanceObj.put("balanceTotal",
                    Utils.formatNumber(balance.getPEGBalance().getQuantity(), 6));
            balanceObj.put("balanceAvailable",
                    Utils.formatNumber(balance.getPEGAvailableBalance().getQuantity(), 6));
            balanceObj.put("balanceLocked",
                    Utils.formatNumber(balance.getPEGBalanceonOrder().getQuantity(), 6));

        } else // nbt
        {
            balanceObj.put("currencyCode", balance.getNBTAvailable().getCurrency().getCode().toUpperCase());

            balanceObj.put("balanceTotal",
                    Utils.formatNumber(balance.getNubitsBalance().getQuantity(), 6));
            balanceObj.put("balanceAvailable",
                    Utils.formatNumber(balance.getNBTAvailable().getQuantity(), 6));
            balanceObj.put("balanceLocked",
                    Utils.formatNumber(balance.getNBTonOrder().getQuantity(), 6));
        }
        return balanceObj;
    }
}
