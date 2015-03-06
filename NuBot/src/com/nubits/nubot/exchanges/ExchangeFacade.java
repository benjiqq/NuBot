package com.nubits.nubot.exchanges;

import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.Trade;
import com.nubits.nubot.options.NuBotOptions;
import com.nubits.nubot.trading.TradeInterface;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;

import java.util.HashMap;

/**
 * a facade for all exchanges
 */
public class ExchangeFacade {
    //Exchanges
    public static final String BTCE = "btce";
    public static final String CCEDK = "ccedk";
    public static final String BTER = "bter";
    public static final String INTERNAL_EXCHANGE_PEATIO = "peatio";
    public static final String BITSPARK_PEATIO = "bitspark";
    public static final String POLONIEX = "poloniex";
    public static final String CCEX = "ccex";
    public static final String ALLCOIN = "allcoin";
    public static final String EXCOIN = "excoin";
    public static final String BITCOINCOID = "bitcoincoid";
    public static final String ALTSTRADE = "altstrade";
    //API base url for peatio instances
    public static final String INTERNAL_EXCHANGE_PEATIO_API_BASE = "http://178.62.186.229/";   //Old

    /**
     * set up interface based on options
     * @param opt
     * @return
     */
    public static TradeInterface exchangeInterfaceSetup(NuBotOptions opt) {
        Global.exchange = new Exchange(Global.options.getExchangeName());
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);
        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());
        TradeInterface ti = ExchangeFacade.getInterface(Global.exchange);
        ti.setKeys(keys);
        ti.setExchange(Global.exchange);
        return ti;
    }

    public static TradeInterface getInterface(Exchange exc){
        return getInterfaceByName(exc.getName());
    }

    public static TradeInterface getInterfaceByName(String name){
        HashMap<String, TradeInterface> supportedExchanges = new HashMap<>();

        supportedExchanges.put(ExchangeFacade.BTCE, new BtceWrapper());
        supportedExchanges.put(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO, new PeatioWrapper());
        supportedExchanges.put(ExchangeFacade.BTER, new BterWrapper());
        supportedExchanges.put(ExchangeFacade.CCEDK, new CcedkWrapper());
        supportedExchanges.put(ExchangeFacade.POLONIEX, new PoloniexWrapper());
        supportedExchanges.put(ExchangeFacade.CCEX, new CcexWrapper());
        supportedExchanges.put(ExchangeFacade.ALLCOIN, new AllCoinWrapper());
        supportedExchanges.put(ExchangeFacade.BITSPARK_PEATIO, new BitSparkWrapper());
        supportedExchanges.put(ExchangeFacade.EXCOIN, new ExcoinWrapper());
        supportedExchanges.put(ExchangeFacade.BITCOINCOID, new BitcoinCoIDWrapper());

        if (supportedExchanges.containsKey(name)) {
            return supportedExchanges.get(name);
        }
        return null;
    }

}
