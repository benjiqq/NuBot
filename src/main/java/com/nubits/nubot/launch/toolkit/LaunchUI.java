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

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This should be packed into a Binary jar that launched nubot with the UI.
 * Associated gradle task LaunchUIjar
 */
public class LaunchUI {
    private static final Logger LOG = LoggerFactory.getLogger(LaunchUI.class.getName());

    private final String JAR_FILE = "NuBot.jar"; //Name of jar file
    private String ARGS = "--ui=true"; //Arguments to pass to CLI
    private String EXECUTE_JAR = "java -jar"; //Command to launch the jar

    private String command = "";

    public static void main(String[] args) {
        if (args.length == 0) {
            LaunchUI launch = new LaunchUI();
            launch.start();
        }
    }

    private void start() {
        String configPath = askUser(); //Ask user for path;

        command = EXECUTE_JAR + " " + JAR_FILE + " ";
        if (!configPath.equals("")) {
            command += configPath + " ";
        }
        command += ARGS;

        Runtime rt = Runtime.getRuntime();
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                //for windows the launch command requires a different syntax
                command = "cmd /c " + command;
            }

            LOG.info("Launching UI from CLI : $ " + command);
            Process pr = rt.exec(command); //Run

            //capture output
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(pr.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(pr.getErrorStream()));

            // readn&print the output from the command
            String output = "";
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                output += s + "\n";
            }
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    private String askUser() {
        //Prompt user for a file. Default return ""
        String path = "";
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = fileChooser.showOpenDialog(new JFrame());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            path = selectedFile.getAbsolutePath();
            LOG.info("Option file selected : " + path);
        }
        return path;
    }
}
