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


import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.models.PairBalance;
import com.nubits.nubot.trading.wrappers.SandboxWrapper;
import junit.framework.TestCase;
import org.junit.Test;

public class TestSandbox extends TestCase {

    @Test
    public void testBalance() {
        SandboxWrapper wrapper = new SandboxWrapper();
        CurrencyPair pair = new CurrencyPair(CurrencyList.NBT, CurrencyList.BTC);
        ApiResponse resp = wrapper.getAvailableBalances(pair);
        assertTrue(resp != null);
        PairBalance pb = (PairBalance)resp.getResponseObject();
        assertTrue(pb != null);
        assertTrue(pb.getNBTAvailable().getQuantity()==0);
    }
}
