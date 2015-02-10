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
import static com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurve.STEEPNESS_HIGH;
import static com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurve.STEEPNESS_LOW;
import static com.nubits.nubot.trading.LiquidityDistribution.LiquidityCurve.STEEPNESS_MID;
import com.nubits.nubot.utils.Utils;
import java.util.logging.Logger;

public class LiquidityCurveExp extends LiquidityCurve {

    private static final Logger LOG = Logger.getLogger(LiquidityCurveExp.class.getName());

    public LiquidityCurveExp(String steepness) {
        super(steepness);
    }

    @Override
    double[] computeOrderSize(double[] prices, double wallHeight, String wallType, double wallPrice, double pegPrice) {
        double[] toReturn = new double[prices.length];

        double m = computeCoefficient();

        double increment = m * wallHeight;
        for (int i = 0; i < prices.length; i++) {
            toReturn[i] = Utils.round(wallHeight + ((i + 1) * increment * (i + 1)), 8);
        }

        return toReturn;
    }

    private double computeCoefficient() {
        switch (steepness) {
            case STEEPNESS_HIGH:
                return 0.25;
            case STEEPNESS_MID:
                return 0.17;
            case STEEPNESS_LOW:
                return 0.06;
            default:
                LOG.severe("Not supported steepness : " + steepness);
        }
        return 0;
    }
}
