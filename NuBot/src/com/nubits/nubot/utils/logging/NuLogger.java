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
package com.nubits.nubot.utils.logging;

/**
 *
 * @author Vogella
 */
import com.nubits.nubot.global.Settings;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NuLogger {

    static private FileHandler fileCsv;
    static private Formatter formatterCsv;
    static private FileHandler fileHTML;
    static private Formatter formatterHTML;

    static public void setup() throws IOException {

        // get the global logger to configure it
        Logger logger = Logger.getLogger("com.nubits.nubot");

        logger.setLevel(Level.FINE);
        String filename = Settings.APP_TITLE + "_" + new Date().getTime() + "_log";
        fileCsv = new FileHandler(Settings.LOG_PATH + filename + ".csv");
        fileHTML = new FileHandler(Settings.LOG_PATH + filename + ".html");

        // Create csv Formatter

        formatterCsv = new LogFormatterCSV();
        fileCsv.setFormatter(formatterCsv);
        logger.addHandler(fileCsv);

        // Create HTML Formatter
        formatterHTML = new LogFormatterHTML();
        fileHTML.setFormatter(formatterHTML);
        logger.addHandler(fileHTML);
    }
}
