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

package functions;


import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TestDateFormat extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestDateFormat.class
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
            LOG.error(pe1.toString());
        }


    }

    @Test
    public void testDuration(){

        DateTime a = new DateTime(2015, 2, 2, 7, 0, 0, 0, DateTimeZone.forID("CET"));
        DateTime b = new DateTime(2015, 2, 2, 8, 0, 0, 0, DateTimeZone.forID("CET"));
        String durs = Utils.getDurationDate(a,b);
        System.out.println(durs);
        assertTrue(durs.equals("1 hour 0 minutes"));

        a = new DateTime(2015, 2, 2, 7, 0, 0, 0, DateTimeZone.forID("CET"));
        b = new DateTime(2015, 2, 2, 17, 0, 0, 0, DateTimeZone.forID("CET"));
        durs = Utils.getDurationDate(a,b);
        System.out.println(durs);
        assertTrue(durs.equals("10 hours 0 minutes"));

        a = new DateTime(2015, 2, 2, 7, 0, 0, 0, DateTimeZone.forID("CET"));
        b = new DateTime(2015, 2, 3, 8, 0, 0, 0, DateTimeZone.forID("CET"));
        durs = Utils.getDurationDate(a,b);
        System.out.println(durs);
        assertTrue(durs.equals("1 day 1 hour 0 minutes"));
    }
}
