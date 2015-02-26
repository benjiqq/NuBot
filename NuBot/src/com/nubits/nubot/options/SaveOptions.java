package com.nubits.nubot.options;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.utils.FileSystem;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Save NuBotOptions to disk
 */
public class SaveOptions {


    public static boolean saveOptionsPretty(NuBotOptions opt, String filepath){
        GsonBuilder gson = new GsonBuilder().setPrettyPrinting();
        gson.registerTypeAdapter(NuBotOptions.class, new OptionsSerializer());
        Gson parser = gson.create();
        String js = parser.toJson(opt);
        FileSystem.writeToFile(js, filepath, false);
        System.out.println(js);
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
        FileSystem.writeToFile(jsonOpt, filepath, false);
        //TODO: success?
        return true;
    }


    /**
     * backup an options file. write it to .bak and increase counter
     *
     * @param filepath
     * @return
     */
    public static boolean backupOptions(String filepath) {
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        return true;
                    }
                }
                i++;
            }
        }
        return false;
    }
}
