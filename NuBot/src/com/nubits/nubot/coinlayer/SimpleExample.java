package com.nubits.nubot.coinlayer;

import io.metaexchange.bitcoinrpc.BitcoinAPI;
import io.metaexchange.bitcoinrpc.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;


public class SimpleExample {

    private static final Logger logger = LoggerFactory.getLogger(SimpleExample.class);

    private static BitcoinAPI fullclient;

    private static String rpcfile = "conf/rpc.properties";


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

        Block block = client.getLastBlock();
        System.out.println("block " + block);

        System.out.println(block.getMerkleroot());

        BigDecimal balance = client.getBalance();
        System.out.println("balance: " + balance);

        //String newaddr = client.getnewaddress("test");
        //System.out.println("newaddr: " + newaddr);

        //Map<String, String> valid = client.validateaddress(newaddr);
        //boolean isvalid = new Boolean(valid.get("isvalid")).booleanValue();
        //System.out.println(isvalid);


    }

}
