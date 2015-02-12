package com.nubits.nubot.webui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.simple.JSONStreamAware;

import com.nubits.nubot.utils.FileSystem;

@SuppressWarnings("serial")
/**
 * manage keys
 * TODO: use keypair object
 * TODO: use proper file paths (Json)
 * TODO: for exchange
 */
public class KeyServlet extends HttpServlet {

    private String keyfile = "keys.txt";

    public void putKeys(String key, String secret, String exchange) {
        // TODO: for testing only
        // TODO: santize inputs (UTF8?, test for validity, etc)
        String s = "key:" + key + ";secret:" + secret;

        FileSystem.writeToFile(s, keyfile, false);
    }

    public String getKeys(String exchange) {
        // TODO: for testing only
        // TODO: santize inputs (UTF8?, test for validity, etc)

        String all = FileSystem.readFromFile(keyfile);
        System.out.println(all);
        return all;
    }

    public boolean keysExist(String exchange) {
        boolean ex = false;
        File f = new File(keyfile);
        ex = f.exists();
        return ex;
    }

    public static JSONStreamAware prepare(final JSONObject json) {
        return new JSONStreamAware() {
            private final char[] jsonChars = json.toString().toCharArray();

            @Override
            public void writeJSONString(Writer out) throws IOException {
                out.write(jsonChars);
            }
        };
    }

    JSONObject demo(String abc) {
        JSONObject json = new JSONObject();
        json.put("apikey", "abc");
        return json;
    }

    public final static JSONStreamAware emptyJSON = prepare(new JSONObject());

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        JSONStreamAware response = prepare(demo("testasdf")); //emptyJSON;
        resp.setContentType("text/plain; charset=UTF-8");
        try (Writer writer = resp.getWriter()) {
            response.writeJSONString(writer);
        }

        //response.setStatus(HttpServletResponse.SC_OK);
        //response.getWriter().println(greeting);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String key = req.getParameter("key");
        String secret = req.getParameter("secret");

        PrintWriter out = resp.getWriter();

        // TODO wrap in html
        String exc = "test";
        putKeys(key, secret, exc);

        out.println("<html>");
        out.println("<body>");
        out.println("Key \"" + key + "\"<br>");
        out.println("Secret \"" + secret + "\"");
        out.println("</body>");
        out.println("</html>");
    }
}