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

import com.nubits.nubot.bot.SessionManager;
import com.nubits.nubot.launch.MainLaunch;
import junit.framework.TestCase;
import org.junit.Test;

import javax.mail.Session;

/**
 * test NuBot in simulation
 */
public class TestSimulation extends TestCase {

    static String configFile = "config/simulation.json";

    @Test
    public void testSimple() {

        String[] args2 = {"-cfg=" + configFile}; //, "-GUI"};
        MainLaunch.main(args2);

        try{
            Thread.sleep(10 * 1000);
        }catch (Exception e){

        }

        //assertTrue(SessionManager.isSessionActive());
        //assertTrue(SessionManager.)

    }


}
