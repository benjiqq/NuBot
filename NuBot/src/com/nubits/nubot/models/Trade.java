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

package com.nubits.nubot.models;

import java.util.Date;

/**
 *
 * @author admin
 */
public class Trade {
    
    private String id; // A String containing a unique identifier for this order
    private String order_id; // A String containing a unique identifier for this order
    private CurrencyPair pair; //Object containing currency pair
    private String type; // string value containing either Constant.BUY or Constant.SELL
    private Amount price; //Object containing the price for each units traded.
    private Amount amount;    //Object containing the number of units for this trade (without fees).
    private Date date; //the time at which this trade was inserted place.

    public Trade(String id, String order_id, CurrencyPair pair, String type, Amount price, Amount amount, Date date) {
        this.id = id;
        this.order_id = order_id;
        this.pair = pair;
        this.type = type;
        this.price = price;
        this.amount = amount;
        this.date = date;
    }

    public Trade() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public void setPair(CurrencyPair pair) {
        this.pair = pair;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Amount getPrice() {
        return price;
    }

    public void setPrice(Amount price) {
        this.price = price;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Trade{" + "id=" + id + ", order_id=" + order_id + ", pair=" + pair.toString() + ", type=" + type + ", price=" + price.getQuantity() + ", amount=" + amount.getQuantity() + ", date=" + date + '}';
    }   
    
    public String toCsvString()
    {
        return id + "," + order_id + "," +pair.toString("_") + "," +type + "," +price.getQuantity() + "," +amount.getQuantity() + "," +date ;
    }
    
}
