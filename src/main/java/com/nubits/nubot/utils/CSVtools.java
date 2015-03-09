package com.nubits.nubot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;


public class CSVtools {

    private static final Logger LOG = LoggerFactory.getLogger(CSVtools.class.getName());

    public static ArrayList<String[]> parseCsvFromPath(File file) {
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
