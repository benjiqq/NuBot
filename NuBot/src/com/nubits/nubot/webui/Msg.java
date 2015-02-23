package com.nubits.nubot.webui;

/**
 * TODO: refactor this class related to NuBotOptions
 */
public class Msg {

    private String apikey;
    private String apisecret;

    public Msg(String apikey, String apisecret) {
        this.apikey = apikey;
        this.apisecret = apisecret;
    }

    public String getApikey() {
        return apikey;
    }

    public String getApisecret() {
        return apisecret;
    }


}