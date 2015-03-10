package com.nubits.nubot.tasks.strategy;


import com.nubits.nubot.bot.Global;
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

    public static void clearOrders() throws OrderException {
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(Global.options.getPair());
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
            if (deleted) {
                LOG.warn("Clear all orders request succesfully");
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

            } else {
                String message = "Could not submit request to clear orders";
                LOG.error(message);
                throw new OrderException(message);
            }

        } else {
            LOG.error(deleteOrdersResponse.getError().toString());
            String message = "Could not submit request to clear orders";
            LOG.error(message);
            throw new OrderException(message);
        }
    }
}
