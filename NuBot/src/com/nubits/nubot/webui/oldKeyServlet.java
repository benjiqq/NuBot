package com.nubits.nubot.webui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.simple.JSONStreamAware;

import com.nubits.nubot.options.OptionsJSON;
import com.nubits.nubot.utils.FileSystem;

@SuppressWarnings("serial")
/**
 * get config and set config
 * TODO: for exchange
 */
public class oldKeyServlet extends HttpServlet {

    private String keyfile = "keys.txt";

    private static String exchangeFile = "config.json";

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

    // TODO refactor and put in OptionJSON
    JSONObject configJson() {
        JSONObject json = new JSONObject();
        Map m = new HashMap();
        m = OptionsJSON.getOptionsFromSingleFile(exchangeFile);
        Iterator<String> it = m.keySet().iterator();
        while (it.hasNext()) {
            String k = it.next();
            Object o = m.get(k);
            json.put(k, "" + o);
        }
        return json;
    }

    public final static JSONStreamAware emptyJSON = prepare(new JSONObject());

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        JSONStreamAware response = prepare(configJson());
        resp.setContentType("text/plain; charset=UTF-8");
        try (Writer writer = resp.getWriter()) {
            response.writeJSONString(writer);
        }

    }

}