import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.global.Constant;
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.testsmanual.WrapperTestUtils;
import com.nubits.nubot.trading.TradeInterface;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExchangePoloniex extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestExchangePeatio.class
            .getName());

    private static String testconfigFile = "poloniex.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    @Test
    public void testLoadConfig() {
        boolean catched = false;

        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(opt != null);

            assertTrue(opt.getExchangeName().equals("poloniex"));


        } catch (NuBotConfigException e) {
            catched = true;
        }

        assertTrue(!catched);
    }

    @Test
    public void testGetBalance() {

        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }

        Global.options = opt;
        Exchange exc = new Exchange(Global.options.getExchangeName());
        assertTrue(exc != null);
        Global.exchange =exc;
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        CurrencyPair testPair = Constant.NBT_BTC;

        WrapperTestUtils.configExchange(opt.getExchangeName());

        TradeInterface ti = ExchangeFacade.getInterface(Global.exchange);
        assertTrue(ti!=null);
        Global.exchange.setTrade(ti);
        Currency btc = CurrencyList.BTC;
        assertTrue(btc != null);
        ApiResponse balancesResponse = ti.getAvailableBalance(btc);

        if (balancesResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.getBalance() ");
            Balance balance = (Balance) balancesResponse.getResponseObject();

            LOG.info(balance.toString());

            assertTrue(balance.getNubitsBalance().getQuantity() == 0.0);
            assertTrue(balance.getPEGBalance().getQuantity() == 1000.0);

        } else {
            assertTrue(false);
        }
    }
}

