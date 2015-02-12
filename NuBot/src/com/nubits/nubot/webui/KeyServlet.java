package com.nubits.nubot.webui;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
/**
 * manage keys
 *
 */
public class KeyServlet extends HttpServlet {

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String key = req.getParameter("key");
        String secret = req.getParameter("secret");

        PrintWriter out = resp.getWriter();

        // TODO wrap in html

        out.println("<html>");
        out.println("<body>");
        out.println("Key \"" + key + "\"<br>");
        out.println("Secret \"" + secret + "\"");
        out.println("</body>");
        out.println("</html>");
    }
}