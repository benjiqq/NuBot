package bot;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.launch.MainLaunch;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import junit.framework.TestCase;
import org.junit.Test;


public class TestAll extends TestCase {

    @Test
    public void testNu(){
        NuBotOptions opt = null;
        String testconfigFile = "poloniex.json";
        String testconfig = "testconfig/" + testconfigFile;

        try {
            opt = ParseOptions
                    .parseOptionsSingle(testconfig);

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }

        opt.executeOrders = false;

        MainLaunch.executeBot(opt);
    }
}
