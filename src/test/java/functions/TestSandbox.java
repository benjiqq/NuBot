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


import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.models.*;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.SimulationWrapper;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;

public class TestSandbox extends TestCase {

    @Test
    public void testAll() {
        SimulationWrapper wrapper = new SimulationWrapper(new ApiKeys("", ""), new Exchange("Simulation"));
        CurrencyPair pair = new CurrencyPair(CurrencyList.NBT, CurrencyList.BTC);
        ApiResponse resp = wrapper.getAvailableBalances(pair);
        assertTrue(resp != null);
        PairBalance pb = (PairBalance) resp.getResponseObject();
        assertTrue(pb != null);
        assertTrue(pb.getNBTAvailable().getQuantity() == 0);

        ArrayList<Order> ao = (ArrayList<Order>) wrapper.getActiveOrders().getResponseObject();
        assertTrue(ao.size() == 1);

        ApiResponse resp2 = wrapper.getTxFee();
        double d = (Double)resp2.getResponseObject();
        assertTrue(d==0);
    }
}
