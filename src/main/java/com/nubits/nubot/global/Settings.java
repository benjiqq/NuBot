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

    //Misc ------
    public final static String APP_NAME = "NuBot";

    //Paths and Filenames ------

    //Logging
    public final static String LOGS_PATH = "logs/"; //name of the log folder
    public final static String CURRENT_SESSION_FILENAME = "session.log";

    public final static String PAST_LOGS_FOLDER = "pastsession/";
    public final static String CURRENT_LOGS_FOLDER = "current/";

    public final static String DEFAULT_HTML_LOG_FILENAME = "standard-output";    //Change this? Update the logback.xml
    public final static String VERBOSE_HTML_LOG_FILENAME = "verbose-output";   //Change this? Update the logback.xml




}
