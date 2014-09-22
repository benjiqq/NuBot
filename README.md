![alt text](../master/readme-assets/logo.png?token=776957__eyJzY29wZSI6IlJhd0Jsb2I6YWR2MHIvbnUtYm90L21hc3Rlci9yZWFkbWUtYXNzZXRzL2xvZ28ucG5nIiwiZXhwaXJlcyI6MTQxMTU5NTQ0N30%3D--e085989b71fcc6e5f2dae1925d97169a84c62ed5 "NuBot Logo")
#####Official automated trading bot for NuBits custodians

*Disclaimer : this documentation is currently under-development therefore subject to sudden changes*

#What is NuBot?

NuBot is a tool that helps NuBits custodians to automate trades. As explained in the white paper, custodian's core mission is to help keeping the peg while introducing new currency in the market.

NuBot is a cross platform automated trading bot written in java.

#How does it work?

Only custodians will use the trading bot and relay liquidity data to other Nu clients. Within this subsystem there are two types of custodians: sell side and dual side custodians. Dual side custodians are custodians whose specific function is to provide liquidity for compensation, and they will initially only provide buy side price support. Once their buy order for NBT is partially filled, the bot should then create a sell order for that NBT. In the case of sell side custodians, the liquidity they provide is secondary to another goal such as funding core development, marketing NBT or distributing Peercoin dividends. They want to spend the proceeds of their NBT, so under no circumstance will they provide buy side liquidity. The trading bot must permit a user to indicate they are either a sell side or dual side custodian. This will effect the trading bot's behavior as detailed in the use cases below.

## Sell-side strategy

First someone who wishes to fulfill this role must seek shareholder approval via the custodial grant mechanism. Say a particular liquidity provider or LP custodian has 10 million USD he wishes to use to provide NuBit liquidity. He would expect compensation for lost opportunity cost (he could otherwise invest those funds in rental property, stocks or bonds) and for the risk of loss via an exchange default, such as we have seen with Mt. Gox and others. While the market will continually reprice this, let's say in our case the prospective LP custodian decides a 5% return every six months is fair compensation for lost opportunity cost and risk of exchange default. So, he promises shareholders to provide 10 million USD/NBT worth of liquidity for one year in exchange for 500,000 NBT. Shareholders approve this using the grant mechanism and he is granted 500,000 NBT. Now he must provide 10 million in liquidity constantly over the next year. He may do this through a single exchange or multiple exchanges. Let's say he does this with a single exchange. He opens an account with the exchange, then deposits $10 million worth of BTC. He exchanges the BTC for USD.
Now he is ready to make use of the trading bot. An appropriate exchange will expose a trading API and our trading bot must implement the API for that specific exchange. Each implementation of a specific exchange API should implement an interface called IExchange to standardize the way the trading bot interacts with these diverse exchange APIs. Doing this will allow the LP custodian to enter authentication information into the trading bot for his exchange account. He will then use the user interface in our trading bot to place an buy order for 10,000,000 NBT on the exchange. The price will not be exactly one USD. It will be one USD minus the exchange transaction fee. If the exchange charges a 0.2% transaction fee, he will place a buy order for 10,000,000 NBT at a price of 0.998 USD. Let us suppose his order is partially filled in the amount of 1,000,000 NBT. Now his exchange account will contain 9,000,000 USD used to fund an order for 9,000,000 NBT. There will also be 1,000,000 NBT in the account. The trading bot should automatically and immediately place these 1,000,000 NBT for sale at a price of 1.002 USD (one USD + a 0.2% transaction fee). If this order fills, then the bot should use the USD proceeds to immediately place a buy order for NBT at 0.998 USD. All funds should be continually on order and the LP custodian's funds should not be depleted by transaction fees.
When an order is placed, canceled or filled (even partially), the liquidityinfo RPC is called Nu client.

See Attached Diagrams

## Dual-side strategy
In some cases custodians will spend the NuBits directly and not use the trading bot at all. For instance, if core developers accept NuBits as compensation then Jordan Lee will simply distribute NuBits granted to him directly without the need for any exchange.
Let's examine the case where a 10,000,000 NBT custodial grant is given for the purpose of distributing a shareholder dividend in Peercoin. Such a custodian will deposit 10,000,000 NBT in a single or multiple exchanges. In our use case we will use a single exchange. Once the NBT deposit is credited, the custodian will start the trading bot, indicate they are a sell side custodian and indicate that orders should be created, although nothing specific about the order should be entered by the user. The trading bot should offer the entire balance of NBT for sale using the formula of one USD + transaction fee + one pricing increment. Let's say our exchange has a transaction fee of 0.2%. Some exchanges allow the fee to be discovered through their API, while others do not. If the fee can be found through the API, it should. If not, the user should be asked to specify the transaction fee. Let's say this exchange supports 4 decimal places in its order book on the NBT/USD pair. Using our formula above, the trading bot would place an sell order for 10,000,000 NBT at a price of 1.0021. The reason it should be 1.0021 instead of 1.002 is that we want dual side sell orders to be executed first, so their funds can be returned to providing buy side liquidity.
Each time an order is placed, cancelled or filled (even partially), the liquidityinfo Nu client RPC method should be called. Details about this method can be found in the Liquidity Pool Tracking section. Calling liquidityinfo will have the effect of transmitting the size of the buy and sell liquidity pool the local trading bot is managing to all known Nu peers. 

See Attached Diagrams

## Other strategies

There might be adjusted versions of the custodians modus operandi explained above. To request a custom build of NuBot to fulfil a particular custodial grant, get in touch with desrever@nubits.com .

Existing strategies to date :
* KTm's Dual Side Strategy with 10% of proceedings from sales kept in balance. See custodial proposal .

#Using NuBot
##Disclaimer . Use NuBot at your own risk

PLEASE BE AWARE THAT AUTOMATED TRADING WITH NUBOT MAY BE RISKY, ADDICTIVE, UNETHICAL OR ILLEGAL. ITS MISUSE MAY ALSO CAUSE FINANCIAL LOSS.

None of the authors, contributors, administrators, or anyone else connected with NuBits, in any way whatsoever, can be responsible for your use of the NuBot. By using NuBot you 


##Assumption for a correct functioning 
* The computer that runs NuBot must be connected to the internet
* The custodian must provide the bot with access to market exchanges where NuBits are traded
* The custodian must avoid manual interacting with the exchange while the automated bot is operating. Do not try to trade, do not try to deposit/withdraw funds. 


##1) Prepare options.json

Essential options : (check under the hood for hidden parameters available ) 
```
{"options":
    {
    "exchangename":"peatio",
    "apikey": "foo",
    "apisecret": "bar",
    "dualside": true,
    "nubitaddress": "bVcXrdTgrMSg6J2YqsLedCbi6Ubek9eTe5",
    "nudip": "127.0.0.1",
    "nudport": 9091,
    "priceincrement": "0.001",
    "rpcpass": "ciaociao",
    "rpcuser": "adva",
    "txfee": "0",
    "sendrpc":true,
    "executeorders":true,
    "verbose":false,
    "pair":"nbt_btc",
    "check-balance-interval":30,
    "check-orders-interval":30,
    "hipchat":false,
    "mail-notifications":true,
    "mail-recipient":"hi@adva.io",
    "crypto-peg-options":
        {
            "main-feed":"blockchain",
            "backup-feeds": {
                "backup1" : { "name" : "bitcoinaverage"},
                "backup2" : { "name" : "coinbase"},
                "backup3" : { "name" : "btce"}
                },
            "refresh-time":124,
            "wallchange-treshold": 3,
            "price-offset": 3,
            "price-distance-threshold":10,
            "emergency-timeout-minutes":60
         }
 }
}
```

| Parameter        | default value           | Description  |
| ------------- |:-------------:| -----:|
| col 3 is      | right-aligned | $1600 |
| col 2 is      | centered      |   $12 |
| zebra stripes | are neat      |    $1 |

```
exchangename : "peatio" is the only value accepted now. In the future this field will be use to switch between exchanges.
dualside :  if set to true, the bot will behave as a dual side custodian, if false as a sell side custodian.
priceincrement : if working in sell-side mode, this value will be added to the sell price 
txfee : 0.2 indicates a 0.2% tx fee
sendrpc : if set to false, will not try to submit RPC liquidityinfo to the nu client
executeorders: if set to false, will not execute the order, only print on screen. Useful for simulations
verbose: if set to true, will print on screen additional debug messages 
pair : the codes of the currency pair to operate, lowercase
check-price-interval: seconds between two checkPrice executions, for BTC_NBT or PPC_NBT pairs
check-balance-interval: seconds between two checkBalance operations, which triggers bot orders
check-orders-interval: seconds between two checkOrders operations, which triggers liquidityinfo RPC call
hipchat : if set to true will notify the Team on hipchat about Orders submitted, shutdowns, emergencies, and others
mail-notifications : if set to true will send email in emergency cases
mail-recipient : the email at which emergency email has to be sent  
```


##2)Prepare the nu client
The bot needs to broadcast liquidity notification to NuNet. It does so by interacting via the NuBits client of the custodian. 



The bot communicates with the Nu client via RCP over HTTP. If you plan to run the bot on a different machine, you must authorise remote calls to your Nu client. In order to do that, open your nu.conf and add make sure you have it properly configured .


```
rpcuser=<your_username>
rpcpassword=<your_password>
port=9090
rpcport=9091
rpcallowip=<ip_of_machine_running_the_bot>
```

Launch Nu client and make sure that bot wallets are unlocked, otherwise it won't execute RPC calls. Make also sure having being in running the client on a valid custodian address, aka the same that received the grant.  
Is it also possible to test NuBot without being a custodian and setting the sendRPC option to false.

##3)Run NuBot


```
java -jar NuBot.jar <path/to/options.json>
```
To terminate the bot, exit the process with "Control+C"

### Requirements

* Java JRE: 1.7 http://www.oracle.com/technetwork/java/javase/downloads/java-se-jre-7-download-432155.html

NuBot comes as a cross platform executable jar file to run from command line.  

#Under the hood
##Adding an exchange
##Design and diagrams
![alt text](../master/readme-assets/bot-case-1.png?token=776957__eyJzY29wZSI6IlJhd0Jsb2I6YWR2MHIvbnUtYm90L21hc3Rlci9yZWFkbWUtYXNzZXRzL2JvdC1jYXNlLTEucG5nIiwiZXhwaXJlcyI6MTQxMTU5NTQyNn0%3D--9f757ea81d3bd4cee44624a3dd85d073945bafaf "NuBot Sell-Side logic")

![alt text](../master/readme-assets/bot-case-2.png?token=776957__eyJzY29wZSI6IlJhd0Jsb2I6YWR2MHIvbnUtYm90L21hc3Rlci9yZWFkbWUtYXNzZXRzL2JvdC1jYXNlLTIucG5nIiwiZXhwaXJlcyI6MTQxMTU5NTQ0Nn0%3D--ae16c75aeeb3f1d56fd2dff536789ef3afcf1e43 "NuBot Double-Side logic")

##Implementation details

##HipChat notifications
##Email notifications
##Hidden parameters
##Adding SSL certificates for an exchange
In order for the bot to communicate with the exchanges via encrypted https API, it is required to add the SSL certificate of the exchange to the list of trusted certificates locally. While NuBot includes the keystore file in its build, I still think that documenting it is a good way for keeping the team up to date. 
The Java JVM uses the Keystore, an encrypted file which contains a collection of authorised certificates.
Using the keytool we can create the keystore 
```
keytool -genkey -alias signFiles -keystore nubot_keystore.jks
```
You will be prompted to enter passwords for the key and keystore.
For nubot's keystore the password happens to be : h4rdc0r_
First we need to get the SSL certificate ( usually .cer).  An easy way is navigate with the browser to the API entry-point, click on the lock icon, and drag the certificate locally.
After that the certificate can be added to the bot using keytool :

```
keytool -importcert -file certificate.cer -keystore nubot_keystore.jks -alias "certificate alias"
```

In the Java code we need to tell to the JVM which keystore to use (usually on startup)

```java
System.setProperty("javax.net.ssl.trustStore",KEYSTORE_FILE_PATH);
System.setProperty("javax.net.ssl.trustStorePassword",KEYSTORE_PASSWORD);
```

To list all the certificates in a keystore

```
keytool -list -v -keystore Nubot_keystore.jks
```

The keystore file containing several SSL certificates from known exchanges :
nubot_keystore.jks

##Logging on HTML and csv

The bot produces a csv and html log for each session. We now have 4 levels of log messages : severe, warning, info and fine. fine are never logged to file (only to console) , info are logged to file if verbose=true, warning are used for logging trading-related messagges, and severe for errors.

#Changelogs
See [changelog.md](../master/CHANGELOG.md)

#Contribute

#License 
NuBot is released under [GNU GPL v2.0](../master/LICENSE.md) 


                               
