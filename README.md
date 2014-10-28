![NuBot Logo](https://bytebucket.org/JordanLeePeershares/nubottrading/raw/faa3a5ebbb483372e176e4a8821d7835c2d404fd/readme-assets/logo.png)

#####Official automated trading bot for NuBits custodians


*Disclaimer : this documentation is currently under-development therefore subject to sudden changes*

#What is NuBot?


NuBot is a tool that helps [NuBits](https://www.nubits.com) custodians to automate trades. As explained in the [white paper](http://staging.nubits.com/about/white-paper), custodian's core mission is to **help keeping the peg while introducing new currency in the market**.

NuBot is a cross-platform automated trading bot written in java. 

[Discuss nubot with the community](http://discuss.nubits.com/category/nubits/automated-trading)


#How does it work?

Only custodians will use the trading bot and relay liquidity data to other Nu clients. Within this subsystem there are two types of custodians: **sell side** and **dual side** custodians. *Dual side custodians* are custodians whose specific function is to provide liquidity for compensation, and they will initially only provide buy side price support. Once their buy order for NBT is partially filled, the bot should then create a sell order for that NBT. In the case of *sell side custodians*, the liquidity they provide is secondary to another goal such as funding core development, marketing NBT or distributing Peercoin dividends. They want to spend the proceeds of their NBT, so under no circumstance will they provide buy side liquidity. NuBot permits a user to indicate they are either a sell side or dual side custodian. This effect the trading bot's behavior as detailed in the use cases below.


## Dual-side strategy

First someone who wishes to fulfill this role must seek shareholder approval via the custodial grant mechanism. Say a particular liquidity provider or *LP* custodian has 10 million USD he wishes to use to provide NuBit liquidity. He would expect compensation for lost opportunity cost (he could otherwise invest those funds in rental property, stocks or bonds) and for the risk of loss via an exchange default, such as we have seen with Mt. Gox and others. While the market will continually reprice this, let's say in our case the prospective LP custodian decides a 5% return every six months is fair compensation for lost opportunity cost and risk of exchange default. So, he promises shareholders to provide 10 million USD/NBT worth of liquidity for one year in exchange for 500,000 NBT. Shareholders approve this using the grant mechanism and he is granted 500,000 NBT. Now he must provide 10 million in liquidity constantly over the next year. He may do this through a single exchange or multiple exchanges. Let's say he does this with a single exchange. He opens an account with the exchange, then deposits $10 million worth of BTC. He exchanges the BTC for USD.
Now he is ready to make use of the trading bot. An appropriate exchange will expose a trading API and our trading bot implements the API for that specific exchange. Each implementation of a specific exchange API implements an interface to standardize the way the trading bot interacts with these diverse exchange APIs. Doing this will allow the LP custodian to enter authentication information into the trading bot for his exchange account. He will then use the user interface in our trading bot to place an buy order for 10,000,000 NBT on the exchange. The price will not be exactly one USD. It will be one USD minus the exchange transaction fee. If the exchange charges a 0.2% transaction fee, he will place a buy order for 10,000,000 NBT at a price of 0.998 USD. Let us suppose his order is partially filled in the amount of 1,000,000 NBT. Now his exchange account will contain 9,000,000 USD used to fund an order for 9,000,000 NBT. There will also be 1,000,000 NBT in the account. The trading bot automatically and immediately place these 1,000,000 NBT for sale at a price of 1.002 USD (one USD + a 0.2% transaction fee). If this order fills, then the bot should use the USD proceeds to immediately place a buy order for NBT at 0.998 USD. All funds should be continually on order and the LP custodian's funds should not be depleted by transaction fees.
When an order is placed, canceled or filled (even partially), the liquidityinfo RPC is called Nu client.

See [Attached Diagrams](#diagram-dual) for a visual flowchart. 

## Sell-side strategy 

In some cases custodians will spend the NuBits directly and not use the trading bot at all. For instance, if core developers accept NuBits as compensation then Jordan Lee will simply distribute NuBits granted to him directly without the need for any exchange.
Let's examine the case where a 10,000,000 NBT custodial grant is given for the purpose of distributing a shareholder dividend in Peercoin. Such a custodian will deposit 10,000,000 NBT in a single or multiple exchanges. In our use case we will use a single exchange. Once the NBT deposit is credited, the custodian will start the trading bot, indicate they are a sell side custodian and indicate that orders should be created, although nothing specific about the order should be entered by the user. The trading bot should offer the entire balance of NBT for sale using the formula of one USD + transaction fee + one pricing increment. Let's say our exchange has a transaction fee of 0.2%. Some exchanges allow the fee to be discovered through their API, while others do not. If the fee can be found through the API, it will. If not, the user should be asked to specify the transaction fee. Let's say this exchange supports 4 decimal places in its order book on the NBT/USD pair. Using our formula above, the trading bot would place an sell order for 10,000,000 NBT at a price of 1.0021. The reason it should be 1.0021 instead of 1.002 is that we want dual side sell orders to be executed first, so their funds can be returned to providing buy side liquidity.
Each time an order is placed, cancelled or filled (even partially), the *liquidityinfo* Nu client RPC method is called. Details about this method can be found in the Liquidity Pool Tracking section. Calling *liquidityinfo* will have the effect of transmitting the size of the buy and sell liquidity pool the local trading bot is managing to all known Nu peers. 

See [Attached Diagrams](#diagram-sell) for a visual flowchart. 

## Other strategies

There might be adjusted versions of the two strategies explained above. To request a custom build of NuBot to fulfil a particular custodial grant, [get in touch](http://discuss.nubits.com/category/nubits/automated-trading).

Alternative strategies currently under development :
* *Secondary Peg Strategy* : a strategy to let custodian trade of pairs different from NBT/USD .
* *KTms Strategy* : dual side Strategy with 10% of proceedings from sales kept in balance. Link to [KTm's custodial proposal](http://discuss.nubits.com/t/proposal-to-operate-a-nubits-grant-to-provide-early-stage-dual-side-liquidity-and-shareholder-dividends/120/25) .

#Using NuBot
##Disclaimer . Use NuBot at your own risk

*PLEASE BE AWARE THAT AUTOMATED TRADING WITH NUBOT MAY BE RISKY, ADDICTIVE, UNETHICAL OR ILLEGAL. ITS MISUSE MAY ALSO CAUSE FINANCIAL LOSS. NONE OF THE AUTHORS, CONTRIBUTORS, ADMINISTRATORS, OR ANYONE ELSE CONNECTED WITH NUBITS, IN ANY WAY WHATSOEVER, CAN BE RESPONSIBLE FOR THE USE YOU MAKE OF NUBOT*

By using NuBot you declare to be accept the afore-mentioned risks.

##Assumption for a correct functioning 

* The computer that runs NuBot must be connected to the internet
* The custodian must provide the bot with access to market exchanges where NuBits are traded
* The custodian must avoid manual interacting with the exchange while the automated bot is operating. Do not try to trade, do not try to deposit/withdraw funds. 
* The custodian must avoid opening multiple instances of Nu client in control of the custodian address at the same time.
* To communicate liquidityinfo, the custodian should possess an unlocked nubit client controlling the custodial grant address. 
* Before running the bot on a market where another instance of NuBot is operating, make sure to reach an agreement on the price-feed to be used.
* For NBT_EUR pair, only one instance per market is currently allowed.

### 1)Prepare the NuBits client client

To function correctly, NuBot needs to communicate with the NuBits client of the custodian. The bot needs to broadcast liquidity notification to NuNet. By doing so, it allow shareholders to have real-time information about active orders in the network across custodians, and react to it. It does so by interacting via the *NuBits client of the custodian*. 

*NOTE: Is it also possible to test NuBot without being a custodian and setting the `"submit-liquidity":false` in the option file. In this case you can ignore this section.*

The bot communicates with the Nu client via RCP over HTTP. If you plan to run the bot on a different machine, you must authorise remote calls to your Nu client. In order to do that, open your *nu.conf* and add make sure you have it properly configured .


```
server=1
rpcuser=<choose-a-username>
rpcpassword=<choose-a-pass>
port=9090
rpcport=9091
rpcallowip=<ip_of_machine_running_the_bot>
```

*NOTE: if using NuBot and NuBits on the same machine, the last parameter can be omitted*

Launch Nu client and , if the NuBits wallet is encrypted with a passphrase, make sure to unlock it, otherwise it won't accept RPC calls to *liquidityinfo*. Also, make sure that the NuBit client controls a public address which received a custodian grants.

Unlocking the wallet *for minting only* is not enough : the two wallets are locked/unlocked separately. To unlock the NuBits wallets, first make sure the client is displaying the NuBits units, then open the conolse and type :
```
walletpassphrase <your passphrase> 9999999999 
```
The comand above will unlock the NBT wallet for  9999999999 seconds, ~ 300 years. That should be enough time for your bot to keep the peg.


### 2) Configure the NuBot

**Download latest stable builds from the [download page](https://bitbucket.org/JordanLeePeershares/nubottrading/downloads).**

The bot reads options from a *.json* file.

Here is a list of essential options : 

| Parameter        | Description          |  Admitted values  |
| ------------- |:-------------:| -----:|
| exchangename     | Name of the exchange where the bots operates | "bter" ; "ccedk" ; "btce" ; "peatio" ; "poloniex" ;  "ccex" ;  |
| apikey      |  Custodian's public key to access the exchange      |  String |
| apisecret |  Custodian's secret key to access the exchange    | String |
| dualside |  If set to true, the bot will behave as a dual side custodian, if false as a sell side custodian.     | true,false |
| nubitaddress | The public address where the custodial grant has been received    |   valid custodian NBT addresses (String) |
| nudport | The RPC port  of the Nu daemon |   1024...65535 |
| rpcpass |  The RPC password of the Nu daemon    |  String |
| rpcuser |  The RPC username  of the Nu daemon    |    String |
| pair |  The currency pair where the bot operates     |   "nbt_usd" |
| mail-recipient | the email at which emergency email has to be sent  |  String  |

With the builds of the bot a sample *options.json* is attached and its will resemble the snipped below :

```json
{"options": 
    {
    "exchangename":"xxx",
    "apikey": "xxx",
    "apisecret": "xxx",
    "dualside": false,
    "nubitaddress": "xxx",
    "nudport": 9091,
    "rpcpass": "xxx",
    "rpcuser": "xxx",
    "pair":"xxx_xxx",
    "mail-recipient":"xxx"
 }
}
  
```

Check the [Under the hood](#hood) section of this document for additional configuration parameters available. 


###3)Run NuBot

Now open a terminal, navigate to the folder of NuBot and execute the jar, specifying the path to the *.json* file you want to use as configuration.

```
java -jar NuBot.jar <path/to/options.json>
```

The bot will start and output messages on the console and write in the */logs* folder. 

To terminate the bot, exit the process with "Control+C"

*Requirements* 
NuBot comes as a cross platform executable jar file to run from command line. It only requires a recent Java Runtime Environment to be installed. Download Java JRE:1.7 [from oracle] (http://www.oracle.com/technetwork/java/javase/downloads/java-se-jre-7-download-432155.html)



# Under the hood
##Additional configuration parameters


###Secondary Peg Parameters
When running the bot against a currency pair different from NBT/USD, the bot needs additional parameters to run.  The custodian must specify the price feeds that the bot must use to track the price of a currency (both fiat and crypto) different from USD.  The price feed will track the live rate between the *secondary peg* currency and USD.

It is sufficient to add a JSON object in the standard options file structured in the following example (for a nbt/usd pair) :

```json
"secondary-peg-options":
        {
            "main-feed":"bter",
            "backup-feeds": {
                "backup1" : { "name" : "bitcoinaverage"},
                "backup2" : { "name" : "coinbase"},
                "backup3" : { "name" : "blockchain"}
                },
            "wallchange-treshold": 0.1,
            "price-distance-threshold":10       
         }
```

####Parameters explanation :

| Parameter        | Description          |  Admitted values  |
| ------------- |:-------------:| -----:|
| main-feed     | the name of the main price feed that has priority over the others | **see following table |
| backup-feeds       |  a json array containing an arbitrary number (>2) of backup price feed names    |   **see following table |
| wallchange-treshold |  how much the price need to change to trigger a wall-shift.    | double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%   |
| price-distance-threshold | for sanity check, the bot checks the feed prices before using it.   If the last price differs for more than <price-distance-threshold%> from the majority of backups, then the bot skips it and tries to use a backup source instead (performing the same sanitycheck). |    double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%  |

#### Available price feed names (can be used as value for *main-feed* and *backup-feeds*)

| Feed name        | Currencies available for tracking   |  Feed |   
| ------------- |:-------------:| -------------:| 
| blockchain    | BTC  |  https://blockchain.info |
| bitcoinaverage    | BTC  |  https://api.bitcoinaverage.com |
| coinbase    | BTC  |  https://coinbase.com |
| btce  | BTC, PPC, ...  |  https://btc-e.com |
| bter    | BTC  |  http://data.bter.com |
| ccedk    | BTC |  https://www.ccedk.com |
| coinmarketcap_no    | PPC |  http://coinmarketcap.northpole.ro |
| coinmarketcap_ne    | PPC |  http://coinmarketcap-nexuist.rhcloud.com |
| bitstampeurusd    | EUR  |  https://bitstamp.com |
| google-unofficial    | EUR,CNY, ...  |  http://rate-exchange.appspot.com |
| yahoo    | EUR,CNY, ...  |  https://yahooapis.com |
| openexchangerates    | EUR,CNY, ...  |  https://openexchangerates.org |
| exchangeratelab    | EUR,CNY, ...  |  https://exchangeratelab.com |


###Additional parameters

NuBot can be further configured by adding additional parameters to its config file. Below a table with an explanation of each parameter and its default value. 


| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| 
| nudip    | "127.0.0.1"  |   | The IP address of the machine that hosts the Nu Client |
| verbose    | false |  if set to true, will print on screen additional debug messages  | boolean |
| hipchat    | true |  f set to false will disable hipchat notifications | boolean |
| mail-notifications    | true |  if set to false will disable email notifications | boolean |
| submit-liquidity    | true  |  if set to false, the bot will not try to submit liquidity info | boolean |
| executeorders    | true |  if set to false the bot will print a warning instead of executing orders  | boolean |
| emergency-timeout    | 60 | max amount of minutes of consecutive failure. After those minute elapses, emergency prodecure starts |  int (minutes) |
| txfee    | 0.2  |  If transaction fee not available from the exchange via api, this value will be used  |  double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5% |
| keep-proceedings    | 0 |  Specific setting for KTm's proposal. Will keep the specified proceedings from sales apart instead of putting 100% of balance on buy . |  double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%|
| priceincrement    | 0.0003 |  if working in sell-side mode, this value (considered USD) will be added to the sell price | double , price increment in expressed USD|
| aggregate | true | If set to false, will nullify the function to put funds back on sell or buy. It will put them back when the walls shift | boolean|


##Adding support for an exchange
*coming soon*
##Design and diagrams

###Sell-side logic

![alt text](https://bytebucket.org/JordanLeePeershares/nubottrading/raw/faa3a5ebbb483372e176e4a8821d7835c2d404fd/readme-assets/bot-case-2.png "NuBot Sell-Side logic")

###Dual-side logic

![alt text](https://bytebucket.org/JordanLeePeershares/nubottrading/raw/faa3a5ebbb483372e176e4a8821d7835c2d404fd/readme-assets/bot-case-1.png "NuBot Dual-Side logic")

##Implementation details
*coming soon*
##HipChat notifications
*coming soon*
##Email notifications
*coming soon*

##Adding SSL certificates for an exchange
In order for the bot to communicate with the exchanges via encrypted https API, it is required to add the SSL certificate of the exchange to the list of trusted certificates locally. NuBot includes the keystore file in its build. The Java JVM uses the Keystore, an encrypted file which contains a [file.jks](../master/NuBot/res/ssl/nubot_keystore.jks) with a collection of authorised certificates.

Using the keytool we can create the keystore 
```
keytool -genkey -alias signFiles -keystore nubot_keystore.jks
```
You will be prompted to enter passwords for the key and keystore.

First we need to get the SSL certificate ( usually .cer).  An easy way is navigate with the browser to the API entry-point, click on the lock icon, and drag the certificate locally. After that the certificate can be added to the bot using keytool :

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
##Download the latest version of the keystore

We are committed to keep the *java keystore* always updated with most recents certificates available. To get the latest available download the most recent version from  the repository : [nubot_keystore.jks](https://bitbucket.org/JordanLeePeershares/nubottrading/src/f82fc9c98ff044afa4af0eaef95199a563cc6250/NuBot/res/ssl/nubot_keystore.jks?at=master). 
and place it in the *res/ssl* folder of NuBot.


##Logging on HTML and csv

The bot produces different output log files, all stored in a special folder created for each session under *logs/*. The bot produces a csv and html log for each session. There 4 levels of log messages : *severe*, *warning*, *info* and *fine*. *fine* are never logged to file (only to console) , info are logged to file if we set`"verbose"=true`, *warning* are used for logging trading-related messagges, and *severe* for errors.

Additionally there are two other logs that trace the history of wall shifts and a history of snapshots of active orders. 

#Changelogs
See [changelog.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/CHANGELOG.md?at=master)

#Contribute
*coming soon*

#License 
NuBot is released under [GNU GPL v2.0](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/LICENSE.md?at=master)