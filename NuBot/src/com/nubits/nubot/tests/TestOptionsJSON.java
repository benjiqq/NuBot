import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import junit.framework.TestCase;
import org.junit.Test;

public class TestOptionsJSON extends TestCase {

    @Test
    public void testGson(){
        NuBotOptions opt = new NuBotOptions();
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);

    }
}
