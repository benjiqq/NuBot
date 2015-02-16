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
package com.nubits.nubot.tests;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import junit.framework.TestCase;
import org.junit.Test;

public class TestDateFormat  extends TestCase {

    private static final Logger LOG = Logger.getLogger(TestDateFormat.class
            .getName());

    @Test
    public void testDate() {

        String date = "2014-12-22T17:55:25.107Z";
        String pattern = "yyyy-MM-dd'T'HH:mm:ss";

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            Date d = sdf.parse(date);
            //assert(d.equals("Mon Dec 22 17:55:25 CET 2014"));
        } catch (java.text.ParseException pe1) {
            LOG.severe(pe1.toString());
        }


    }
}
