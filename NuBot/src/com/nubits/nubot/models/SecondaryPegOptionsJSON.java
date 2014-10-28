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
package com.nubits.nubot.models;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import java.util.ArrayList;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author advanced
 */
public class SecondaryPegOptionsJSON {

    private static final Logger LOG = Logger.getLogger(SecondaryPegOptionsJSON.class.getName());
    //Compulsory settings ----------------------------
    private String mainFeed;
    private ArrayList<String> backupFeedNames;
    //Optional settings with a default value  ----------------------------
    private long refreshTime;
    private double wallchangeTreshold, priceOffset, distanceTreshold;

    /**
     *
     * @param refreshTime
     * @param wallchangeTreshold
     * @param priceOffset
     * @param distanceTreshold
     * @param mainFeed
     * @param backupFeedNames
     */
    public SecondaryPegOptionsJSON(long refreshTime, double wallchangeTreshold, double priceOffset, double distanceTreshold, String mainFeed, ArrayList<String> backupFeedNames) {
        this.refreshTime = refreshTime;
        this.wallchangeTreshold = wallchangeTreshold;
        this.priceOffset = priceOffset;
        this.distanceTreshold = distanceTreshold;
        this.mainFeed = mainFeed;
        this.backupFeedNames = backupFeedNames;

    }

    /**
     *
     * @param optionsJSON
     * @return
     */
    public static SecondaryPegOptionsJSON create(org.json.JSONObject optionsJSON, CurrencyPair pair) {
        OptionsJSON options = null;
        try {
            //First try to parse compulsory parameters

            String mainFeed = (String) optionsJSON.get("main-feed");


            ArrayList<String> backupFeedNames = new ArrayList<>();
            org.json.JSONObject dataJson = (org.json.JSONObject) optionsJSON.get("backup-feeds");

            //Iterate on backupFeeds

            String names[] = org.json.JSONObject.getNames(dataJson);
            if (names.length < 2) {
                LOG.severe("The bot requires at least two backup data feeds to run");
                System.exit(0);
            }
            for (int i = 0; i < names.length; i++) {
                try {
                    org.json.JSONObject tempJson = dataJson.getJSONObject(names[i]);
                    backupFeedNames.add((String) tempJson.get("name"));
                } catch (JSONException ex) {
                    LOG.severe(ex.toString());
                    System.exit(0);
                }
            }

            //Then parse optional settings. If not use the default value declared here

            //set the refresh time according to the global trading pair
            long refreshTime;
            if (pair.getPaymentCurrency().isFiat()) {
                refreshTime = 8 * 60 * 59 * 1000; //8 hours;
            } else {
                refreshTime = 61;
            }
            double wallchangeTreshold = 3;
            double priceOffset = 0;
            double distanceTreshold = 10;

            if (optionsJSON.has("wallchange-treshold")) {
                wallchangeTreshold = new Double((optionsJSON.get("wallchange-treshold")).toString());
            }

            // TODO : to avoid custodians having the freedom of arbitrarily setting their own price, this parameter will be disabled until further decision is taken

            if (optionsJSON.has("price-offset")) {
                priceOffset = new Double((optionsJSON.get("price-offset")).toString());
            }


            if (optionsJSON.has("price-distance-threshold")) {
                distanceTreshold = new Double((optionsJSON.get("price-distance-threshold")).toString());
            }


            /* ignore the refresh-time parameter to avoid single custodians checking faster than others (causing self-executing orders)
             if (optionsJSON.has("refresh-time")) {
             refreshTime = new Integer((optionsJSON.get("refresh-time")).toString());
             }
             */

            return new SecondaryPegOptionsJSON(refreshTime, wallchangeTreshold, priceOffset, distanceTreshold, mainFeed, backupFeedNames);
        } catch (JSONException ex) {
            LOG.severe(ex.toString());
            System.exit(0);
        }
        return null; //never reached
    }

    /**
     *
     * @return
     */
    public long getRefreshTime() {
        return refreshTime;
    }

    /**
     *
     * @param refreshTime
     */
    public void setRefreshTime(int refreshTime) {
        this.refreshTime = refreshTime;
    }

    /**
     *
     * @return
     */
    public double getWallchangeTreshold() {
        return wallchangeTreshold;
    }

    /**
     *
     * @param wallchangeTreshold
     */
    public void setWallchangeTreshold(double wallchangeTreshold) {
        this.wallchangeTreshold = wallchangeTreshold;
    }

    /**
     *
     * @return
     */
    public double getPriceOffset() {
        return priceOffset;
    }

    /**
     *
     * @param priceOffset
     */
    public void setPriceOffset(double priceOffset) {
        this.priceOffset = priceOffset;
    }

    /**
     *
     * @return
     */
    public double getDistanceTreshold() {
        return distanceTreshold;
    }

    /**
     *
     * @param distanceTreshold
     */
    public void setDistanceTreshold(double distanceTreshold) {
        this.distanceTreshold = distanceTreshold;
    }

    /**
     *
     * @return
     */
    public String getMainFeed() {
        return mainFeed;
    }

    /**
     *
     * @param mainFeed
     */
    public void setMainFeed(String mainFeed) {
        this.mainFeed = mainFeed;
    }

    /**
     *
     * @return
     */
    public ArrayList<String> getBackupFeedNames() {
        return backupFeedNames;
    }

    /**
     *
     * @param backupFeedNames
     */
    public void setBackupFeedNames(ArrayList<String> backupFeedNames) {
        this.backupFeedNames = backupFeedNames;
    }

    @Override
    public String toString() {
        return "SecondaryPegOptionsJSON [" + "backupFeedNames " + backupFeedNames + " " + "distanceTreshold " + distanceTreshold + "mainFeed " + mainFeed + " " + "priceOffset " + priceOffset + " " + "refreshTime " + refreshTime + " " + "wallchangeTreshold " + wallchangeTreshold + "]";
    }

    String toHtmlString() {
        return "SecondaryPegOptionsJSON : <br>" + "backupFeedNames " + backupFeedNames + " <br>" + "distanceTreshold " + distanceTreshold + "<br>" + "mainFeed " + mainFeed + " <br>" + "priceOffset " + priceOffset + " <br>" + "refreshTime " + refreshTime + " <br>" + "wallchangeTreshold " + wallchangeTreshold;
    }
}
