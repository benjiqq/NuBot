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

package com.nubits.nubot.trading;


import com.nubits.nubot.bot.Global;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.TreeMap;

public class TradeUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TradeUtils.class.getName());


    public static double getSellPrice(double txFee) {
        if (Global.options.isDualSide()) {
            return 1 + (0.01 * txFee);
        } else {
            return 1 + (0.01 * txFee) + Global.options.getPriceIncrement();
        }

    }

    public static double getBuyPrice(double txFeeUSDNTB) {
        return 1 - (0.01 * txFeeUSDNTB);
    }

    /**
     * Build the query string given a set of query parameters
     *
     * @param args
     * @param encoding
     * @return
     */
    public static String buildQueryString(AbstractMap<String, String> args, String encoding) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) {
                result += '&';
            }
            try {
                result += URLEncoder.encode(hashkey, encoding) + "="
                        + URLEncoder.encode(args.get(hashkey), encoding);
            } catch (Exception ex) {
                LOG.error(ex.toString());
            }
        }
        return result;
    }

    public static String buildQueryString(TreeMap<String, String> args, String encoding) {
        String result = new String();
        for (String hashkey : args.keySet()) {
            if (result.length() > 0) {
                result += '&';
            }
            try {
                result += URLEncoder.encode(hashkey, encoding) + "="
                        + URLEncoder.encode(args.get(hashkey), encoding);
            } catch (Exception ex) {
                LOG.error(ex.toString());
            }
        }
        return result;
    }


    public static String signRequest(String secret, String hash_data, String hashfFunction, String encoding) {

        String sign = "";
        try {
            Mac mac;
            SecretKeySpec key;
            // Create a new secret key
            key = new SecretKeySpec(secret.getBytes(encoding), hashfFunction);
            // Create a new mac
            mac = Mac.getInstance(hashfFunction);
            // Init mac with key.
            mac.init(key);
            sign = Hex.encodeHexString(mac.doFinal(hash_data.getBytes(encoding)));
        } catch (UnsupportedEncodingException uee) {
            LOG.error("Unsupported encoding exception: " + uee.toString());
        } catch (NoSuchAlgorithmException nsae) {
            LOG.error("No such algorithm exception: " + nsae.toString());
        } catch (InvalidKeyException ike) {
            LOG.error("Invalid key exception: " + ike.toString());
        }
        return sign;
    }

}
