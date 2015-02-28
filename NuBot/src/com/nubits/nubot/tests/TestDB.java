import com.nubits.nubot.db.NuDB;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;

public class TestDB extends TestCase {

    @Test
    public void testTestup(){

        try{
            String f = "test.db";
            File ff = new File(f);
            assertTrue(!ff.exists());
            NuDB.createDB(f);
            assertTrue(ff.exists());
            ff.delete();
        }catch(Exception e){
            assertTrue(false);
        }
    }
}
