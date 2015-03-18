package com.nubits.nubot.utils;

import sun.security.krb5.Config;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * a small utility to show the used classpath and VM arguments
 */
public class ClasspathUtils {

    public static void main(String[] a) {

        System.out.println("++++ system arguments...");
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }

        //-cp .:conf

        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();

        System.out.println("++++ vm arguments... " + arguments.size());
        for (int i = 0; i < arguments.size(); i++) {
            System.out.println(arguments.get(i));
        }

        System.out.println("++++ classpath...");
        System.out.println(a.length);

        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);

        for (int i = 0; i < classpathEntries.length; i++) {
            System.out.println(classpathEntries[i]);
        }


        //see
        // http://docs.oracle.com/javase/8/docs/api/java/lang/ClassLoader.html#getResources-java.lang.String-

        System.out.println("classloader roots");

        ClassLoader classLoader = ClasspathUtils.class.getClassLoader();
        // or use:
        // ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // depending on what's appropriate in your case.
        try{
            Enumeration<URL> roots = classLoader.getResources(".");
            while (roots.hasMoreElements()) {
                final URL url = roots.nextElement();
                System.out.println(url);
            }
        }catch(Exception e){

        }

    }
}
