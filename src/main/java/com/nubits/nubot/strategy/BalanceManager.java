package com.nubits.nubot.strategy;


import com.nubits.nubot.bot.Global;
import com.nubits.nubot.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalanceManager {

    private static final Logger LOG = LoggerFactory.getLogger(BalanceManager.class.getName());

    private Amount balance;

    private PairBalance pairBalance;

    public void fetchBalance(Currency currency) throws Exception {
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalance(currency);
        if (!balancesResponse.isPositive()) {
            String errmsg = balancesResponse.getError().toString();
            LOG.error(errmsg);
            throw new Exception(errmsg);
        }

        this.balance = (Amount) balancesResponse.getResponseObject();

    }

    public void fetchBalances(CurrencyPair pair) throws Exception {
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(pair);

        if (!balancesResponse.isPositive()) {
            String errmsg =balancesResponse.getError().toString();
            LOG.error(errmsg);
            throw new Exception(errmsg);
        }

        this.pairBalance = (PairBalance) balancesResponse.getResponseObject();

    }

    public Amount getBalance() {
        return this.balance;
    }

    public PairBalance getPairBalance(){
        return this.pairBalance;
    }
}
