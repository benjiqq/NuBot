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

package tasks;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.tasks.BotTask;
import com.nubits.nubot.tasks.CheckConnectionTask;
import com.nubits.nubot.trading.TradeInterface;
import junit.framework.TestCase;
import org.junit.Test;


public class TestTasksConnectivity extends TestCase {

    @Test
    public void test() {

        Global.exchange = new Exchange(ExchangeFacade.BTCE);
        TradeInterface ti = ExchangeFacade.getInterfaceByName(ExchangeFacade.BTCE);
        Global.exchange.getLiveData().setUrlConnectionCheck(ti.getUrlConnectionCheck());

        BotTask checkConnectionTask = new BotTask(
                new CheckConnectionTask(), 127, "checkConnection");
        boolean r = checkConnectionTask.isRunning();
        assertTrue(!r);

        checkConnectionTask.start();
        try{
            Thread.sleep(1000);
        }catch(Exception e){

        }

        CheckConnectionTask t =(CheckConnectionTask)checkConnectionTask.getTask();
        boolean isconnected = t.isConnected();
        assertTrue(isconnected);


        checkConnectionTask.stop();
    }



}
