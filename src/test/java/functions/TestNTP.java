package functions;

import com.nubits.nubot.NTP.NTPClient;
import com.nubits.nubot.utils.Utils;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Date;

/**
 * Created by benjamin on 10/03/2015.
 */
public class TestNTP extends TestCase {

    @Test
    public void testok(){
        NTPClient client = new NTPClient();

         int w = Utils.getSecondsToNextwindow(3);
        assertTrue(w!=0);
        System.out.println(w);
        assertTrue(w<500);

        //Try multiple servers
         Date m = client.getTime();
        long diff = m.getTime() - System.currentTimeMillis();
        assertTrue(diff <1000);

        //Try single server
        Date n= client.getTime("time.nist.gov");
        diff = m.getTime() - System.currentTimeMillis();
        assertTrue(diff <1000);

    }
}
