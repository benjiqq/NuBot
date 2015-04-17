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
       /*if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }*/

        //System.out.println("event " + event);

        LoggingEvent loggingEvent = (LoggingEvent) event;

        if (loggingEvent.getLevel().equals(Level.WARN) || loggingEvent.getLevel().equals(Level.ERROR))
            return FilterReply.DENY;

        if (Global.options != null) {
            boolean isDebug = (loggingEvent.getLevel().equals(Level.DEBUG));
            if (Global.options.isVerbose() && isDebug) {
                System.out.println("allow debug");
                return FilterReply.NEUTRAL;
            } else {
                System.out.println("deny debug");
                return FilterReply.DENY;
            }
        } else {
            return FilterReply.NEUTRAL;
        }

        /*List<Level> eventsToKeep = Arrays.asList(Level.TRACE, Level.DEBUG, Level.INFO);
        if (Global.options!= null)
            System.out.println("verbose: " + Global.options.isVerbose());
        if (eventsToKeep.contains(loggingEvent.getLevel())) {
            return FilterReply.NEUTRAL;
        } else {
            return FilterReply.DENY;
        }*/
    }

}