package com.nubits.nubot.utils;


import com.nubits.nubot.global.Settings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class VersionInfo {

    private static HashMap getInfoFile() {
        String wdir = System.getProperty("user.dir");

        String fp = wdir + "/" + Settings.INFO_FILE;

        File file = new File(fp);
        try {
            List lines = FileUtils.readLines(file, "UTF-8");
            HashMap km = new HashMap();
            for (Object o : lines) {
                String l = "" + o;
                try {
                    String[] a = l.split("=");
                    km.put(a[0], a[1]);
                } catch (Exception e) {
                    //ignore line with "="
                }

            }
            return km;
        } catch (Exception e) {
            //throw e;
        }
        return null;
    }

    public static String getBranchCommitInfo() {
        if (FileSystem.insideJar()) {

            HashMap km = getInfoFile();

            if (km.containsKey("version")) {
                return "" + km.get("version");
            }

            return "load version error";

        } else {
            return getCurrentgitBranch();
        }
    }

    /**
     * read current git branch within a git repository
     *
     * @return
     */
    private static String getCurrentgitBranch() {
        //get current git branch
        try {
            String fp = System.getProperty("user.dir") + "/" + ".git" + "/" + "HEAD";
            File f = new File(fp);
            if (f.exists()) {
                String s = FileUtils.readFileToString(f);
                s = s.replace("ref: refs/heads/", "");
                s = s.replace("\n", "");
                return "develop:branch-" + s;
            }

        } catch (Exception e) {
            ;
        }
        return "error";
    }

    /**
     * get version from ".nubot file"
     *
     * @return
     */
    public static String getVersionName() {

        if (FileSystem.insideJar()) {

            HashMap km = getInfoFile();

            if (km.containsKey("version")) {
                return "" + km.get("version");
            }

            return "load version error";

        } else {
            String branch = getCurrentgitBranch();
            return branch;
        }
    }
}
