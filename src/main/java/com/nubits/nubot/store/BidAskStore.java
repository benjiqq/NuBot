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
