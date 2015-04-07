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

    /**
     * the precision used to round
     */
    public static final int DEFAULT_PRECISION = 8;

    /**
     * [%] Force the a spread to avoid collisions (multi-custodians)
     */
    public static final double FORCED_SPREAD = 0.9;

    // ------ Timing ------
    /**
     * [seconds] Submit liquidity info
     *
     */

    /**
     * [seconds] Execute StrategyTask
     */
    public static final int EXECUTE_STRATEGY_INTERVAL = 41;

    public static final int SUBMIT_LIQUIDITY_SECONDS = 130;  // [seconds] Submit liquidity info

    /**
     * [seconds] Check connection with exchanges API
     */
    public static final int CHECK_CONNECTION_INTERVAL = 127;

    /**
     * [seconds] Check connection with nudaemon
     */
    public static final int CHECK_NUD_INTERVAL = 30;

    /**
     * [seconds]
     */
    public static final int CHECK_PRICE_INTERVAL = 61;

    /**
     * [seconds] ~ 8 hours, used for fiat only
     */
    public static final int CHECK_PRICE_INTERVAL_FIAT = 8 * 60 * 59 * 1000;

    /**
     * [minutes] Used in multi-custodian mode
     */
    public static final int RESET_EVERY_MINUTES = 3;

    /**
     * Timeout for NTP calls
     */
    public static final int NTP_TIMEOUT = 10 * 1000;

    // ------ Paths and filenames
    // Refer to md/FILES-AND-FOLDERS.md for the convention of where to place files and folders
    // In defining folder names, omit the "/" at the end
    //--- Logging ---

    /**
     * the main logging file
     */
    public static final String LOGXML = "config/logging/logback.xml";

    /**
     * the file for configuring logging during test. uses sift to direct tests to different files
     */
    public static final String TEST_LOGXML = "config/logging/test_logback.xml";

    /**
     * configuration file for test launches
     */
    public static final String TEST_LAUNCH_XML = "config/logging/test_launch.xml";

    /**
     * main logs file. also defined in logback.xml
     */
    public final static String LOGS_PATH = "logs";

    /**
     * testlog directory. also defined in test_logback.xml
     */
    public static final String TEST_LOGFOLDER = LOGS_PATH + "/" + "tests";

    /**
     * the relative path for resources
     */
    public final static String RES_PATH = "res";

    public final static String KEYSTORE_PATH = RES_PATH + "/" + "ssl/nubot_keystore.jks";

    /**
     * folder containing resources needed at runtime
     */
    public final static String FROZEN_FUNDS_PATH = RES_PATH + "/" + "frozen-funds/";

    public final static String DEFAULT_CONFIG_FILENAME = "nubot-config.json";//Used by UI in case file not explicitly declared

    public final static String IMAGE_FOLDER = RES_PATH + "/images";

    public final static String SESSION_LOG = "session_";

    public final static String ORDERS_FILENAME = "orders_history"; //Filename for historical snapshot of active orders

    public final static String BALANCES_FILEAME = "balance_history"; //Filename for historical snapshot of balance

    public final static String WALLSHIFTS_FILENAME = "wall_shifts"; //Filename for recording wallshifts

    public final static String TESTS_LOG_PREFIX = "tests"; //Prefix used in naming the directory for saving the output of tests

    public static final String TESTS_CONFIG_PATH = "config/myconfig"; //Directory containing configuration files used in tests

    public final static String CURRENCY_FILE_PATH = RES_PATH + "/" + "currencies.csv";

    public final static String APP_FOLDER = ".nubot";

    public final static String SESSION_FILE = "_session.txt";

    public final static String SESSION_LOGGER_NAME = "SessionLOG";

    /**
     * a utility file which gets created in the distribution folder
     */
    public static String INFO_FILE = ".nubot";
    //TODO via settings
    public static String HTML_FOLDER = "./UI/templates/";
}
