package com.nubits.nubot.coinlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import io.metaexchange.bitcoinrpc.BitcoinRPC;
import io.metaexchange.bitcoinrpc.ClientStatic;

public class SimpleExample {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExample.class);

    public static void main(String[] args) {

        JsonRpcHttpClient clientrpc = null;

        logger.debug("[MAIN] Current Date : {}", getCurrentDate());
        System.out.println(getCurrentDate());

        String username, password, host, port;

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(
                    "config/rpc.properties"));
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            host = properties.getProperty("host");
            port = properties.getProperty("port");

            System.out.println("using config " + username + " " + password + " " + host + " " + port);

            clientrpc = ClientStatic.createRpc(username, password, host, port);
            System.out.println("client " + clientrpc);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }


        try {
            BitcoinRPC client = ClientStatic.clientCreate(clientrpc);

            String bcount = "" + client.getblockcount();
            System.out.println("block count: " + bcount);

            String balance = "" + client.getbalance();
            System.out.println("balance: " + balance);

            String newaddr = client.getnewaddress("test");
            System.out.println("newaddr: " + newaddr);

            Map<String,String> valid = client.validateaddress(newaddr);
            boolean isvalid = new Boolean(valid.get("isvalid")).booleanValue();
            System.out.println(isvalid);


        } catch (Exception e) {
            try {
                logger.info("server not available " + e);
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
            }
        }
    }

    private static Date getCurrentDate() {
        return new Date();
    }
}
