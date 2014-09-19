/*
 * Copyright (C) 2014 desrever <desrever at nubits.com>
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
 *
 */
public class Settings {

    public static final String CURRENT_VERSION = "0.1.0";
    //Paths
    public static final String RES_PATH = "res/";  //Path to the folder cointaing resources
    //Logs
    public static final String LOG_PATH = "logs/"; //Folder cointaining the logs
    //SSL Keystore
    public static final String KEYSTORE_PATH = RES_PATH + "ssl/nubot_keystore.jks";
    public static final String KEYSTORE_PWD = "h4rdc0r_";   //password used to encrypt the keystore
    public static final String APP_TITLE = "NuBot";  //Name of the application
}
