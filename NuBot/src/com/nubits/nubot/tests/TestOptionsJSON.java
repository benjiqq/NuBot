import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.NuBotOptionsDefault;
import com.nubits.nubot.options.NuBotOptionsSerializer;
import junit.framework.TestCase;
import org.junit.Test;

public class TestOptionsJSON extends TestCase {

    @Test
    public void testGson(){
        NuBotOptions opt = NuBotOptionsDefault.defaultFactory();
        assertTrue(opt != null);
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        assertTrue(parser != null);
        try{
            String js = parser.toJson(opt);
        } catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }

    }
}
