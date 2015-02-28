package com.nubits.nubot.webui;

import com.nubits.nubot.options.NuBotOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;

/**
 * controller for status updates
 */
public class StatusController {

    final static Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    public StatusController(String endpoint) {


        get(endpoint, "application/json", (request, response) -> {

            return "";
        });


    }
}
