/*
 * Copyright (C) 2014-2015 Nu Development Team
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
package functions;

/**
 *
 *
 */

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;

public class TestSettings extends TestCase {


    private static String testconfigFile = "test.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    @Override
    public void setUp() {
        Utils.loadProperties("settings.properties");
    }

    @Test
    public void testRead() {
        Utils.loadProperties("settings.properties");
        //Global.settings.getProperty("submit_liquidity_seconds"));
        assertTrue(Global.settings.containsKey("version"));
    }


}
