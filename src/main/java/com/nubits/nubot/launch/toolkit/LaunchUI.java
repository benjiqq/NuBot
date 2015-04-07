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

import com.nubits.nubot.global.Settings;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private final String ARGS = "-GUI"; //Arguments to pass to CLI to run : java - jar NuBot -cfg=<pathTo/options.json> -GUI
    private final String CFG_PREFIX = "-cfg=";
    private final String EXECUTE_JAR = "java -jar"; //Command to launch the jar

    private final String ICON_PATH = Settings.IMAGE_FOLDER + "/nubot-logo.png";
    private String command = "";
    private String local_path;

    public static void main(String[] args) {
        if (args.length == 0) {
            LaunchUI launch = new LaunchUI();
            launch.start();
        }
    }

    private void start() {
        local_path = System.getProperty("user.dir");
        String configPath = askUser(); //Ask user for path; returns "" if nothing selected

        command = EXECUTE_JAR + " " + JAR_FILE + " ";
        if (!configPath.equals("")) {//User indicated a file
            command += CFG_PREFIX + configPath + " ";
        } else {
            command += CFG_PREFIX + Settings.DEFAULT_CONFIG_FILENAME + " "; //Default config
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

        System.exit(0);
    }

    private String askUser() {
        //Create Options for option dialog
        String path = "";

        // Set cross-platform Java L&F (also called "Metal")
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            final ImageIcon icon = new ImageIcon(local_path + "/" + ICON_PATH);

            //Ask the user to ask for scratch
            Object[] options = {"Import existing JSON option file",
                    "Configure the bot from scratch"};

            int n = JOptionPane.showOptionDialog(new JFrame(),
                    "Chose one of the following options:",
                    "NuBot UI Launcher",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    icon,     //do not use a custom Icon
                    options,  //the titles of buttons
                    options[0]); //default button title

            if (n == JOptionPane.YES_OPTION) {

                //Prompt user to chose a file.
                JFileChooser fileChooser = createFileChoser();

                int result = fileChooser.showOpenDialog(new JFrame());
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    path = selectedFile.getAbsolutePath();
                    LOG.info("Option file selected : " + path);
                }
            }
        } catch (ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
            LOG.error(e.toString());
        }

        return path;
    }

    private JFileChooser createFileChoser() {
        JFileChooser fileChooser = new JFileChooser();

        // Set the text
        fileChooser.setApproveButtonText("Import");
        // Set the tool tip
        fileChooser.setApproveButtonToolTipText("Import configuration file");

        //Filter .json files
        FileFilter filter = new FileNameExtensionFilter(".json files", "json");
        fileChooser.setFileFilter(filter);

        fileChooser.setCurrentDirectory(new File(local_path));
        return fileChooser;
    }
}
