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
package com.nubits.nubot.trading.LiquidityDistribution;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.Amount;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.OrderToPlace;
import com.nubits.nubot.utils.Utils;
import java.util.ArrayList;
import java.util.logging.Logger;

public class LiquidityDistributionModel {

    private static final Logger LOG = Logger.getLogger(LiquidityDistributionModel.class.getName());
    private ModelParameters sellParams, buyParams;

    public LiquidityDistributionModel(ModelParameters sellParams, ModelParameters buyParams) {
        this.sellParams = sellParams;
        this.buyParams = buyParams;
    }

    //Generate basic order distribution model
    public static LiquidityDistributionModel generateStaticDistribution() {
        return new LiquidityDistributionModel(ModelParameters.generateTestParams(), ModelParameters.generateTestParams());
    }

    public ArrayList<OrderToPlace> getOrdersToPlace(String type, Amount fundsAvailable, double pegPrice, CurrencyPair pair, double txFee) {
        if (type.equals(Constant.SELL)) {
            return getOrdersToPlaceImpl(this.sellParams, fundsAvailable, Constant.SELL, pegPrice, pair, txFee);
        } else {
            return getOrdersToPlaceImpl(this.buyParams, fundsAvailable, Constant.BUY, pegPrice, pair, txFee);
        }
    }

    //TODO implement this
    private ArrayList<OrderToPlace> getOrdersToPlaceImpl(ModelParameters params, Amount funds, String wallType, double pegPrice, CurrencyPair pair, double txFee) {
        ArrayList<OrderToPlace> toReturn = new ArrayList();

        //First create the wall order and add it to the list
        toReturn.add(buildWall(pegPrice, params, txFee, wallType, pair));

        return toReturn;
    }

    private OrderToPlace buildWall(double pegPrice, ModelParameters params, double txFee, String wallType, CurrencyPair pair) {
        double oneUSD = Utils.round(1 / pegPrice, 8); //one $ expressed in the peg currency
        double wallPrice = oneUSD;

        double offset = Utils.round(params.getOffset() * oneUSD, 8); //Convert the spread in the peg currency
        double fee = Utils.round((oneUSD / 100) * txFee, 8); //Convert the txFee in the peg currency

        double totalOffset = offset + fee;//Compute the total offset by adding spread+fee

        //Add it or remove it from the price, based on the type of order
        if (wallType.equals(Constant.SELL)) {
            wallPrice += totalOffset;
        } else {
            wallPrice -= totalOffset;
        }

        return new OrderToPlace(wallType, pair, params.getWallHeight(), wallPrice);
    }

    public ModelParameters getSellParams() {
        return sellParams;
    }

    public void setSellParams(ModelParameters sellParams) {
        this.sellParams = sellParams;
    }

    public ModelParameters getBuyParams() {
        return buyParams;
    }

    public void setBuyParams(ModelParameters buyParams) {
        this.buyParams = buyParams;
    }
}
