package com.nubits.nubot.webui;


public class Msg {

    private String apikey;

    public Msg(String apikey) {
        this.apikey= apikey;
    }

    public String getApikey() {
        return apikey;
    }

    public void setApikey(String apikey) {
        this.apikey= apikey;
    }

}