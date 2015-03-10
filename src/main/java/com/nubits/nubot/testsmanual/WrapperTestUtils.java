package com.nubits.nubot.testsmanual;



import com.nubits.nubot.bot.Global;
import com.nubits.nubot.exchanges.Exchange;
import com.nubits.nubot.exchanges.ExchangeFacade;
import com.nubits.nubot.exchanges.ExchangeLiveData;
import com.nubits.nubot.models.*;
import com.nubits.nubot.options.NuBotConfigException;
import com.nubits.nubot.tasks.TaskManager;
import com.nubits.nubot.trading.Ticker;
import com.nubits.nubot.trading.TradeUtils;
import com.nubits.nubot.trading.keys.ApiKeys;
import com.nubits.nubot.trading.wrappers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class WrapperTestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WrapperTestUtils.class.getName());

    public static void testGetAvailableBalances(CurrencyPair pair) {
        //Get all the balances  associated with the account
        LOG.warn("Get Balances for " + pair.toString());
        ApiResponse balancesResponse = Global.exchange.getTrade().getAvailableBalances(pair);
        if (balancesResponse.isPositive()) {
            Balance balance = (Balance) balancesResponse.getResponseObject();
            LOG.info(balance.toString());
        } else {
            LOG.error(balancesResponse.getError().toString());
        }
    }

    public static void testGetAvailableBalance(Currency cur) {
        //Get the USD balance associated with the account
        LOG.warn("Get Balance for " + cur.getCode());
        ApiResponse balanceResponse = Global.exchange.getTrade().getAvailableBalance(cur);
        if (balanceResponse.isPositive()) {
            Amount balance = (Amount) balanceResponse.getResponseObject();
            LOG.info(balance.toString());
        } else {
            LOG.error(balanceResponse.getError().toString());
        }
    }

    public static void testGetLastPrice(CurrencyPair pair) {
        //Get lastPrice for a given CurrencyPair
        LOG.warn("Get last price for " + pair.toString());
        ApiResponse lastPriceResponse = Global.exchange.getTrade().getLastPrice(pair);
        if (lastPriceResponse.isPositive()) {
            Ticker ticker = (Ticker) lastPriceResponse.getResponseObject();
            LOG.info("Last price : 1 " + pair.getOrderCurrency().getCode() + " = "
                    + ticker.getLast() + " " + pair.getPaymentCurrency().getCode());
            LOG.info("ask  : 1 " + pair.getOrderCurrency().getCode() + " = "
                    + ticker.getAsk() + " " + pair.getPaymentCurrency().getCode());
            LOG.info("bid  : 1 " + pair.getOrderCurrency().getCode() + " = "
                    + ticker.getBid() + " " + pair.getPaymentCurrency().getCode());
        } else {
            LOG.error(lastPriceResponse.getError().toString());
        }

    }

    public static void testSell(double amountSell, double priceSell, CurrencyPair pair) {
        //Place a sell order
        LOG.warn("Place a SELL order");
        ApiResponse sellResponse = Global.exchange.getTrade().sell(pair, amountSell, priceSell);
        if (sellResponse.isPositive()) {
            LOG.info("SELL" + amountSell + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceSell + " " + pair.getPaymentCurrency().getCode());
            String sellResponseString = (String) sellResponse.getResponseObject();
            LOG.info("Response = " + sellResponseString);
        } else {
            LOG.error(sellResponse.getError().toString());
        }
    }

    public static void testBuy(double amountBuy, double priceBuy, CurrencyPair pair) {
        //Place a buy order
        LOG.warn("Place a BUY order");
        ApiResponse buyResponse = Global.exchange.getTrade().buy(pair, amountBuy, priceBuy);
        if (buyResponse.isPositive()) {
            LOG.info("\nPositive response  from TradeInterface.buy(...) ");
            LOG.info("BUY" + amountBuy + " " + pair.getOrderCurrency().getCode()
                    + " @ " + priceBuy + " " + pair.getPaymentCurrency().getCode());
            String buyResponseString = (String) buyResponse.getResponseObject();
            LOG.info("Response = " + buyResponseString);
        } else {
            LOG.error(buyResponse.getError().toString());
        }
    }

    public static void testGetActiveOrders() {
        //Get active orders
        LOG.warn("Get ALL Active Orders");
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getActiveOrders();
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Order> orderList = (ArrayList<Order>) activeOrdersResponse.getResponseObject();
            LOG.info("Active orders : " + orderList.size());
            for (int i = 0; i < orderList.size(); i++) {
                Order tempOrder = orderList.get(i);
                LOG.info(tempOrder.toString());
            }
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static void testGetActiveOrders(CurrencyPair pair) {
        //Get active orders associated with a specific CurrencyPair
        LOG.warn("Get Active Orders for " + pair.toString());
        ApiResponse activeOrdersUSDNTBResponse = Global.exchange.getTrade().getActiveOrders(pair);
        if (activeOrdersUSDNTBResponse.isPositive()) {
            ArrayList<Order> orderListUSDNBT = (ArrayList<Order>) activeOrdersUSDNTBResponse.getResponseObject();
            LOG.info("Active orders : " + orderListUSDNBT.size());
            for (int i = 0; i < orderListUSDNBT.size(); i++) {
                Order tempOrder = orderListUSDNBT.get(i);
                LOG.info(tempOrder.toString());
            }
        } else {
            LOG.error(activeOrdersUSDNTBResponse.getError().toString());
        }
    }

    public static void testGetOrderDetail(String order_id_detail) {
        //Get the order details for a specific order_id
        LOG.warn("Get Order Detail for " + order_id_detail);
        ApiResponse orderDetailResponse = Global.exchange.getTrade().getOrderDetail(order_id_detail);
        if (orderDetailResponse.isPositive()) {
            Order order = (Order) orderDetailResponse.getResponseObject();
            LOG.info(order.toString());
        } else {
            LOG.info(orderDetailResponse.getError().toString());
        }
    }

    public static void testCancelOrder(String order_id_delete, CurrencyPair pair) {
        //Cancel an order
        LOG.warn("Cancel Order " + order_id_delete + " for " + pair.toString());
        ApiResponse deleteOrderResponse = Global.exchange.getTrade().cancelOrder(order_id_delete, pair);
        if (deleteOrderResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrderResponse.getResponseObject();
            if (deleted) {
                LOG.info("Order deleted successfully");
            } else {
                LOG.info("Could not delete order");
            }
        } else {
            LOG.error(deleteOrderResponse.getError().toString());
        }
    }

    public static void testGetTxFee() {
        //Get current trascation fee
        LOG.warn("Get the tx fee");
        ApiResponse txFeeResponse = Global.exchange.getTrade().getTxFee();
        if (txFeeResponse.isPositive()) {
            double txFee = (Double) txFeeResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFee + "%");
        } else {
            LOG.error(txFeeResponse.getError().toString());
        }
    }

    public static void testGetTxFeeWithArgs(CurrencyPair pair) {
        //Get the current transaction fee associated with a specific CurrencyPair
        LOG.warn("Get tx fee for " + pair.toString());
        ApiResponse txFeeNTBUSDResponse = Global.exchange.getTrade().getTxFee(pair);
        if (txFeeNTBUSDResponse.isPositive()) {
            double txFeeUSDNTB = (Double) txFeeNTBUSDResponse.getResponseObject();
            LOG.info("Trasaction fee = " + txFeeUSDNTB + "%");
        } else {
            LOG.error(txFeeNTBUSDResponse.getError().toString());
        }
    }

    public static void testIsOrderActive(String orderId) {
        //Check if orderId is active
        LOG.warn("Test if " + orderId + " is active");
        ApiResponse orderDetailResponse = Global.exchange.getTrade().isOrderActive(orderId);
        if (orderDetailResponse.isPositive()) {
            boolean exist = (boolean) orderDetailResponse.getResponseObject();
            LOG.info("Order " + orderId + "  active? " + exist);
        } else {
            LOG.error(orderDetailResponse.getError().toString());
        }
    }

    public static void testClearAllOrders(CurrencyPair pair) {
        LOG.warn("Clear all orders");
        ApiResponse deleteOrdersResponse = Global.exchange.getTrade().clearOrders(pair);
        if (deleteOrdersResponse.isPositive()) {
            boolean deleted = (boolean) deleteOrdersResponse.getResponseObject();
            if (deleted) {
                LOG.info("Order clear request succesfully");
            } else {
                LOG.info("Could not submit request to clear orders");
            }
        } else {
            LOG.error(deleteOrdersResponse.getError().toString());
        }
    }

    public static void testGetLastTrades(CurrencyPair pair) {
        //Get active orders
        LOG.warn("Get last trades for " + pair.toString());
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair);
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            LOG.info("Last 24h trades : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);
                LOG.info(tempTrade.toString());
            }
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static void testGetLastTrades(CurrencyPair pair, long startTime) {
        //Get active orders
        LOG.warn("Get last trades for " + pair.toString() + " starting from " + startTime);
        ApiResponse activeOrdersResponse = Global.exchange.getTrade().getLastTrades(pair, startTime);
        if (activeOrdersResponse.isPositive()) {
            ArrayList<Trade> tradeList = (ArrayList<Trade>) activeOrdersResponse.getResponseObject();
            LOG.info("Last trades from " + startTime + " : " + tradeList.size());
            for (int i = 0; i < tradeList.size(); i++) {
                Trade tempTrade = tradeList.get(i);
                LOG.info(tempTrade.toString());
            }
        } else {
            LOG.error(activeOrdersResponse.getError().toString());
        }
    }

    public static void testMultipleOrders(ArrayList<OrderToPlace> orders, CurrencyPair pair) {

        boolean success = TradeUtils.placeMultipleOrders(orders);
        LOG.info("Multiple orders (" + orders + ") placed. success = " + success);

    }

    public static void testPlaceAndClearAllOrders(CurrencyPair pair) {

        //clear old orders if any
        testClearAllOrders(pair);


        // place a few orders
        for (int i = 0; i <= 5; i++) {
            testSell(0.1, 0.004, pair);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }
        }

        for (int i = 0; i <= 5; i++) {
            testBuy(0.1, 0.001, pair);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ex) {
                LOG.error(ex.toString());
            }
        }


        //Wait 4 secs
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

        //try to clear orders
        testClearAllOrders(pair);
    }

    public static void configExchange(String exchangeName) throws NuBotConfigException {

        configExchangeWrapper(exchangeName);

        Global.exchange.getLiveData().setUrlConnectionCheck(Global.exchange.getTrade().getUrlConnectionCheck());

        //Create a TaskManager
        Global.taskManager = new TaskManager();
        //Start checking for connection
        Global.taskManager.getCheckConnectionTask().start();


        //Wait a couple of seconds for the connectionThread to get live
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            LOG.error(ex.toString());
        }

    }

    private static void configExchangeWrapper(String exchangeName) throws NuBotConfigException {
        //config exchange in 3 steps

        //1. create exchange object
        Global.exchange = new Exchange(exchangeName);

        //2. Create e ExchangeLiveData object to accomodate liveData from the Global.exchange
        ExchangeLiveData liveData = new ExchangeLiveData();
        Global.exchange.setLiveData(liveData);

        //3.
        //Create a new TradeInterface object using the custom implementation
        //Assign the TradeInterface to the exchange
        //Wrap the keys into a new ApiKeys object

        ApiKeys keys = new ApiKeys(Global.options.getApiSecret(), Global.options.getApiKey());

        if (exchangeName.equals(ExchangeFacade.BTCE)) {
            Global.exchange.setTrade(new BtceWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.INTERNAL_EXCHANGE_PEATIO)) {
            Global.exchange.setTrade(new PeatioWrapper(keys, Global.exchange, ExchangeFacade.INTERNAL_EXCHANGE_PEATIO_API_BASE));
        } else if (exchangeName.equals(ExchangeFacade.CCEDK)) {
            Global.exchange.setTrade(new CcedkWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.BTER)) {
            Global.exchange.setTrade(new BterWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.POLONIEX)) {
            Global.exchange.setTrade(new PoloniexWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.CCEX)) {
            Global.exchange.setTrade(new CcexWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.ALLCOIN)) {
            Global.exchange.setTrade(new AllCoinWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.BITSPARK_PEATIO)) {
            Global.exchange.setTrade(new BitSparkWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.EXCOIN)) {
            Global.exchange.setTrade(new ExcoinWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.BITCOINCOID)) {
            Global.exchange.setTrade(new BitcoinCoIDWrapper(keys, Global.exchange));
        } else if (exchangeName.equals(ExchangeFacade.ALTSTRADE)) {
            Global.exchange.setTrade(new AltsTradeWrapper(keys, Global.exchange));
        } else {
            throw new NuBotConfigException("Exchange " + exchangeName + " not supported");
        }

    }
}
