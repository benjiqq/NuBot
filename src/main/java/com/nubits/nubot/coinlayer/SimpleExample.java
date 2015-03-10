package com.nubits.nubot.coinlayer;

import io.metaexchange.bitcoinapi.BitcoinAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;


public class SimpleExample {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExample.class);

    private static BitcoinAPI fullclient;

    private static String rpcfile = "config/rpc.properties";


    public static void main(String[] args) {

        BitcoinAPI client = null;
        try {
            client = new BitcoinAPI(rpcfile);
        } catch (Exception e) {
            System.exit(0);
        }

        boolean islive = client.isLive();
        if (!islive) {
            System.out.println("blockchain is not live");
            System.exit(0);
        }

        BigDecimal balance = client.getBalance();
        System.out.println("balance: " + balance);

        client.isValid("abc");
        client.sendtoaddress("notworkingaddress",new BigDecimal(0.0001));


    }

}
