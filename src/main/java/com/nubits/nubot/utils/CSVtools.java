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

package com.nubits.nubot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;


public class CSVtools {

    private static final Logger LOG = LoggerFactory.getLogger(CSVtools.class.getName());

    /**
     * parse csv from a file that is on the classpath
     * @param filename
     * @return
     */
    /*public static ArrayList<String[]> parseCsvFromClassPath(String filename) {
        InputStream is = CSVtools.class.getClassLoader().getResourceAsStream(filename);

        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String[]> toReturn = new ArrayList<>();
        try {
            br= new BufferedReader(new InputStreamReader(is));

            //br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] tempLine = line.split(cvsSplitBy);
                toReturn.add(tempLine);

            }

        } catch (FileNotFoundException e) {
            LOG.error(e.toString());
        } catch (IOException e) {
            LOG.error(e.toString());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error(e.toString());
                }
            }
        }

        return toReturn;
    }*/

    /**
     * parse a csv file. used by parseCsvFromClassPath to load from classhpath
     *
     * @param file
     * @return
     */
    public static ArrayList<String[]> parseCsvFromFile(String filepath) {
        File file = new File(filepath);
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        ArrayList<String[]> toReturn = new ArrayList<>();
        try {

            br = new BufferedReader(new FileReader(file));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] tempLine = line.split(cvsSplitBy);
                toReturn.add(tempLine);

            }

        } catch (FileNotFoundException e) {
            LOG.error(e.toString());
        } catch (IOException e) {
            LOG.error(e.toString());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error(e.toString());
                }
            }
        }

        return toReturn;
    }
}
