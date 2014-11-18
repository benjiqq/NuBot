package com.nubits.nubot.tests;

/**
 *
 * @author desrever <desrever at nubits.com>
 */
import com.nubits.nubot.NTP.NTPClient;
import com.nubits.nubot.notifications.HipChatNotifications;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

public class TestSync extends TimerTask {

    private static final Logger LOG = Logger.getLogger(TestSync.class.getName());
    private static final int TASK_INTERVAL = 61 * 1000;
    private static String id;
    private static int startTime;

    public static void main(String[] args) throws InterruptedException {

        startTime = (int) (System.currentTimeMillis() / 1000);
        System.out.println("Start-time = " + startTime);
        id = UUID.randomUUID().toString();

        message("Started");
        //Random sleep + 10 seconds
        int rand = 10 + (int) Math.round(Math.random() * 10);
        Thread.sleep(rand * 1000);

        //Read remote date
        message("Reading remote time");
        Date remoteDate = new NTPClient().getTime();
        Calendar remoteCalendar = new GregorianCalendar();
        remoteCalendar.setTime(remoteDate);

        //Compute the delay
        message("Computing delay");

        int remoteTimeInSeconds = remoteCalendar.get(Calendar.SECOND);
        message("Remote time in sec = " + remoteTimeInSeconds);
        int delay = (60 - remoteTimeInSeconds) * 1000;
        message("Delay = " + delay + " ms");

        //Construct and use a TimerTask and Timer.
        TimerTask testSync = new TestSync();
        Timer timer = new Timer();
        timer.schedule(testSync, delay, TASK_INTERVAL);
        message("Timer scheduled");
    }

    private static void message(String msg) {
        System.out.println(getIdString() + msg);
    }

    private static String getIdString() {
        int now = (int) (System.currentTimeMillis() / 1000);
        int secondsFromStart = now - startTime;
        return id.substring(id.lastIndexOf("-") + 10) + " , t=" + secondsFromStart + "     - ";
    }

    @Override
    public void run() {
        //Send hipchat notification
        message("Run");

        HipChatNotifications.sendMessage(getIdString() + " test", com.nubits.nubot.notifications.jhipchat.messages.Message.Color.RED);

        // add a random sleep after the notification to see if the keep sync

        /*int rand = (int) Math.round(Math.random() * 20);
         try {
         Thread.sleep(rand * 1000);
         } catch (InterruptedException ex) {
         LOG.severe(ex.getMessage());
         }
         */
    }
}
