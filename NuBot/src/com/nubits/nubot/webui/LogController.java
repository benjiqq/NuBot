package com.nubits.nubot.webui;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.get;


public class LogController {

    String filePath = "llogs/NuBot_main.log";

    public LogController(String endpoint) {

        get(endpoint, "application/json", (request, response) -> {
            try {
                String l = new String(Files.readAllBytes(Paths.get(filePath)));
                return l;
            }catch(Exception e){

            }

            return "log";

        }, new JsonTransformerOptions());


    }
}
