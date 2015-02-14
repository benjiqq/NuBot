package com.nubits.nubot.webui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * basic server for WebUI
 *
 */
public class oldUIServer {

    private static final int UI_PORT = 8080;

    private static Server server;

    public static void main(String[] args) {

        // we're always serving from localhost for now
        String host = "localhost";
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(UI_PORT);
        connector.setHost(host);

        server.addConnector(connector);

        HandlerList uiHandlers = new HandlerList();

        ResourceHandler userFileHandler = new ResourceHandler();
        userFileHandler.setDirectoriesListed(false);
        userFileHandler.setWelcomeFiles(new String[] { "index.html" });
        // folder path
        String htmlPath = "html";
        userFileHandler.setResourceBase(htmlPath);

        uiHandlers.addHandler(userFileHandler);

        ServletHandler srvHandler = new ServletHandler();
        ServletHolder ch = srvHandler.addServletWithMapping(oldKeyServlet.class,
                "/keys");
        // u.setAsyncSupported(true);
        

        uiHandlers.addHandler(srvHandler);

        uiHandlers.addHandler(new DefaultHandler());

        server.setHandler(uiHandlers);

        try {
            server.start();
            server.join();
            // LOG
        } catch (Exception e) {
            e.printStackTrace();
        }
        // server.setStopAtShutdown(true);

    }
}
