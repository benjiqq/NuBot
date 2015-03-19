package functions;

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

import com.nubits.nubot.NTP.NTPClient;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;

/**
 * Created by benjamin on 10/03/2015.
 */
public class TestNTP extends TestCase {

    @Test
    public void testok(){
        NTPClient client = new NTPClient();

         int w = Utils.getSecondsToNextwindow(3);
        assertTrue(w!=0);
        System.out.println(w);
        assertTrue(w<500);

        //Try multiple servers
         Date m = client.getTime();
        long diff = m.getTime() - System.currentTimeMillis();
        assertTrue(diff <1000);

        //Try single server
        Date n= client.getTime("time.nist.gov");
        diff = m.getTime() - System.currentTimeMillis();
        assertTrue(diff <1000);

    }
}
