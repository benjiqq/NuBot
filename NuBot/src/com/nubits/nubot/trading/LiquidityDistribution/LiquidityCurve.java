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

public abstract class LiquidityCurve {

    private static final Logger LOG = Logger.getLogger(LiquidityCurve.class.getName());
    public static final String STEEPNESS_LOW = "s_low";
    public static final String STEEPNESS_MID = "s_mid";
    public static final String STEEPNESS_HIGH = "s_high";
    public static final String TYPE_LIN = "t_lin";
    public static final String TYPE_EXP = "t_exp";
    public static final String TYPE_LOG = "t_log";
    private String type; //type of model, lin-log-exp
    protected String steepness; //steepness of the curve, low-mid-high
    //Abstract methods

    public LiquidityCurve(String steepness) {
        if (steepness.equals(STEEPNESS_LOW) || steepness.equals(STEEPNESS_MID) || steepness.equals(STEEPNESS_HIGH)) {
            this.steepness = steepness;
        } else {
            LOG.severe("Value not accepted for steepness : " + steepness);

        }
    }

    abstract double[] computeOrderSize(double[] prices, double wallHeight, String wallType, double startPrice, double pegPrice);
}
