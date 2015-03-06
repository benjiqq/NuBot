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
