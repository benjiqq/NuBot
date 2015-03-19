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

package com.nubits.nubot.options;

import com.nubits.nubot.models.Currency;
import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.models.CurrencyPair;
import com.nubits.nubot.notifications.MailNotifications;

/**
 * Default options for NuBot
 */
public class NuBotOptionsDefault {

    //double wallchangeThreshold = 0.5;
    //double spread = 0;
    //double distanceThreshold = 10;


    static {
        CurrencyList.init();
    }

    public static NuBotOptions defaultFactory() {

        NuBotOptions opt = new NuBotOptions();
        opt.dualSide = true;
        opt.apiKey = "";
        opt.apiSecret = "";
        opt.rpcUser = "";
        opt.rpcPass = "";
        opt.nudIp = "127.0.0.1";
        opt.priceIncrement = 0.0003;
        opt.txFee = 0.2;
        opt.submitLiquidity = false;
        opt.executeOrders = false;
        opt.sendHipchat = true;
        opt.sendMails = MailNotifications.MAIL_LEVEL_SEVERE;
        opt.mailRecipient = "";
        opt.emergencyTimeout = 30;
        opt.keepProceeds = 0.0;
        opt.distributeLiquidity = false;
        opt.secondarypeg = false;
        opt.pair = CurrencyList.NBT_BTC;
        opt.verbose = false;
        opt.sendHipchat = true;
        opt.multipleCustodians = false;
        opt.maxSellVolume = 0;
        opt.maxBuyVolume = 0;
        opt.nudPort = 9091;
        opt.nudIp = "127.0.0.1";
        return opt;
    }
}
