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
import com.nubits.nubot.utils.Utils;
import java.util.logging.Logger;

public class LiquidityCurveLin extends LiquidityCurve {

    private static final Logger LOG = Logger.getLogger(LiquidityCurveLin.class.getName());

    public LiquidityCurveLin(String steepness) {
        super(steepness);
    }

    @Override
    double[] computeOrderSize(double[] prices, double wallHeight, String wallType, double wallPrice, double pegPrice) {
        double[] toReturn = new double[prices.length];

        double m = computeAngularCoefficient();

        double increment = m * wallHeight;
        for (int i = 0; i < prices.length; i++) {
            toReturn[i] = Utils.round(wallHeight + ((i + 1) * increment), 8);
        }

        return toReturn;
    }

    private double computeAngularCoefficient() {
        switch (steepness) {
            case LiquidityCurve.STEEPNESS_HIGH:
                return 0.5;
            case LiquidityCurve.STEEPNESS_MID:
                return 0.25;
            case LiquidityCurve.STEEPNESS_LOW:
                return 0.1;
            default:
                LOG.severe("Not supported steepness : " + steepness);
        }
        return 0;
    }
}
