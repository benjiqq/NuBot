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
package com.nubits.nubot.tasks;

import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


public class BotTask {

    private static final Logger LOG = LoggerFactory.getLogger(BotTask.class.getName());
    private Timer timer;
    private boolean running;
    private long interval; //expressed in millseconds
    private TimerTask task;
    private String name;

    public BotTask(TimerTask task, long interval, String name) {
        this.timer = new Timer(name);
        this.running = false;

        this.name = name;
        this.interval = interval;
        this.task = task;
    }

    public void toggle() {
        if (!isRunning()) {
            this.start();
        } else {
            this.stop();
        }
    }

    public void start() {
        timer.scheduleAtFixedRate(task, 0, interval * 1000);
        setRunning(true);
        LOG.info("Started BotTask " + this.name);
    }

    public void start(int delay) {
        LOG.info("Starting BotTask " + delay + " interval " + interval);
        timer.scheduleAtFixedRate(task, delay * 1000, interval * 1000);
        setRunning(true);
        LOG.info("Started BotTask " + this.name);

    }

    public void stop() {
        timer.cancel();
        setRunning(false);
        LOG.info("Stopped BotTask " + this.name);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public TimerTask getTask() {
        return task;
    }

    public void setTask(TimerTask task) {
        this.task = task;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
