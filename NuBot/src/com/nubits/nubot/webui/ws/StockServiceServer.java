package com.nubits.nubot.webui.ws;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class StockServiceServer implements Runnable {

    @Override
    public void run(){
        Server server = new Server(4568);

        ServletContextHandler ctx = new ServletContextHandler();
        ctx.setContextPath("/");
        ctx.addServlet(StockServiceSocketServlet.class, "/stocks");

        server.setHandler(ctx);

        try {
            server.start();
            //server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static class StockServiceSocketServlet extends WebSocketServlet {

        @Override
        public void configure(WebSocketServletFactory factory) {
            factory.register(StockService.class);
        }
    }
}