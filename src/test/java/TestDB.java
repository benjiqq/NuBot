import com.nubits.nubot.db.RecordReader;
import com.nubits.nubot.db.RecordWriter;
import com.nubits.nubot.db.RecordsFile;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.util.Date;


public class TestDB extends TestCase {

    static void log(String s) {
        System.out.println(s);
    }

    @Test
    public void testAll() {

        log("creating records file...");
        String fp ="sampleFile.records";
        File f = new File(fp);
        if (f.exists())
            f.delete();

        try {
            RecordsFile recordsFile = new RecordsFile(fp, 64);

            log("adding a record...");
            RecordWriter rw = new RecordWriter("foo.lastAccessTime");
            Date insertD = new Date();
            rw.writeObject(insertD);

            recordsFile.insertRecord(rw);
            assertTrue(recordsFile.recordExists("foo.lastAccessTime"));

            log("reading record...");
            RecordReader rr = recordsFile.readRecord("foo.lastAccessTime");
            Date d = (Date) rr.readObject();
            assertTrue(d.getTime()==insertD.getTime());

            System.out.println("\tlast access was at: " + d.toString());

            log("updating record...");
            rw = new RecordWriter("foo.lastAccessTime");
            rw.writeObject(new Date());
            recordsFile.updateRecord(rw);


            log("reading record...");
            rr = recordsFile.readRecord("foo.lastAccessTime");
            d = (Date) rr.readObject();
            System.out.println("\tlast access was at: " + d.toString());

            log("deleting record...");
            recordsFile.deleteRecord("foo.lastAccessTime");
            if (recordsFile.recordExists("foo.lastAccessTime")) {
                assertTrue(false);
            } else {
                log("record successfully deleted.");
            }

            File f1 = new File(fp);
            assertTrue(f1.exists());


        } catch (Exception e) {
            assertTrue(false);
        }
    }

}
