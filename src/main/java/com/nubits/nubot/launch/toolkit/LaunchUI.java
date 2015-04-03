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

package com.nubits.nubot.launch.toolkit;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class LaunchUI {
    private static final Logger LOG = LoggerFactory.getLogger(LaunchUI.class.getName());

    final static String JAR_FILE = "NuBot.jar"; //Name of jar file
    final static String ARGS = "runui"; //Arguments to pass to CLI
    final static String EXECUTE_JAR = "java -jar"; //Command to launch the jar

    //Compose the launch command
    static String LAUNCH_COMMAND = EXECUTE_JAR + " " + JAR_FILE + " " + ARGS;

    public static void main(String[] args) {
        if (args.length == 0) {
            Runtime rt = Runtime.getRuntime();
            try {
                LOG.info("Launching UI from CLI : $ " + LAUNCH_COMMAND);
                if (SystemUtils.IS_OS_WINDOWS) {
                    //for windows the launch command requires a different syntax
                    LAUNCH_COMMAND = "cmd /c " + LAUNCH_COMMAND;
                }

                Process pr = rt.exec(LAUNCH_COMMAND); //Run
                
                //capture output
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(pr.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(pr.getErrorStream()));

                // readn&print the output from the command
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }

                // readn&print any errors from the attempted command
                while ((s = stdError.readLine()) != null) {
                    System.err.println(s);
                }
            } catch (IOException e) {
                LOG.error(e.toString());
            }
        }
    }
}
