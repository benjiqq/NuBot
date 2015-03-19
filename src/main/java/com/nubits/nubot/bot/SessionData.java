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

package com.nubits.nubot.bot;

/**
 * a session object for store in HTML overview
 */
public class SessionData {

    String description;
    String sessionid;
    String currency;
    String exchange;
    String startTime;
    String stopTime;

    public SessionData(String description, String sessionid, String exchange, String startTime, String stopTime) {
        this.description = description;
        this.sessionid = sessionid;
        this.exchange = exchange;
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public String toCSVrow() {
        String s = "";
        s += this.description + ";" + this.sessionid + ";" + this.exchange + ";" + this.startTime + ";" + this.stopTime;
        return s;
    }

}
