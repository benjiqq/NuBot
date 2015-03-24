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

package com.nubits.nubot.global;

import com.nubits.nubot.bot.Global;

/**
 * This class contains a list of settings that can be configured by developers
 * Settings are grouped and commented.
 */
public class Settings {

    /**
     * Misc
     */
    public final static String APP_NAME = "NuBot";

    public static final int DEFAULT_PRECISION = 8 ; //Used to round

    public static final double FORCED_SPREAD = 0.9 ; //[%] Force the a spread to avoid collisions (multi-custodians)

    /**
     * Timing
     */

    public static final int SUBMIT_LIQUIDITY_SECONDS = 130;  // [seconds] Submit liquidity info
    public static final int EXECUTE_STRATEGY_INTERVAL = 41;  // [seconds)Execute StrategyTask
    public static final int CHECK_CONNECTION_INTERVAL = 127; //[seconds) Check connection with exchanges API
    public static final int CHECK_NUD_INTERVAL = 30; //[seconds)Check connection with nudaemon

    public static final int CHECK_PRICE_INTERVAL = 61; //[seconds]
    public static final int CHECK_PRICE_INTERVAL_FIAT = 8 * 60 * 59 * 1000 ; //[seconds] ~ 8 hours, used for fiat only

    public static final int RESET_EVERY_MINUTES = 3; //[minutes] Used in multi-custodian mode

    public static final int NTP_TIMEOUT = 10 * 1000; //Timeout for NTP calls

    /**
     * Paths and filenames
     */
    //Logging
    public final static String LOGS_PATH = "logs/"; //name of the log folder
    public final static String CURRENT_SESSION_FILENAME = "session.log";

    public final static String FROZEN_PATH = "frozen/";

    public final static String PAST_LOGS_FOLDER = "pastsession/"; //!!!Change this? Update the logback.xml //TODO load value from xml?
    public final static String CURRENT_LOGS_FOLDER = "current/"; ///!!!Change this? Update the logback.xml //TODO load value from xml?

    public final static String DEFAULT_HTML_LOG_FILENAME = "standard-output";    //!!!Change this? Update the logback.xml //TODO load value from xml?
    public final static String VERBOSE_HTML_LOG_FILENAME = "verbose-output";   //!!!Change this? Update the logback.xml //TODO not being used in the code

    public final static String ORDERS_FILENAME  = "orders_history"; //Filename for historical snapshot of active orders
    public final static String BALANCES_FILEAME = "balance_history"; //Filename for historical snapshot of balance

    public final static String TESTS_LOG_PREFIX = "tests"; //Prefix used in naming the directory for saving the output of tests

    public static final String TESTS_CONFIG_PATH = "config/myconfig"; //Directory containing configuration files used in tests
}
