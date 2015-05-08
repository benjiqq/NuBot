/*
 * Copyright (C) 2015 Nu Development Team
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

package com.nubits.nubot.utils;


import com.nubits.nubot.global.Settings;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class VersionInfo {
    final static Logger LOG = LoggerFactory.getLogger(VersionInfo.class);

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
            LOG.error(e.toString());
        }
        return null;
    }

    public static String getBranchCommitInfo() {
        if (FilesystemUtils.insideJar()) {

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

        if (FilesystemUtils.insideJar()) {
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
