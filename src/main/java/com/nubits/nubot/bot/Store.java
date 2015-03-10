package com.nubits.nubot.bot;

import com.nubits.nubot.models.BidAskPair;

import java.util.Observable;

/**
 * in memory store for data points
 */
public class Store extends Observable {

    private BidAskPair bidask;

    public void setPricePEG(BidAskPair bidask){
        this.bidask = bidask;
        setChanged();
        notifyObservers(this.bidask);
    }

    public BidAskPair getPricePeg(){
        return this.bidask;
    }
}
