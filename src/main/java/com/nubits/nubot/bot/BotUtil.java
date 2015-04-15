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


import com.nubits.nubot.models.ApiResponse;
import com.nubits.nubot.notifications.HipChatNotifications;
import com.nubits.nubot.notifications.MailNotifications;
import com.nubits.nubot.trading.OrderException;
import com.nubits.nubot.trading.TradeUtils;
import io.evanwong.oss.hipchat.v2.rooms.MessageColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BotUtil.class.getName());

    /**
     * clear all orders
     *
     * @throws OrderException
     */
    public static void clearOrders() throws OrderException {
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
        if (!deleteOrdersResponse.isPositive()) {
            LOG.error(deleteOrdersResponse.getError().toString());
            String message = "Could not submit request to clear orders";
            LOG.error(message);
            throw new OrderException(message);
        }

        boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
        if (!deleted) {
            String message = "Could not submit request to clear orders";
            LOG.error(message);
            throw new OrderException(message);
        }

        LOG.warn("Clear all orders request successful");
        //Wait until there are no active orders
        boolean timedOut = false;
        long timeout = Global.options.getEmergencyTimeout() * 1000;
        long wait = 6 * 1000;
        long count = 0L;
        do {
            try {
                Thread.sleep(wait);
                count += wait;
                timedOut = count > timeout;

            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }
        } while (!TradeUtils.tryCancelAllOrders(Global.options.getPair()) && !timedOut);

        if (timedOut) {
            String message = "There was a problem cancelling all existing orders";
            LOG.error(message);
            HipChatNotifications.sendMessage(message, MessageColor.YELLOW);
            MailNotifications.send(Global.options.getMailRecipient(), "NuBot : Problem cancelling existing orders", message);
            //Continue anyway, maybe there is some balance to put up on order.
        }

    }

}
