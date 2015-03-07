/*
 * Copyright (C) 2014-2015 Nu Development Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package functions;

/**
 *
 *
 */

import com.nubits.nubot.utils.FileSystem;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class TestReadCsv  extends TestCase {

    private static final Logger LOG = LoggerFactory.getLogger(TestReadCsv.class
            .getName());
    private static final String TEST_FILE_PATH = "/currencies.csv";
    private static final String TEST_FILE = "currencies.csv";

    @Test
    public void testFile() {
        // try {
        // TestClasspathOut.addPath("/conf");
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // TestClasspathOut.printCpInfo();

        final File dic = FileUtils.getFile("NuBot", "res", TEST_FILE);
        LOG.debug(dic.getAbsolutePath());
        //assert (dic.exists());

        // InputStream ins = TestReadCsv.class.getClass().
        // getResourceAsStream(TEST_FIL);
        // System.out.println(ins);
    }

    @Test
    public void testReadCSV() {
        // getResourceAsStream
        final File f = FileUtils.getFile("NuBot", "res", TEST_FILE);
        
        ArrayList<String[]> parsedCsv = FileSystem
                .parseCsvFromFile(f.getAbsolutePath());
        assert(parsedCsv.size()>5);
        for (int j = 0; j < parsedCsv.size(); j++) {
            String[] tempLine = parsedCsv.get(j);
            String message = "Line " + j + 1 + "/" + parsedCsv.size() + " = ";
            for (int i = 0; i < tempLine.length; i++) {
                message += "[" + i + "]=" + tempLine[i];
            }
            //assert(message.length()>10);
            //assert(message.contains("USD"));
            //assert(message.contains("NBT"));
            LOG.debug(message);
        }

    }
}
