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

import com.nubits.nubot.NTP.NTPClient;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Passwords;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.models.OrderToPlace;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.nubits.nubot.utils.LiquidityPlot.addPlot;
import static com.nubits.nubot.utils.LiquidityPlot.plot;


public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class.getName());

    /**
     * @param originalString
     * @param passphrase
     * @param pathToOutput
     * @return
     */
    public static String encodeToFile(String originalString, String passphrase, String pathToOutput) {
        String encodedString = "";
        MessageDigest digest;
        try {

            //System.out.println("Writing " +originalString +" to "+ pathToOutput +" with \npassphrase = "+passphrase);

            //Encapsule the passphrase in a 16bit SecretKeySpec key
            digest = MessageDigest.getInstance("SHA");
            digest.update(passphrase.getBytes());
            SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

            //Cypher the message
            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.ENCRYPT_MODE, key);

            byte[] ciphertext = aes.doFinal(originalString.getBytes());
            encodedString = new String(ciphertext);

            FileUtils.writeByteArrayToFile(new File(pathToOutput), ciphertext);


        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            LOG.error(ex.toString());
        }

        return encodedString;
    }

    /**
     * @param pathToFile
     * @param passphrase
     * @return
     */
    public static String decode(String pathToFile, String passphrase) {
        String clearString = null;
        MessageDigest digest;
        try {
            //Encapsule the passphrase in a 16bit SecretKeySpec key
            digest = MessageDigest.getInstance("SHA");
            digest.update(passphrase.getBytes());
            SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");

            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.DECRYPT_MODE, key);

            byte[] ciphertextBytes = FileUtils.readFileToByteArray(new File(pathToFile));
            clearString = new String(aes.doFinal(ciphertextBytes));

        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            LOG.error(ex.toString());
            return "-1";
        }
        return clearString;
    }

    // http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }

    /**
     * @return
     */
    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Windows")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return
     */
    public static boolean isMacPlatform() {
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param arg
     * @return
     */
    public static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    /**
     * @return
     */
    public static String getTimestampString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static long getTimestampLong() {
        Date timeStamp = new Date();
        return timeStamp.getTime();

    }

    public static String getHTML(String url, boolean removeNonLatinChars) throws IOException {
        String line = "", all = "";
        URL myUrl = null;
        BufferedReader br = null;
        try {
            myUrl = new URL(url);

            URLConnection con = myUrl.openConnection();
            con.setConnectTimeout(1000 * 8);
            con.setReadTimeout(1000 * 8);
            InputStream in = con.getInputStream();

            br = new BufferedReader(new InputStreamReader(in));

            while ((line = br.readLine()) != null) {
                all += line;
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        if (removeNonLatinChars) {
            all = all.replaceAll("[^\\x00-\\x7F]", "");
        }
        return all;
    }

    public static void printSeparator() {
        LOG.info("\n----------- -----------  -----------\n");
    }


    //When parsing Json with org.json.simple.JSONObject, use this for doubles
    //not needed if using alibaba json parser
    public static double getDouble(Object obj) {
        double toRet = -1;
        if (obj.getClass().equals((new Long((long) 10)).getClass())) //If the price is round (i.e. 100) the type will be parsed as Long
        {
            Long l = new Long((long) obj);
            toRet = l.doubleValue();
        } else {
            try {
                toRet = (Double) obj;
            } catch (ClassCastException e) {
                //probably a String
                try {
                    toRet = Double.parseDouble((String) obj);
                } catch (ClassCastException ex) {
                    LOG.error("cannot parse object : " + obj.toString());
                    return -1;
                }
            }
        }
        return toRet;
    }


    public static long getOneDayInMillis() {
        return 1000 * 60 * 60 * 24;
    }

    //Computes the seconds missing till the next remote minutes clocks
    public static int getSecondsToRemoteMinute() {
        Date remoteDate = new NTPClient().getTime();
        Calendar remoteCalendar = new GregorianCalendar();
        remoteCalendar.setTime(remoteDate);
        int remoteTimeInSeconds = remoteCalendar.get(Calendar.SECOND);
        int delay = (60 - remoteTimeInSeconds);
        return delay;
    }


    /**
     * Returns a pseudo-random number between min and max, inclusive. The
     * difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value. Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * For use with the multi-custodian option. The walls are removed and
     * re-added every three minutes. The bot needs to wait for the next 3 minute
     * window before placing walls
     *
     * @return delay
     */
    public static int getSecondsToNextwindow(int windowWidthSeconds) {
        Date remoteDate = new NTPClient().getTime();
        Calendar remoteCalendar = new GregorianCalendar();
        remoteCalendar.setTime(remoteDate);
        int remoteTimeInSeconds = remoteCalendar.get(Calendar.SECOND);
        int remoteTimeInMinutes = remoteCalendar.get(Calendar.MINUTE);
        int minutesTillWindow = windowWidthSeconds - (remoteTimeInMinutes % windowWidthSeconds);
        int delay = ((60 * minutesTillWindow) - remoteTimeInSeconds);
        return delay;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static String calcDate(long millisecs) {
        String timezone = "UTC";
        SimpleDateFormat date_format = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS");
        date_format.setTimeZone(TimeZone.getTimeZone("timezone"));
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate) + " " + timezone;

    }

    public static String generateSessionID() {
        String sep = "|";
        //Generate a random alfanumeric id of 6 chars
        String uid = UUID.randomUUID().toString().substring(0, 6);
        //Get nubot version
        String version = VersionInfo.getVersionName();
        //Get timestamp
        String timest = "" + getTimestampLong();
        //conatenate
        return version + sep + timest + sep + uid;
    }


    /**
     * Install a trust manager that does not validate certificate chains for https calls
     *
     * @throws Exception
     */
    private static void installTrustAllManager() throws Exception {


        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static void installKeystore(boolean trustAll) {

        LOG.info("installKeystore. trustall: " + trustAll);
        if (trustAll) {
            try {
                Utils.installTrustAllManager();
            } catch (Exception ex) {
                LOG.error(ex.toString());
            }

        } else {

            //load file depending whether run from inside a Jar or not

            String wdir = FilesystemUtils.getBotAbsolutePath();
            String wdirpath = wdir + "/" + Settings.KEYSTORE_PATH;

            LOG.info("Reading keystorefile from : " + wdirpath);

            System.setProperty("javax.net.ssl.trustStore", wdirpath);
            System.setProperty("javax.net.ssl.trustStorePassword", Passwords.KEYSTORE_ENCRYPTION_PASS);
        }
    }

    public static void drawOrderBooks(ArrayList<OrderToPlace> sellOrders, ArrayList<OrderToPlace> buyOrders, double pegPrice) {
        double[] xSell = new double[sellOrders.size()];
        double[] ySell = new double[sellOrders.size()];
        double[] xBuy = new double[buyOrders.size()];
        double[] yBuy = new double[buyOrders.size()];


        for (int i = 0; i < sellOrders.size(); i++) {
            OrderToPlace tempOrder = sellOrders.get(i);
            xSell[i] = tempOrder.getPrice() * pegPrice * 100;
            ySell[i] = tempOrder.getSize();

        }

        for (int i = 0; i < buyOrders.size(); i++) {
            OrderToPlace tempOrder = buyOrders.get(i);
            xBuy[i] = tempOrder.getPrice() * pegPrice * 100;
            yBuy[i] = tempOrder.getSize();

        }

        plot(xSell, ySell); // create a plot using xaxis and yvalues
        addPlot(xBuy, yBuy); // create a second plot on top of first

    }

    //Return the uptime of the bot [hours]
    public static String getBotUptime() {
        long upTimeMs = System.currentTimeMillis() - Global.sessionStarted;
        String toReturn = "";
        if (getDaysFromMillis(upTimeMs) > 2) {
            toReturn = getDaysFromMillis(upTimeMs) + " days";
        } else if (getHoursFromMillis(upTimeMs) > 2) {
            toReturn = getHoursFromMillis(upTimeMs) + " hours";
        } else {
            toReturn = getMinutesFromMillis(upTimeMs) + " minutes";
        }
        return toReturn;
    }

    public static double getHoursFromMillis(long millis) {
        return round((getMinutesFromMillis(millis)) / 60, 2);
    }

    public static double getMinutesFromMillis(long millis) {
        return round(((millis / 1000) / 60), 2);
    }

    public static double getDaysFromMillis(long millis) {
        return round((getHoursFromMillis(millis)) / 24, 2);
    }

}
