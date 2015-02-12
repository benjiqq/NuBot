package com.nubits.nubot.webui;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * basic server for setting up a WebUI
 *
 */
public class UIServer {

    private static final int TESTNET_UI_PORT = 2875;

    private static Server server;

    public static void main(String[] args) {

        int port = 8080;

        // final String host = getStringProperty("uiServerHost");
        String host = "localhost";
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost(host);
        
        server.addConnector(connector);
        
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        // connector.setIdleTimeout(Nxt.getIntProperty("nxt.uiServerIdleTimeout"));
        // connector.setReuseAddress(true);
        // userServer.addConnector(connector);
        //
        //
        // HandlerList userHandlers = new HandlerList();
        //
        // ResourceHandler userFileHandler = new ResourceHandler();
        // userFileHandler.setDirectoriesListed(false);
        // userFileHandler.setWelcomeFiles(new String[]{"index.html"});
        // userFileHandler.setResourceBase(Nxt.getStringProperty("nxt.uiResourceBase"));
        //
        // userHandlers.addHandler(userFileHandler);
        //
        // ServletHandler userHandler = new ServletHandler();
        // ServletHolder userHolder =
        // userHandler.addServletWithMapping(UserServlet.class, "/nxt");
        // userHolder.setAsyncSupported(true);
        //
        //
        // userHandlers.addHandler(userHandler);
        //
        // userHandlers.addHandler(new DefaultHandler());
        //
        // userServer.setHandler(userHandlers);
        // userServer.setStopAtShutdown(true);
        //
        //
        // userServer.start();
        // Logger.logMessage("Started user interface server at " + host + ":" +
        // port);

    }
}
