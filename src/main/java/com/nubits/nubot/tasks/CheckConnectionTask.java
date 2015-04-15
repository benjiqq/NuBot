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

import com.nubits.nubot.bot.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.TimerTask;


public class CheckConnectionTask extends TimerTask {

    private static final Logger LOG = LoggerFactory.getLogger(CheckConnectionTask.class.getName());

    private String url;

    @Override
    public void run() {

        LOG.info("Executing " + this.getClass());

        this.url = Global.exchange.getLiveData().getUrlConnectionCheck();
        Global.exchange.getLiveData().setConnected(isConnected());
        LOG.debug("Checking connection to " + url + " -  Connected : " + Global.exchange.getLiveData().isConnected());

    }

    public boolean isConnected() {
        boolean connected = false;
        HttpURLConnection connection = null;
        URL query = null;
        try {
            query = new URL(this.url);
        } catch (MalformedURLException ex) {
            LOG.error(ex.toString());
        }
        try {
            connection = (HttpURLConnection) query.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.getOutputStream();
            connected = true;
        } catch (NoRouteToHostException | UnknownHostException ex) {
            connected = false;
            LOG.error(ex.toString());
        } catch (IOException ex) {
            connected = false;
            LOG.error(ex.toString());
        }
        return connected;
    }
}
