/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
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
package com.nubits.nubot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.CurrencyPair;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FrozenBalancesManager {

    private static final Logger LOG = Logger.getLogger(FrozenBalancesManager.class.getName());
    private String pathToFrozenBalancesFiles;
    private FrozenAmount frozenAmount;
    private String exchangeName;
    private CurrencyPair pair;
    private String folder;
    private ArrayList<HistoryRow> history;

    //Call this on bot startup
    public FrozenBalancesManager(String exchangName, CurrencyPair pair, String folder) {
        String fileName = pair.toString("_") + "-" + exchangName + "-frozen.json";
        this.pathToFrozenBalancesFiles = folder + fileName;
        this.exchangeName = exchangName;
        this.pair = pair;
        history = new ArrayList<>();
        if (new File(pathToFrozenBalancesFiles).exists()) {
            parseFrozenBalancesFile();
        } else {
            //Create the file and write 0 on it
            frozenAmount = new FrozenAmount(new Amount(0, pair.getPaymentCurrency()));
            updateFrozenFilesystem();
        }
    }

    //use this method to set frozen amount
    public void setInitialFrozenAmount(Amount newAmount, boolean writeToFile) {
        this.frozenAmount = new FrozenAmount(newAmount);
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);
        LOG.info("Setting frozen amount to : " + df.format(this.frozenAmount.getAmount().getQuantity()) + " " + pair.getPaymentCurrency().getCode());
        if (writeToFile) {
            updateFrozenFilesystem();
        }
    }

    //Use this method to add frozen balance (on top of the existing balance)
    public void updateFrozenBalance(Amount toAdd) {
        double oldQuantity = this.frozenAmount.getAmount().getQuantity();
        double quantityToAdd = toAdd.getQuantity();
        Amount newAmount = new Amount(oldQuantity + quantityToAdd, pair.getPaymentCurrency());
        this.frozenAmount = new FrozenAmount(newAmount);
        //here I could log the history

        HistoryRow historyRow = new HistoryRow(new Date(), quantityToAdd, pair.getPaymentCurrency().getCode());
        history.add(historyRow);

        updateFrozenFilesystem();
    }

    //Use this method to retreive the updated amount
    public FrozenAmount getFrozenAmount() {
        return frozenAmount;
    }

    public void reset() {
        this.frozenAmount = new FrozenAmount(new Amount(0, pair.getPaymentCurrency()));
        updateFrozenFilesystem();
    }

    private void parseFrozenBalancesFile() {
        JSONParser parser = new JSONParser();
        String FrozenBalancesManagerString = FileSystem.readFromFile(this.pathToFrozenBalancesFiles);
        try {
            JSONObject frozenBalancesJSON = (JSONObject) (parser.parse(FrozenBalancesManagerString));
            double quantity = Double.parseDouble((String) frozenBalancesJSON.get("frozen-quantity-total"));
            Amount frozenAmount = new Amount(quantity, pair.getPaymentCurrency());
            setInitialFrozenAmount(frozenAmount, false);

            JSONArray historyArr = (JSONArray) frozenBalancesJSON.get("history");
            for (int i = 0; i < historyArr.size(); i++) {
                JSONObject tempHistoryRow = (JSONObject) historyArr.get(i);

                DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
                Date timestamp = df.parse((String) tempHistoryRow.get("timestamp"));

                double frozeQuantity = Double.parseDouble((String) tempHistoryRow.get("froze-quantity"));
                String currencyCode = (String) tempHistoryRow.get("currency-code");

                history.add(new HistoryRow(timestamp, frozeQuantity, currencyCode));
            }

        } catch (ParseException | NumberFormatException | java.text.ParseException e) {
            LOG.severe("Error while parsing the frozen balances file (" + pathToFrozenBalancesFiles + ")\n"
                    + e.getMessage());
        }
    }

    private void updateFrozenFilesystem() {
        String toWrite = "";
        JSONObject toWriteJ = new JSONObject();

        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(8);

        toWriteJ.put("frozen-quantity-total", df.format(getFrozenAmount().getAmount().getQuantity()));
        toWriteJ.put("frozen-currency", getFrozenAmount().getAmount().getCurrency().getCode());
        JSONArray historyListJ = new JSONArray();
        for (int i = 0; i < history.size(); i++) {
            JSONObject tempRow = new JSONObject();
            HistoryRow tempHistory = history.get(i);



            tempRow.put("timestamp", tempHistory.getTimestamp().toString());
            tempRow.put("froze-quantity", df.format(tempHistory.getFreezedQuantity()));
            tempRow.put("currency-code", tempHistory.getCurrencyCode());

            historyListJ.add(tempRow);
        }

        toWriteJ.put("history", historyListJ);

        toWrite += toWriteJ.toString();


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(toWrite);
        String toWritePretty = gson.toJson(je);

        try {
            FileUtils.writeStringToFile(new File(pathToFrozenBalancesFiles), toWritePretty);
            LOG.info("Updated Froozen Balances file (" + pathToFrozenBalancesFiles + ") : " + df.format(getFrozenAmount().getAmount().getQuantity()) + " " + pair.getPaymentCurrency().getCode());
        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        }

    }

    public String getCurrencyCode() {
        return pair.getPaymentCurrency().getCode();
    }

    public class FrozenAmount {

        private Amount amount;

        public FrozenAmount(Amount amount) {
            this.amount = new Amount(Utils.round(amount.getQuantity(), 8), amount.getCurrency());
        }

        public Amount getAmount() {
            return amount;
        }

        public void setAmount(Amount amount) {
            this.amount = new Amount(Utils.round(amount.getQuantity(), 8), amount.getCurrency());
        }

        @Override
        public String toString() {
            return amount.getQuantity() + " " + amount.getCurrency().getCode().toUpperCase();
        }
    }

    public class HistoryRow {

        private Date timestamp;
        private double frozeQuantity;
        private String currencyCode;

        public HistoryRow(Date timestamp, double frozeQuantity, String currencyCode) {
            this.timestamp = timestamp;
            this.frozeQuantity = frozeQuantity;
            this.currencyCode = currencyCode;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public double getFreezedQuantity() {
            return frozeQuantity;
        }

        public void setFreezedQuantity(double frozeQuantity) {
            this.frozeQuantity = frozeQuantity;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        @Override
        public String toString() {
            return "historyRow{" + "timestamp=" + timestamp + ", frozeQuantity=" + frozeQuantity + ", currencyCode=" + currencyCode + '}';
        }
    }
}
