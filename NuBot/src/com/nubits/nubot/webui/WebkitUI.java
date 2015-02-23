package com.nubits.nubot.webui;


import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;


public class WebkitUI extends Application {
    private Scene scene;

    private String startPage = "http://www.nubits.com";

    @Override
    public void start(Stage stage) {
        // create the scene
        stage.setTitle("Web View");
        Browser browser = new Browser();
        scene = new Scene(browser, 750, 500, Color.web("#666970"));
        stage.setScene(scene);
        scene.getStylesheets().add("webviewsample/BrowserToolbar.css");

        browser.loadSite(startPage);

        stage.show();


    }

    public static void main(String[] args) {
        System.out.println("launch app");
        for (String a : args){
            System.out.println(a);
        }
        launch();
    }
}



class Browser extends Region {

    final WebView browser = new WebView();
    final WebEngine webEngine = browser.getEngine();

    public Browser() {

        System.out.println("create browser");
        //apply the styles
        getStyleClass().add("browser");

        //add the web view to the scene
        getChildren().add(browser);

    }

    public void loadSite(String url) {
        // load the web page
        webEngine.load(url);
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        layoutInArea(browser, 0, 0, w, h, 0, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }
}
