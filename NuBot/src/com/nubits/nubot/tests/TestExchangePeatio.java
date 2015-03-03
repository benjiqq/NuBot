import com.nubits.nubot.global.Constant;
import com.nubits.nubot.global.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.Balance;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.testsmanual.WrapperTestUtils;
import junit.framework.TestCase;
import org.junit.Test;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TestExchangePeatio extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestExchangePeatio.class
            .getName());

    private static String testconfigFile = "peatio.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    @Test
    public void testPing() {

        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(opt != null);

            assertTrue(opt.getExchangeName().equals("peatio"));

            //assertTrue(nuo.getPair() != null);

            //assertTrue(nuo.getSecondaryPegOptions() != null);
            //.getSpread())

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }

        Global.options = opt;

        CurrencyPair testPair = Constant.NBT_BTC;

        WrapperTestUtils.configExchange(opt.getExchangeName());

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(testPair);


        if (balancesResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance() ");
            Balance balance = (Balance) balancesResponse.getResponseObject();

            LOG.info(balance.toString());

            assertTrue(balance.getNubitsBalance().getQuantity()==0.0);
            assertTrue(balance.getPEGBalance().getQuantity()==1000.0);

        } else {
            assertTrue(false);
        }
    }
}
