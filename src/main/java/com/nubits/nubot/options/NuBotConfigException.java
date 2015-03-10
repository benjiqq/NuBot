package com.nubits.nubot.options;

/**
 * NuBot Configuration Exception. Configuration is wrongly defined
 */
public class NuBotConfigException extends Exception {

    public NuBotConfigException(String errorMessage){
        super(errorMessage);
    }
}
