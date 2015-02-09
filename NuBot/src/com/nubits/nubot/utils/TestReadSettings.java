package com.nubits.nubot.utils;

import java.util.Properties;

import com.nubits.nubot.utils.logging.SetEclipse;

/**
 * a small utility to show settings
 *
 */
public class TestReadSettings {

    public static void main(String[] a) {
        SetEclipse.setEclipseLogback("logback.xml");
        Utils.loadProperties("settings.properties");
        //System.out.println(prop);
    }
}
