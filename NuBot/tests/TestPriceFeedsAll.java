import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * test feed in aggregated
 */
public class TestPriceFeedsAll extends TestCase {

    List<Double> alllast;
    private String dbfile = "testprices.db";

    public void setUp(){

        File ff = new File(dbfile);
        if (ff.exists()) {
            ff.delete();
        }


    }

    @Test
    public void testZAvg() {

       /* alllast.add(lastbtce);
        alllast.add(lastbci);
        alllast.add(lastbf);
        alllast.add(lastcb);

        assertTrue(alllast.size()==4);

        double sumlast = 0.0;
        double min = 0.0;
        double max = 9999999.9;
        int n = 0;

        System.out.println("n: " + n);
        for (Iterator<Double> it = alllast.iterator(); it.hasNext();) {
            double p = it.next();
            System.out.println("p: " + p);
            sumlast += p;

            if (p > max)
                max = p;

            if (p < min)
                min = p;

            n++;
        }

        System.out.println("sum: " + sumlast);

        assertTrue(sumlast > 0.0);
        //sum should be bigger than min * number of prices (n)
        assertTrue(sumlast > min*n);
        //same for max
        assertTrue(sumlast < max*n);
*/


    }
}