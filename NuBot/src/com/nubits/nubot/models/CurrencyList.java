package com.nubits.nubot.models;


public class CurrencyList {

    public static final Currency USD = Currency.createCurrency("USD");
    public static final Currency CNY = Currency.createCurrency("CNY");
    public static final Currency EUR = Currency.createCurrency("EUR");
    public static final Currency PHP = Currency.createCurrency("PHP");
    public static final Currency HKD = Currency.createCurrency("HKD");
    public static final Currency BTC = Currency.createCurrency("BTC");
    public static final Currency NBT = Currency.createCurrency("NBT");
    public static final Currency NSR = Currency.createCurrency("NSR");
    public static final Currency PPC = Currency.createCurrency("PPC");
    public static final Currency LTC = Currency.createCurrency("LTC");

    public static final CurrencyPair NBT_USD = new CurrencyPair(NBT, USD);
    public static final CurrencyPair BTC_USD = new CurrencyPair(BTC, USD);

}
