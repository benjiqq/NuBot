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
import com.nubits.nubot.models.OrderToPlace;
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

    public ArrayList<OrderToPlace> getOrdersToPlace(String type, Amount fundsAvailable) {
        if (type.equals(Constant.SELL)) {
            return getOrdersToPlaceImpl(this.sellParams, fundsAvailable);
        } else {
            return getOrdersToPlaceImpl(this.buyParams, fundsAvailable);

        }
    }

    //TODO implement this
    private ArrayList<OrderToPlace> getOrdersToPlaceImpl(ModelParameters params, Amount funds) {
        ArrayList<OrderToPlace> toReturn = new ArrayList();

        return toReturn;
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
