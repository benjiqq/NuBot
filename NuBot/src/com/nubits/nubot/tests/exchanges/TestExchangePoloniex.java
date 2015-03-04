package exchanges;

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
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;

public class TestExchangePoloniex extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestExchangePoloniex.class
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
        Utils.loadProperties("settings.properties");

        Global.options = opt;
        Exchange exc = new Exchange(Global.options.getExchangeName());
        assertTrue(exc != null);
        Global.exchange =exc;
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());

        Global.exchange = new Exchange(Global.options.getExchangeName());

        CurrencyPair testPair = Constant.NBT_BTC;

        WrapperTestUtils.configExchange(opt.getExchangeName());

        TradeInterface ti = ExchangeFacade.getInterface(Global.exchange);
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);

        assertTrue(ti!=null);
        Global.exchange.setTrade(ti);
        Currency btc = CurrencyList.BTC;
        assertTrue(btc != null);
        long start = System.currentTimeMillis();
        ApiResponse balancesResponse = ti.getAvailableBalance(btc);
        long stop = System.currentTimeMillis();
        long delta = stop - start;
        System.out.println("delta " + delta);

        if (balancesResponse.isPositive()) {
            LOG.info("Positive response  from TradeInterface.getBalance() ");
            Object o = balancesResponse.getResponseObject();
            LOG.info("response " + o);
            try{
                Amount a = (Amount) o;
                assertTrue(a.getQuantity()>=0);
            }catch(Exception e){
                assertTrue(false);
            }
            //Balance balance = (Balance) o;

            //LOG.info(balance.toStringSep());

            //assertTrue(balance.getNubitsBalance().getQuantity() == 0.0);
            //assertTrue(balance.getPEGBalance().getQuantity() == 1000.0);

        } else {
            assertTrue(false);
        }
    }

    @Test
    public void testMakeOrder() {

        NuBotOptions opt = null;
        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }
        Utils.loadProperties("settings.properties");

        Global.options = opt;
        Exchange exc = new Exchange(Global.options.getExchangeName());
        assertTrue(exc != null);
        Global.exchange =exc;
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());

        Global.exchange = new Exchange(Global.options.getExchangeName());

        CurrencyPair testPair = Constant.NBT_BTC;

        WrapperTestUtils.configExchange(opt.getExchangeName());

        TradeInterface ti = ExchangeFacade.getInterface(Global.exchange);
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);
        Global.exchange.setTrade(ti);
        Currency btc = CurrencyList.BTC;

        double tinyQty = 0.0000001;
        double tinyPrice = 0.0000001;
        ApiResponse orderresponse = ti.buy(testPair, tinyQty , tinyPrice);
        //should raise  {"error":"Total must be at least 0.0001."}
        assertTrue(!orderresponse.isPositive());

        double minimialQty = 1;
        double minimalPrice = 0.0001;
        ApiResponse orderresponse2 = ti.buy(testPair, minimialQty, minimalPrice);
        //should raise  {"error":"Total must be at least 0.0001."}
        assertTrue(orderresponse2.isPositive());

        if (orderresponse2.isPositive()) {
            LOG.info("Positive response  from TradeInterface.getBalance() ");
            Object o = orderresponse.getResponseObject();
            LOG.info("response " + o);

        } else {
            assertTrue(false);
        }

        ApiResponse resp = ti.getActiveOrders();
        assertTrue(resp.isPositive());

        Object o = resp.getResponseObject();
        ArrayList<Order> orderList = (ArrayList<Order>)o;

        assertTrue(orderList.size()>0);

        Order issued = orderList.get(0);

        //cancel all
        Iterator<Order> it = orderList.iterator();
        while(it.hasNext()){
            Order order = it.next();
            ApiResponse cancelresp = ti.cancelOrder(order.getId(), testPair);
            boolean success = ((Boolean)cancelresp.getResponseObject()).booleanValue();
            assertTrue(success);
        }

        try{
            Thread.sleep(1000);
        }catch(Exception e){

        }

        ApiResponse resp2 = ti.getActiveOrders();
        assertTrue(resp2.isPositive());

        Object o2 = resp2.getResponseObject();
        ArrayList<Order> orderList2 = (ArrayList<Order>)o2;

        assertTrue(orderList2.size()==0);

    }
}

