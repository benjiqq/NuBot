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

/**
 *
 * @author desrever <desrever at nubits.com>
 */
public class Currency {

//Class Variables
    private String symbol; // i.e. $
    private boolean fiat; // indicate whether its crypto or fiat
    private String code; // i.e USD
    private String extendedName; // the extended name where available

//Constructor
    /**
     *
     * @param symbol
     * @param fiat
     * @param code
     * @param extendedName
     */
    public Currency(String symbol, boolean fiat, String code, String extendedName) {
        this.symbol = symbol;
        this.fiat = fiat;
        this.code = code;
        this.extendedName = extendedName;
    }

    /**
     *
     * @return
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     *
     * @param symbol
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     *
     * @return
     */
    public boolean isFiat() {
        return fiat;
    }

    /**
     *
     * @param fiat
     */
    public void setFiat(boolean fiat) {
        this.fiat = fiat;
    }

    /**
     *
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     *
     * @param code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     *
     * @return
     */
    public String getExtendedName() {
        return extendedName;
    }

    /**
     *
     * @param extendedName
     */
    public void setExtendedName(String extendedName) {
        this.extendedName = extendedName;
    }

    @Override
    public String toString() {
        return "Currency{" + "symbol=" + symbol + ", fiat=" + fiat + ", code=" + code + ", extendedName=" + extendedName + '}';
    }
}
