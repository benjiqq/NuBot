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
import java.util.logging.Logger;

public class LiquidityCurveExp extends LiquidityCurve {

    private static final Logger LOG = Logger.getLogger(LiquidityCurveExp.class.getName());

    public LiquidityCurveExp(String steepness) {
        super(steepness);
    }

    @Override
    double[] computeOrderSize(double[] prices, double wallHeight, String wallType, double wallPrice) {
        throw new UnsupportedOperationException("LiquidityCurveExp.computeOrderSize() not implemented yet.");
    }
}
