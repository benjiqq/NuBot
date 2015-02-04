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
package com.nubits.nubot.models;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import java.util.logging.Logger;

//TODO rename (requires refactoring of current Order class)
public class OrderToPlace {

    private static final Logger LOG = Logger.getLogger(OrderToPlace.class.getName());
    private String type; // string value containing either Constant.BUY or Constant.SELL
    private CurrencyPair pair; //Object containing currency pair
    private Amount amount;    //Object containing the number of units for this trade (without fees). Expressed in pair.OrderCurrency
    private Amount price; //Object containing the price for each units traded. Expressed in pair.PaymentCurrency

    public OrderToPlace(String type, CurrencyPair pair, Amount amount, Amount price) {
        this.type = type;
        this.pair = pair;
        this.amount = amount;
        this.price = price;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public CurrencyPair getPair() {
        return pair;
    }

    /**
     *
     * @param pair
     */
    public void setPair(CurrencyPair pair) {
        this.pair = pair;
    }

    /**
     *
     * @return
     */
    public Amount getAmount() {
        return amount;
    }

    /**
     *
     * @param amount
     */
    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    /**
     *
     * @return
     */
    public Amount getPrice() {
        return price;
    }

    /**
     *
     * @param price
     */
    public void setPrice(Amount price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "OrderToPlace{" + "type=" + type + ", pair=" + pair + ", amount=" + amount + ", price=" + price + '}';
    }
}
