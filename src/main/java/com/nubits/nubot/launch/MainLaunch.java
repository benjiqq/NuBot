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

package com.nubits.nubot.launch;

import com.nubits.nubot.global.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;


/**
 * the main launcher class. either start bot through commandline
 * or a GUI is launched where user starts the Bot himself
 */
public class MainLaunch {

    static {
        System.setProperty("logback.configurationFile", Settings.LOGXML);
    }

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static boolean runui = false;

    //private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json> [runui]";
    private static final String USAGE_STRING = "java - jar NuBot <path/to/options.json>";

    private static File file;
    private static FileChannel channel;
    private static FileLock lock;

    /**
     * Start the NuBot. start if config is valid and other instance is running
     *
     * @param args a list of valid arguments
     */
    public static void main(String args[]) {

        boolean isActive = isAppActive();
        if (isActive)
            System.out.println("NuBot is already running");

        if (args.length != 1) {
            exitWithNotice("wrong argument number : run nubot with \n" + USAGE_STRING);
        }

        String configfile = args[0];

        SessionManager.mainLaunch(configfile, false);

    }

    public static boolean isAppActive() {
        try {
            String refFolder = System.getProperty("user.home"); //wdir

            file = new File
                    (refFolder, Settings.APP_NAME+ ".tmp");
            System.out.println("checking " + refFolder + " " + file.exists());
            channel = new RandomAccessFile(file, "rw").getChannel();

            try {
                lock = channel.tryLock();
            }
            catch (OverlappingFileLockException e) {
                // already locked
                closeLock();
                return true;
            }

            if (lock == null) {
                closeLock();
                return true;
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {
                // destroy the lock when the JVM is closing
                public void run() {
                    closeLock();
                    deleteFile();
                }
            });
            return false;
        }
        catch (Exception e) {
            closeLock();
            return true;
        }
    }

    private static void closeLock() {
        try { lock.release();  }
        catch (Exception e) {  }
        try { channel.close(); }
        catch (Exception e) {  }
    }

    private static void deleteFile() {
        try { file.delete(); }
        catch (Exception e) { }
    }

    /**
     * exit application and notify user
     *
     * @param msg
     */
    public static void exitWithNotice(String msg) {
        LOG.error(msg);
        System.exit(0);
    }




}

