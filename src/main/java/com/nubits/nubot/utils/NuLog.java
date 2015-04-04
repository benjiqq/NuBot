package com.nubits.nubot.utils;

import com.nubits.nubot.bot.Global;
import org.slf4j.Logger;

/**
 * Log Filtered
 */
public class NuLog {


    /**
     * log based on global options filter
     * @param LOG
     * @param msg
     */
    public static void info(Logger LOG, String msg){
        if (Global.options != null) {
            if (Global.options.isVerbose()) {
                NuLog.info(LOG, msg);
            } else {
                LOG.debug(msg);
            }
        } else{
            LOG.info(msg);
        }
    }

}
