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

package com.nubits.nubot.options;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.bot.Global;
import com.nubits.nubot.utils.FilesystemUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Save NuBotOptions to disk
 */
public class SaveOptions {


    public static String jsonPretty(NuBotOptions opt){
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        //gson.registerTypeAdapter(NuBotOptions.class, new NuBotOptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);
        return js;
    }

    public static boolean saveOptionsPretty(NuBotOptions opt, String filepath){
        String js = jsonPretty(opt);
        FilesystemUtils.writeToFile(js, filepath, false);
        return true;
    }
    /**
     * save options to file
     *
     * @param opt
     * @param filepath
     * @return
     */
    public static boolean saveOptions(NuBotOptions opt, String filepath) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String jsonOpt = gson.toJson(opt);
        FilesystemUtils.writeToFile(jsonOpt, filepath, false);
        return true;
    }

    public static boolean optionsReset(String filepath) {
        NuBotOptions opt = NuBotOptionsDefault.defaultFactory();
        Global.options = opt;
        return true;
    }


    /**
     * backup an options file. write it to .bak and increase counter
     *
     * @param filepath
     * @return
     */
    public static boolean backupOptions(String filepath) throws IOException{
        File f = new File(filepath);
        if (f.exists()) {
            boolean wrote = false;
            int i = 0;
            while (!wrote) {
                String fp = f.getParent() + File.separator + f.getName() + "_" + i + ".bak";
                File dest = new File(fp);
                if (!dest.exists()) {
                    try {
                        FileUtils.copyFile(f, dest);
                        return true;
                    } catch (IOException e) {
                        throw new IOException(e);
                    }
                }
                i++;

                if (i > 100)
                    return false;
            }
        }
        return false;
    }
}
