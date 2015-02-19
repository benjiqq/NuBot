package com.nubits.nubot.options;

/**
 * NuBot Configuration Error. Configuration is wrongly defined
 */
public class NuBotConfigError extends Exception {

    public NuBotConfigError(String errorMessage){
        super(errorMessage);
    }
}
