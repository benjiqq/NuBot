
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

import com.nubits.nubot.testsmanual.SSLConnectionTest;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;


public class TestSSL extends TestCase {


    @Test
    public void testPolo() {


        Utils.installKeystore(false);

        String API_BASE_URL = "poloniex.com"; //"https://poloniex.com/tradingApi";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }

    @Test
    public void testBitspark() {


        Utils.installKeystore(false);

        String API_BASE_URL = "bitspark.io";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }

    @Test
    public void testAlts() {


        Utils.installKeystore(false);

            String API_BASE_URL = "alts.trade";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }

    @Test
    public void testCoinbase() {

        Utils.installKeystore(false);

        String API_BASE_URL = "coinbase.com";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }

    @Test
    public void testHipchat() {

        Utils.installKeystore(false);

        String API_BASE_URL = "hipchat.com";

        boolean success = SSLConnectionTest.connectionTest(API_BASE_URL, 443);
        assertTrue(success);

    }


}