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

package com.nubits.nubot.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Query implements Delayed {
    private static final Logger LOG = LoggerFactory.getLogger(Query.class.getName());
    private long startTime;

    private String url;
    private String method;
    private TreeMap<String, String> query_args;
    private boolean isGet;

    public Query(String url, String method, TreeMap<String, String> query_args, boolean isGet, long delay) {
        this.url = url;
        this.method = method;
        this.query_args = query_args;
        this.isGet = isGet;
        this.startTime = System.currentTimeMillis() + delay;

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public TreeMap<String, String> getQuery_args() {
        return query_args;
    }

    public void setQuery_args(TreeMap<String, String> query_args) {
        this.query_args = query_args;
    }

    public boolean isGet() {
        return isGet;
    }

    public void setIsGet(boolean isGet) {
        this.isGet = isGet;
    }


    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);

    }
    
    @Override
    public int compareTo(Delayed o) {
        if (this.startTime < ((Query) o).startTime) {
            return -1;
        }

        if (this.startTime > ((Query) o).startTime) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Query{" +
                "startTime=" + startTime +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", query_args=" + query_args +
                ", isGet=" + isGet +
                '}';
    }
}
