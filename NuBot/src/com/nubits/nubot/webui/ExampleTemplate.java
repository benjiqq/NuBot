package com.nubits.nubot.webui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * example use of template, model and view with Mustache and Spark
 *
 */
public class ExampleTemplate {

    List<Item> items() {
        return Arrays.asList(
                new Item("Item 1", "$19.99", Arrays.asList(new Feature("New!"),
                        new Feature("Awesome!"))),
                new Item("Item 2", "$29.99", Arrays.asList(new Feature("Old."),
                        new Feature("Ugly."))));
    }

    static class Item {
        Item(String name, String price, List<Feature> features) {
            this.name = name;
            this.price = price;
            this.features = features;
        }

        String name, price;
        List<Feature> features;
    }

    static class Feature {
        Feature(String description) {
            this.description = description;
        }

        String description;
    }

    public static void main(String[] args) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        // TODO: use relative path
        Mustache mustache = mf.compile("./html/tmpl/template.mustache");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, new ExampleTemplate()).flush();
        String s = writer.toString();
        System.out.println(s);
    }
}