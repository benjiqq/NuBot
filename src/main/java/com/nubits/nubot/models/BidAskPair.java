package com.nubits.nubot.models;

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
