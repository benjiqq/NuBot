package com.nubits.nubot.utils;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import com.nubits.nubot.bot.Global;

import java.util.Arrays;
import java.util.List;

public class OptionLogFilter extends ch.qos.logback.core.filter.AbstractMatcherFilter {

    @Override
    public FilterReply decide(Object event) {
       /*if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }*/
        System.out.println("event " + event);

        LoggingEvent loggingEvent = (LoggingEvent) event;

        List<Level> eventsToKeep = Arrays.asList(Level.TRACE, Level.DEBUG, Level.INFO);
        if (Global.options!= null)
            System.out.println("verbose: " + Global.options.isVerbose());
        if (eventsToKeep.contains(loggingEvent.getLevel())) {
            return FilterReply.NEUTRAL;
        } else {
            return FilterReply.DENY;
        }
    }

}