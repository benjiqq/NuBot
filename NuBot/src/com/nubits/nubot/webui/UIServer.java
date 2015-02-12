package com.nubits.nubot.webui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * basic server for setting up a WebUI
 *
 */
public class UIServer {

    private static final int TESTNET_UI_PORT = 2875;

    private static Server server;

    public static class HelloServlet extends HttpServlet {
        private static final String greeting = "Hello World";

        protected void doGet(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException {

            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(greeting);
        }

    }

    public static void main(String[] args) {

        int port = 8080;

        // final String host = getStringProperty("uiServerHost");
        String host = "localhost";
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost(host);

        // connector.setIdleTimeout(getIntProperty("uiServerIdleTimeout"));
        // connector.setReuseAddress(true);

        server.addConnector(connector);

        HandlerList userHandlers = new HandlerList();

        ResourceHandler userFileHandler = new ResourceHandler();
        userFileHandler.setDirectoriesListed(false);
        userFileHandler.setWelcomeFiles(new String[] { "index.html" });
        // userFileHandler.setResourceBase(getStringProperty("uiResourceBase"));
        userFileHandler.setResourceBase("html");

        userHandlers.addHandler(userFileHandler);

        ServletHandler userHandler = new ServletHandler();
        // ServletHolder userHolder =
        // userHandler.addServletWithMapping(UserServlet.class, "/nu");
        ServletHolder u = userHandler.addServletWithMapping(HelloServlet.class,
                "/");
        // u.setAsyncSupported(true);

        userHandlers.addHandler(userHandler);

        userHandlers.addHandler(new DefaultHandler());

        server.setHandler(userHandlers);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // userServer.setStopAtShutdown(true);
        //
        //
        // userServer.start();
        // Logger.logMessage("Started user interface server at " + host + ":" +
        // port);

    }
}
