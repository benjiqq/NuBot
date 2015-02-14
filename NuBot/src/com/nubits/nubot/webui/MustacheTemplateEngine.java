package com.nubits.nubot.webui;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.jetty.io.RuntimeIOException;

import spark.ModelAndView;
import spark.TemplateEngine;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Defaults to the 'templates' directory under the resource path.
 *
 * @author Sam Pullara https://github.com/spullara
 */
public class MustacheTemplateEngine extends TemplateEngine {

    private MustacheFactory mustacheFactory;

    /**
     * Constructs a mustache template engine
     */
    public MustacheTemplateEngine() {
        mustacheFactory = new DefaultMustacheFactory("templates");
    }

    /**
     * Constructs a mustache template engine
     *
     * @param resourceRoot the resource root
     */
    public MustacheTemplateEngine(String resourceRoot) {
        mustacheFactory = new DefaultMustacheFactory(resourceRoot);
    }

    /**
     * Constructs a mustache template engine
     *
     * @param mustacheFactory the mustache factory
     */
    public MustacheTemplateEngine(MustacheFactory mustacheFactory) {
        this.mustacheFactory = mustacheFactory;
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
        return stringWriter.toString();
    }
}