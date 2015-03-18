package com.nubits.nubot.store;

import com.nubits.nubot.models.BidAskPair;

import java.util.Observable;

/**
 * in memory store for data points
 */
public class BidAskStore {
//extends Observable

    private final Object lock = new Object();

    private BidAskPair bidask;

    public void setPricePEG(BidAskPair bidask){
        synchronized (lock) {
            this.bidask = bidask;
            /*setChanged();
            notifyObservers(this.bidask);*/
        }

    }

    public BidAskPair getPricePeg(){
        synchronized (lock) {
            return this.bidask;
        }
    }
}
