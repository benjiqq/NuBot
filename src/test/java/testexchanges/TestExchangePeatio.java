package testexchanges;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.models.PairBalance;
import com.nubits.nubot.models.CurrencyList;
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

        CurrencyPair testPair = CurrencyList.NBT_BTC;

        try{
            WrapperTestUtils.configExchange(opt.getExchangeName());
        }catch(NuBotConfigException ex){

        }

        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(testPair);


        if (balancesResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance() ");
            PairBalance balance = (PairBalance) balancesResponse.getResponseObject();

            LOG.info(balance.toString());

            //assertTrue(balance.getNubitsBalance().getQuantity()==0.0);
            //assertTrue(balance.getPEGBalance().getQuantity()==1000.0);

        } else {
            //assertTrue(false);
        }
    }
}
