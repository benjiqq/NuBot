package functions;


import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import junit.framework.TestCase;
import org.junit.Test;

public class TestCurrencyPair extends TestCase {

    @Test
    public void testParse(){
        CurrencyPair pair = CurrencyPair.getCurrencyPairFromString("nbt_btc");
        assertTrue(pair!=null);
        assertTrue(pair.getOrderCurrency().equals(CurrencyList.NBT));
        assertTrue(pair.getPaymentCurrency().equals(CurrencyList.BTC));

        assertTrue(pair.toStringSep().equals("nbt_btc"));
    }
}
