package com.nubits.nubot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
 
/** Establish a SSL connection to a host and port, writes a byte and
 * prints the response. See
 * http://confluence.atlassian.com/display/JIRA/Connecting+to+SSL+services
 */
public class SSLConnectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(SSLConnectionTest.class);

    public static boolean connectionTest(String host, int port){
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, port);

            InputStream in = sslsocket.getInputStream();
            OutputStream out = sslsocket.getOutputStream();

            // Write a test byte to get a reaction
            out.write(1);

            String s = "";
            while (in.available() > 0) {
                s += in.read();
            }
            LOG.info("SSLTest. Successfully connected. Read string: " + s);

            return true;

        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }

    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: "+ SSLConnectionTest.class.getName()+" <host> <port>");
            System.exit(1);
        }

        connectionTest(args[0], Integer.parseInt(args[1]));


    }
}