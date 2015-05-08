package com.nubits.nubot.webui;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.eclipse.jetty.io.RuntimeIOException;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Render bootstrap based HTML with Mustache
 * Top: html head
 * Footer: footer
 */
public class LayoutTemplateEngine extends TemplateEngine {

    private MustacheFactory mustacheFactory;
    private String htmlfolder;

    /**
     * Constructs a mustache template engine
     */
    public LayoutTemplateEngine(String htmlfolder) {
        this.htmlfolder = htmlfolder;
        mustacheFactory = new DefaultMustacheFactory("templates");
    }

    /**
     * Constructs a mustache template engine
     *
     * @param mustacheFactory the mustache factory
     */
    public LayoutTemplateEngine(MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
    }

    public String renderTop() {
        Map map = new HashMap();

        //using default of 4567
        int serverPort = 4567;

        //TODO: spark issue: can't set port.
        //setPort(serverPort); // Spark will run on this port
        //map.put("port", "" + serverPort);

        ModelAndView mv = new ModelAndView(map, this.htmlfolder + "top.mustache");
        return renderTemplateFile(mv);
    }

    public String renderFooter() {
        Map map = new HashMap();
        ModelAndView mv = new ModelAndView(map, this.htmlfolder + "footer.mustache");
        return renderTemplateFile(mv);
    }

    public String renderTemplateFile(ModelAndView modelAndView) {

        String viewName = modelAndView.getViewName();
        Mustache mustache = mustacheFactory.compile(viewName);
        StringWriter stringWriter = new StringWriter();
        try {
            mustache.execute(stringWriter, new Object()).close();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
        return stringWriter.toString();
    }

    @Override
    public String render(ModelAndView modelAndView) {
        String viewName = modelAndView.getViewName();
        Mustache mustache = mustacheFactory.compile(viewName);
        StringWriter stringWriter = new StringWriter();
        try {
            mustache.execute(stringWriter, modelAndView.getModel()).close();
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }

        String body = stringWriter.toString();
        String head = renderTop();
        String footer = renderFooter();
        String all = head + body + footer;
        return all;

    }
}