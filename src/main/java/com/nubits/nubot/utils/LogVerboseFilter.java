package com.nubits.nubot.utils;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import com.nubits.nubot.bot.Global;

/**
 * Custom filter for Stdout
 * ERROR and WARN go into separate loggers
 */
public class LogVerboseFilter extends ch.qos.logback.core.filter.AbstractMatcherFilter {

    @Override
    public FilterReply decide(Object event) {

        LoggingEvent loggingEvent = (LoggingEvent) event;

        if (loggingEvent.getLevel().equals(Level.WARN) || loggingEvent.getLevel().equals(Level.ERROR))
            return FilterReply.DENY;

        //return FilterReply.NEUTRAL;

        //only filter if global.options is defined
        if (Global.options != null) {
            boolean isDebug = (loggingEvent.getLevel().equals(Level.DEBUG));
            if (isDebug) {
                if (Global.options.isVerbose()) {
                    return FilterReply.NEUTRAL;
                } else {
                    return FilterReply.DENY;
                }
            }

        }

        return FilterReply.NEUTRAL;


    }

}