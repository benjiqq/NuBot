package com.nubits.nubot.bot;

import com.nubits.nubot.global.Settings;
import com.nubits.nubot.launch.MainLaunch;
import com.nubits.nubot.options.*;
import com.nubits.nubot.strategy.Primary.NuBotSimple;
import com.nubits.nubot.strategy.Secondary.NuBotSecondary;
import com.nubits.nubot.utils.FilesystemUtils;
import com.nubits.nubot.utils.NuLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

/**
 * Session Manager starts and stops NuBots and manages the associated sessions
 */
public class SessionManager {

    private static final Logger LOG = LoggerFactory.getLogger(MainLaunch.class.getName());

    private static final Logger sessionLOG = LoggerFactory.getLogger(Settings.SESSION_LOGGER_NAME);

    private static File sessionFile;

    private static String appFolder;


    /**
     * set config in global
     *
     * @param configfile
     */
    public static void setConfigGlobal(String configfile) {

        sessionLOG.debug("parsing options from " + configfile);

        try {
            //Check if NuBot has valid parameters and quit if it doesn't
            NuBotOptions opt = ParseOptions.parseOptionsSingle(configfile);
            LOG.debug("loading opt: " + opt.toStringNoKeys());
            Global.options = opt;
        } catch (NuBotConfigException e) {
            MainLaunch.exitWithNotice("" + e);
        }

    }

    /**
     * set config in global
     */
    public static void setConfigDefault() {

        NuBotOptions defaultOpt = NuBotOptionsDefault.defaultFactory();
        Global.options = defaultOpt;
        SaveOptions.saveOptionsPretty(defaultOpt,Settings.DEFAULT_CONFIG_FILE_PATH);

    }

    /**
     * setup all the logging and storage for one session
     */
    private static void setupSession() {

        //set up session dir
        String wdir = FilesystemUtils.getBotAbsolutePath();

        Global.sessionLogFolder = wdir + "/" + Global.sessionPath;

        // check if other bots are running
        boolean otherSessions = isSessionActive();

        if (otherSessions) {
            LOG.info("NuBot is already running");
            //handle different cases later

            //TODO: several NuBots running one exchange should be prohibited (?)
        }

        //create session file, which auto-deletes itself
        //it increases the counter
        createSessionFile();

        Global.sessionRunning = true;
        Global.sessionStarted = System.currentTimeMillis();

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
            try {
                Global.bot.execute(opt);
            } catch (Exception e) {

            }
        } else {
            LOG.debug("creating simple bot object");
            Global.bot = new NuBotSimple();
            try {
                Global.bot.execute(opt);
            } catch (Exception e) {

            }
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

    /**
     * create a session file in the app folder. increase session counter with each session
     * Note: session file gets marked as deleted after exit
     */
    public static void createSessionFile() {

        try {
            File appdir = new File(System.getProperty("user.home") + "/" + Settings.APP_FOLDER);
            if (!appdir.exists())
                appdir.mkdir();
        } catch (Exception e) {
        }

        boolean done = false;
        int i = 0;
        while (!done) {

            String sessionFileName = Settings.APP_NAME + "_" + i + Settings.SESSION_FILE;
            sessionFile = new File(appFolder, sessionFileName);
            if (!sessionFile.exists()) {
                try {
                    sessionFile.createNewFile();
                    //delete the file on exit
                    sessionFile.deleteOnExit();
                    done = true;
                } catch (Exception e) {

                }

            }
            i++;
        }
    }


}
