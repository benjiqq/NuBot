package com.nubits.nubot.utils.logging;

import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SetEclipse {
    public static void setEclipseLogback(String logbackFile) {
        try {
            String ud = System.getProperty("user.dir");
            String fp = ud + "/conf/" + logbackFile;
            System.out.println(fp);
            File f = new File(fp);

            String fs = "" + f.toURI().toURL();

            // System.setProperty("log4j.configuration", fs);
            System.setProperty("logback.configurationFile", fs);
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss");
            System.setProperty("current.date", dateFormat.format(new Date()));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
}
