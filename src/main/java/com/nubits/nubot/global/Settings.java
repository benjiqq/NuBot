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
     * Refer to md/FILES-AND-FOLDERS.md for the convention of where to place files and folders
     */

    //--- Logging ---

    /**
     * the main logging file
      */
    public static final String LOGXML = "config/logging/logback.xml";

    public static final String TEST_LOGXML = "config/logging/test_logback.xml";

    public final static String LOGS_PATH = "logs/"; //name of the log folder

    public final static String RES_PATH = "res/";

    public final static String KEYSTORE_PATH = RES_PATH + "ssl/nubot_keystore.jks";

    public final static String FROZEN_FUNDS_PATH =  RES_PATH + "frozen-funds/"; //folder containing resources needed at runtime

    public final static String PAST_LOGS_FOLDER = "pastsession/"; //!!!Change this? Update the logback.xml //TODO load value from xml?
    public final static String CURRENT_LOGS_FOLDER = "current/"; ///!!!Change this? Update the logback.xml //TODO load value from xml?

    public final static String ORDERS_FILENAME  = "orders_history"; //Filename for historical snapshot of active orders
    public final static String BALANCES_FILEAME = "balance_history"; //Filename for historical snapshot of balance

    public final static String TESTS_LOG_PREFIX = "tests"; //Prefix used in naming the directory for saving the output of tests

    public static final String TESTS_CONFIG_PATH = "config/myconfig"; //Directory containing configuration files used in tests


    public static final String TEST_LOGFOLDER = "testlog"; //same as in test_logback.xml


}
