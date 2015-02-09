package com.nubits.nubot.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

/**
 * a small utility to show the used classpath and VM arguments
 *
 */
public class TestClasspathOut {

    public static void main(String[] a) {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();

        System.out.println("vm arguments...");
        for (int i = 0; i < arguments.size(); i++) {
            System.out.println(arguments.get(i));
        }

        System.out.println("classpath...");
        System.out.println(a.length);

        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);

        for (int i = 0; i < classpathEntries.length; i++) {
            System.out.println(classpathEntries[i]);
        }
    }
}
