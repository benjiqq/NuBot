package com.nubits.nubot.webui;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.FilterReply;


public class JettyLogFilter extends ch.qos.logback.core.filter.AbstractMatcherFilter {

    @Override
    public FilterReply decide(Object event) {

        /*if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }*/

        LoggingEvent loggingEvent = (LoggingEvent) event;
        if (loggingEvent.getMessage().contains("DEBUG")) {
            return FilterReply.DENY;
        } else {
            return FilterReply.NEUTRAL;
        }

        /*List<Level> eventsToKeep = Arrays.asList(Level.TRACE, Level.DEBUG, Level.INFO);
        if (eventsToKeep.contains(loggingEvent.getLevel()))
        {
            return FilterReply.NEUTRAL;
        }
        else
        {
            return FilterReply.DENY;
        }*/
    }

}
