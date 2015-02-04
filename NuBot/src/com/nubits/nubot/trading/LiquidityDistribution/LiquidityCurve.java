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
    private String steepness; //steepness of the curve, low-mid-high
    //Abstract methods
}
