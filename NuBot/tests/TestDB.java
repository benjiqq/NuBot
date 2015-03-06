//import com.nubits.nubot.db.NuDB;
//import junit.framework.TestCase;
//import org.junit.Test;
//
//import java.io.File;
//
//public class TestDB extends TestCase {
//
//    @Test
//    public void testCreate() {
//
//        String f = "test.db";
//        File ff = new File(f);
//
//        if (!ff.exists()) {
//            try {
//
//                NuDB.createDB(f);
//                assertTrue(ff.exists());
//                ff.delete();
//            } catch (Exception e) {
//                assertTrue(false);
//            }
//        }
//    }
//
//    @Test
//    public void testWrite() {
//
//        String f = "test.db";
//        File ff = new File(f);
//        if (ff.exists()) {
//            ff.delete();
//        }
//
//        try {
//            NuDB.createDB(f);
//        } catch (Exception e) {
//            assertTrue(false);
//        }
//
//        try {
//
//            NuDB.writeTo("12345678".getBytes());
//            byte[] b = NuDB.readNbytes(8);
//            String s = new String(b);
//            assertTrue(s.equals("12345678"));
//
//            NuDB.writeTo("abcdefgh".getBytes());
//            byte[] a = NuDB.readAll();
//            String sa = new String(a);
//            assertTrue(sa.equals("12345678abcdefgh"));
//
//            //ff.delete();
//            //assertTrue(!ff.exists());
//        } catch (Exception e) {
//            System.out.println(e);
//            assertTrue(false);
//        }
//
//
//    }
//}
