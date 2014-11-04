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
package com.nubits.nubot.utils;

import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.launch.NuBot;
import com.nubits.nubot.models.CurrencyPair;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author desrever < desrever@nubits.com >
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    /**
     *
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
            LOG.severe(ex.toString());
        }

        return encodedString;
    }

    /**
     *
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
            LOG.severe(ex.toString());
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
     *
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
     *
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
     *
     * @param arg
     * @return
     */
    public static String toHex(String arg) {
        return String.format("%040x", new BigInteger(1, arg.getBytes(/*YOUR_CHARSET?*/)));
    }

    /**
     *
     * @return
     */
    public static String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String getHTML(String url, boolean removeNonLatinChars) throws IOException {
        String line = "", all = "";
        URL myUrl = null;
        BufferedReader in = null;
        try {
            myUrl = new URL(url);
            in = new BufferedReader(new InputStreamReader(myUrl.openStream(), "UTF-8"));

            while ((line = in.readLine()) != null) {
                all += line;
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }

        if (removeNonLatinChars) {
            all = all.replaceAll("[^\\x00-\\x7F]", "");
        }
        return all;
    }

    public static void printSeparator() {
        LOG.fine("\n----------- -----------  -----------\n");
    }

    public static boolean isSupported(CurrencyPair pair) {
        if (pair.equals(Constant.NBT_USD)
                || pair.equals(Constant.BTC_CNY)//TODO this is only for testing purposes on our internal exchange
                || pair.equals(Constant.NBT_BTC)
                || pair.equals(Constant.BTC_NBT)
                || pair.equals(Constant.NBT_EUR)
                || pair.equals(Constant.NBT_CNY)
                || pair.equals(Constant.NBT_PPC)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean requiresSecondaryPegStrategy(CurrencyPair pair) {
        //Return TRUE when it requires a dedicated NBT peg to something that is not USD
        if (pair.equals(Constant.NBT_USD)
                || pair.equals(Constant.BTC_CNY)) { //TODO this is only for testing purposes on our internal exchange
            return false;
        } else {
            return true;
        }
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
                    LOG.severe("cannot parse object : " + obj.toString());
                    return -1;
                }
            }
        }
        return toRet;
    }

    public static Properties loadProperties(String filename) {
        Global.settings = new Properties();
        InputStream input = null;

        try {

            input = NuBot.class.getClassLoader().getResourceAsStream(filename);

            if (input == null) {
                LOG.severe("Sorry, unable to find " + filename);
                System.exit(0);
            }

            //load a properties file from class path, inside static method
            Global.settings.load(input);
        } catch (IOException ex) {
            LOG.severe(ex.toString());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOG.severe(e.toString());
                }
            }
        }
        return null;
    }

    public static long getOneDayInMillis() {
        return 1000 * 60 * 60 * 24;
    }
}
