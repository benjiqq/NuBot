package com.nubits.nubot.db;

/**
 * local store for prices
 */
public class PriceStore {

    private static String dbfile = "prices.db";

    public static void createPricestore(){
        try{
            NuDB.createDB(dbfile);
        }   catch(Exception ex){

        }
    }
}
