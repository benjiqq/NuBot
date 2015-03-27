package com.nubits.nubot.launch;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.global.Settings;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.options.ParseOptions;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

/**
 * Session Manager starts and stops NuBots and manages the associated sessions
 */
public class SessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static final Logger sessionLOG = LoggerFactory.getLogger(Settings.Session_LOGGER_NAME);

    private static File sessionFile;

    private static String appFolder;


    /**
     * main launch of a bot
     *
     * @param configfile
     * @param runui
     */
    public static void sessionLaunch(String configfile, boolean runui) {

        NuBotOptions nuopt = null;

        sessionLOG.debug("parsing options from " + configfile);

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            nuopt = ParseOptions.parseOptionsSingle(configfile);
        } catch (NuBotConfigException e) {
            MainLaunch.exitWithNotice("" + e);
        }

        sessionLOG.debug("launch bot");

        launchBot(nuopt);

    }

    /**
     * setup all the logging and storage for one session
     */
    private static void setupSession() {

        //set up session dir
        String wdir = System.getProperty("user.dir");

        /*File ldir = new File(wdir + "/" + Settings.LOGS_PATH);
        if (!ldir.exists())
            ldir.mkdir();*/


        Global.sessionLogFolder = wdir + "/" + Global.sessionPath;
        //String sessiondir = wdir + "/" + Settings.LOGS_PATH + Global.sessionLogFolder;



        //LOG.debug("session launch called from " + caller);
        //LOG.debug("launch bot session. with configfile " + configfile + " " + " runui " + runui);


        //TODO!
        // check if other bots are running
        boolean otherSessions = isSessionActive();

        if (otherSessions) {
            LOG.info("NuBot is already running");
            //handle different cases
        } else {
            createSessionFile();
        }

        Global.sessionStarted = System.currentTimeMillis();
        //sessionLOG.debug("session start;" + Global.sessionLogFolder + ";" + Global.sessionStarted);

        String timestamp =
                new java.text.SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());

        LOG.info("*** session *** starting at " + timestamp);

    }

    /**
     * execute a NuBot based on valid options
     *
     * @param opt
     */
    public static void launchBot(NuBotOptions opt) {

        Global.mainThread = Thread.currentThread();

        Global.createShutDownHook();

        setupSession();

        LOG.debug("execute bot depending on defined strategy");

        if (opt.requiresSecondaryPegStrategy()) {
            LOG.debug("creating secondary bot object");
            Global.bot = new NuBotSecondary();
            Global.bot.execute(opt);
        } else {
            LOG.debug("creating simple bot object");
            Global.bot = new NuBotSimple();
            Global.bot.execute(opt);


        }

    }


    /**
     * check whether other sessions are active via a temp file
     *
     * @return
     */
    public static boolean isSessionActive() {

        appFolder = System.getProperty("user.home") + "/" + Settings.APP_FOLDER;

        sessionFile = new File
                (appFolder, Settings.APP_NAME + Settings.SESSION_FILE);
        LOG.info("checking " + sessionFile.getAbsolutePath() + " " + sessionFile.exists());
        return sessionFile.exists();
    }

    public static void createSessionFile() {
        try {
            File appdir = new File(System.getProperty("user.home") + "/" + Settings.APP_FOLDER);
            if (!appdir.exists())
                appdir.mkdir();

            String sessionFileName = Global.sessionStarted + Settings.SESSION_FILE;
            sessionFile = new File(appFolder, Settings.APP_NAME + sessionFileName);
            sessionFile.createNewFile();

            //delete the file on exit
            sessionFile.deleteOnExit();

        } catch (Exception e) {

        }

    }
}
