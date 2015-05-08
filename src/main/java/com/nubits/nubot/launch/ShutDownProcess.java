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

package com.nubits.nubot.launch;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.bot.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ShutDownProcess implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ShutDownProcess.class.getName());

    @Override
    public void run() {
        LOG.debug("global shutdownhook called");
        //shutdown logic of the bot handled in the bot related to the global thread
        if (SessionManager.isSessionRunning()) {
            LOG.debug("bot is running. shut it down");
            Global.bot.shutdownBot();
            SessionManager.setModeHalting();
            SessionManager.sessionStopped = System.currentTimeMillis();
        }

        //Interrupt mainThread
        if (Global.mainThread != null)
            Global.mainThread.interrupt();

        LOG.info("Exit main");

        //this shuts down UI as well
        Thread.currentThread().interrupt();
        return;
    }
}
