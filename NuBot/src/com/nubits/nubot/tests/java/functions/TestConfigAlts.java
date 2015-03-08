package functions;

import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestConfigAlts extends TestCase {

    private static String testconfigFile = "alts.json";
    private static String testconfig = "testconfig/" + testconfigFile;

    @Override
    public void setUp() {
        Utils.loadProperties("settings.properties");
    }

    @Test
    public void testConfigExists() {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        final String wdir = System.getProperty("user.dir");

        File f = new File(testconfig);
        assertTrue(f.exists());
    }

    @Test
    public void testLoadComplete() {

        String testconfigFile = "alts.json";
        String testconfig = "testconfig/" + testconfigFile;
        boolean catched = false;

        try {
            JSONObject inputJSON = ParseOptions.parseSingleJsonFile(testconfig);
            assertTrue(inputJSON.containsKey("exchangename"));
        } catch (Exception e) {

        }
        assertTrue(!catched);
    }

    @Test
    public void testLoadConfig() {
        try {
            NuBotOptions nuo = ParseOptions
                    .parseOptionsSingle(testconfig);

            assertTrue(nuo != null);
            String exc = nuo.getExchangeName();
            assertTrue(exc.equals("altstrade"));

        } catch (NuBotConfigException e) {
            e.printStackTrace();
        }
    }


}