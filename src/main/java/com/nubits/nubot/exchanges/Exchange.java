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

package com.nubits.nubot.exchanges;

import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Exchange {


    //Persisted
    private static final Logger LOG = LoggerFactory.getLogger(Exchange.class.getName());
    private String name; //Name of the exchange
    //Not persisted
    private ExchangeLiveData exchangeLiveData; //contains the data shown in the UI
    private ApiKeys keys;
    private TradeInterface trade;

    //handle concurrent requests
    private boolean isBusy;

    public Exchange(String name) {
        this.name = name;
        this.exchangeLiveData = new ExchangeLiveData();
        this.setFree();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExchangeLiveData getLiveData() {
        return exchangeLiveData;
    }

    public void setLiveData(ExchangeLiveData exchangeLiveData) {
        this.exchangeLiveData = exchangeLiveData;
    }

    public ApiKeys getKeys() {
        return keys;
    }

    public void setKeys(ApiKeys keys) {
        this.keys = keys;
    }

    public TradeInterface getTrade() {
        return trade;
    }

    public void setTrade(TradeInterface trade) {
        this.trade = trade;
    }

    public TradeInterface getTradeInterface() {
        return this.trade;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public boolean isFree() {
        return !isBusy;
    }

    public void setBusy() {
        this.isBusy = true;
    }

    public void setFree() {
        this.isBusy = false;
    }
}
