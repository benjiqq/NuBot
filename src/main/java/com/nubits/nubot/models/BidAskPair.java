package com.nubits.nubot.models;

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

/**
 * a Quote to buy and sell, i.e. a pair of one Bid and one Ask price
 * (quantities are not considered)
 */
public class BidAskPair {

    private double bid;
    private double ask;

    public BidAskPair(double bid, double ask){
        this.bid = bid;
        this.ask = ask;
    }

    public void setBidAsk(double newBid, double newAsk){
        this.bid = newBid;
        this.ask = newAsk;
    }

    public double getBid(){
        return this.bid;
    }

    public double getAsk(){
        return this.ask;
    }
}
