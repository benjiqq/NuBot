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

package com.nubits.nubot.pricefeeds.feedservices;

import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.LastPrice;
import com.nubits.nubot.pricefeeds.FeedFacade;
import com.nubits.nubot.trading.wrappers.TradeUtilsCCEDK;
import com.nubits.nubot.utils.Utils;
import java.io.IOException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class CcedkPriceFeed extends AbstractPriceFeed {

    private static final Logger LOG = LoggerFactory.getLogger(CcedkPriceFeed.class.getName());

    public static final String name = FeedFacade.CcedkPriceFeed;

    public CcedkPriceFeed() {
        refreshMinTime = 50 * 1000; //one minutee
    }

    @Override
    public LastPrice getLastPrice(CurrencyPair pair) {
        String url = TradeUtilsCCEDK.getCCEDKTickerUrl(pair);
        long now = System.currentTimeMillis();
        long diff = now - lastRequest;
        if (diff >= refreshMinTime) {
            String htmlString;
            try {
                htmlString = Utils.getHTML(url, true);
            } catch (IOException ex) {
                LOG.error(ex.toString());
                return new LastPrice(true, name, pair.getOrderCurrency(), null);
            }
            JSONParser parser = new JSONParser();
            try {
                //{"errors":false,"response":{"entity":{"pair_id":"2","min":"510","max":"510","avg":"510","vol":"0.0130249"}}}
                JSONObject httpAnswerJson = (JSONObject) (parser.parse(htmlString));
                JSONObject tickerObject = (JSONObject) httpAnswerJson.get("response");
                JSONObject entityObject = (JSONObject) tickerObject.get("entity");

                double last = Double.valueOf((String) entityObject.get("avg"));

                lastRequest = System.currentTimeMillis();
                lastPrice = new LastPrice(false, name, pair.getOrderCurrency(), new Amount(last, pair.getPaymentCurrency()));
                return lastPrice;
            } catch (Exception ex) {
                LOG.error(ex.toString());
                lastRequest = System.currentTimeMillis();
                return new LastPrice(true, name, pair.getOrderCurrency(), null);
            }
        } else {
            LOG.info("Wait " + (refreshMinTime - (System.currentTimeMillis() - lastRequest)) + " ms "
                    + "before making a new request. Now returning the last saved price\n\n");
            return lastPrice;
        }

    }
}
