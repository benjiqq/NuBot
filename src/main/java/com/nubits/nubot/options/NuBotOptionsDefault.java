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

import com.nubits.nubot.models.CurrencyList;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.pricefeeds.FeedFacade;

import java.util.ArrayList;

/**
 * Default options for NuBot
 */
public class NuBotOptionsDefault {

    public static NuBotOptions defaultFactory() {

        NuBotOptions opt = new NuBotOptions();
        opt.apiKey = "";
        opt.exchangeName = "";
        opt.apiSecret = "";
        opt.txFee = 0.2;
        opt.pair = CurrencyList.NBT_BTC.toStringSep();
        opt.dualSide = true;
        opt.multipleCustodians = false;
        opt.executeOrders = false;
        opt.verbose = false;
        opt.hipchat = true;
        opt.mailnotifications = MailNotifications.MAIL_LEVEL_SEVERE;
        opt.mailRecipient = "";
        opt.emergencyTimeout = 30;
        opt.keepProceeds = 0.0;
        opt.maxSellVolume = 0;
        opt.maxBuyVolume = 0;
        opt.priceIncrement = 0.0003;
        opt.submitLiquidity = false;
        opt.nubitAddress = "";
        opt.nudIp = "127.0.0.1";
        opt.nudPort = 9091;
        opt.nudIp = "127.0.0.1";
        opt.rpcPass = "";
        opt.rpcUser = "";
        opt.wallchangeThreshold = 0;
        opt.spread = 0;
        opt.mainFeed = FeedFacade.CoinbasePriceFeed;
        opt.backupFeeds = new ArrayList<String>() {{

            add(FeedFacade.BtcePriceFeed);
            add(FeedFacade.BlockchainPriceFeed);
            add(FeedFacade.CoinmarketcapnexuistPriceFeed);
        }};

        return opt;
    }
}
