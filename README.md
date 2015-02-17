![NuBot Logo](https://bytebucket.org/JordanLeePeershares/nubottrading/raw/faa3a5ebbb483372e176e4a8821d7835c2d404fd/readme-assets/logo.png)

#####Official automated trading bot for NuBits custodians

*Disclaimer : this documentation is currently under-development and is therefore subject to sudden changes*


#What is NuBot?

NuBot is a cross-platform automated trading bot written in java.  
NuBot is a tool that helps [NuBits](https://www.nubits.com) custodians to automate trades.  
As explained in the [white paper](https://nubits.com/about/white-paper), a custodian's core mission is to **help maintain the peg while introducing new currency into the market**.  

[Discuss NuBot with the community](http://discuss.nubits.com/category/nubits/automated-trading)

#Changelogs
See [changelog.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/CHANGELOG.md?at=master)


#How does it work?

Only custodians will use the trading bot and relay liquidity data to other Nu clients. 
Within the Nu system there are two types of custodians: **sell side** and **dual side** custodians. 
*Dual side custodians* are custodians whose specific function is to provide liquidity for compensation, and they will initially only provide buy side price support. Once their buy order for NBT is partially filled, the bot should then create a sell order for that NBT. 
In the case of *sell side custodians*, the liquidity they provide is secondary to another goal such as funding core development, marketing NBT or distributing Peercoin dividends. They want to spend the proceeds of their NBT, so under no circumstance will they provide buy side liquidity.  
NuBot permits a user to indicate they are either a sell side or dual side custodian. This effect the trading bots behavior is detailed in the use cases below.

#Using NuBot

See [SETUP.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/d69252c90538a92acb0f40a0367f0a10a207719e/SETUP.md?at=master)
##Dual-side strategy

First, someone who wishes to fulfill this role must seek shareholder approval via the custodial grant mechanism.  
Say a particular liquidity provider or *LP* custodian has 10 million USD he wishes to use to provide NuBit liquidity. He would expect compensation for lost opportunity cost (he could otherwise invest those funds in rental property, stocks or bonds) and for the risk of loss via an exchange default, such as we have seen with Mt. Gox and others.  
While the market will continually reprice this, let's say in our case the prospective LP custodian decides a 5% return every six months is fair compensation for lost opportunity cost and risk of exchange default. So, he promises shareholders to provide 10 million USD/NBT worth of liquidity for one year in exchange for 500,000 NBT. Shareholders approve this using the grant mechanism and he is granted 500,000 NBT. Now he must provide 10 million in liquidity constantly over the next year. He may do this through a single exchange or multiple exchanges.  
Let's say he does this with a single exchange. He opens an account with the exchange, then deposits $10 million worth of BTC and exchanges the BTC for USD.
Now he is ready to make use of the trading bot.  
An appropriate exchange will expose a trading API and our trading bot implements the API for that specific exchange. Each implementation of a specific exchange API implements an interface to standardize the way the trading bot interacts with these diverse exchange APIs.  
Doing this will allow the LP custodian to enter authentication information into the trading bot for his exchange account. He will then use the user interface in our trading bot to place an buy order for 10,000,000 NBT on the exchange.  
The price will not be exactly one USD, it will be one USD minus the exchange transaction fee. If the exchange charges a 0.2% transaction fee, he will place a buy order for 10,000,000 NBT at a price of 0.998 USD.  
Let us suppose his order is partially filled in the amount of 1,000,000 NBT. Now his exchange account will contain 9,000,000 USD used to fund an order for 9,000,000 NBT. There will also be 1,000,000 NBT in the account. The trading bot automatically and immediately place these 1,000,000 NBT for sale at a price of 1.002 USD (one USD + a 0.2% transaction fee). If this order fills, then the bot should use the USD proceeds to immediately place a buy order for NBT at 0.998 USD. All funds should be continually on order and the LP custodian's funds should not be depleted by transaction fees.
When an order is placed, canceled or filled (even partially), the *liquidityinfo* RPC method is called in the Nu client.


See [Attached Diagrams](#markdown-header-dual-side-logic) for a visual flowchart. 


##Sell-side strategy 

In some cases custodians will spend the NuBits directly and not use the trading bot at all.  
For instance, if core developers accept NuBits as compensation then Jordan Lee will simply distribute NuBits granted to him directly without the need for any exchange.
Let's examine the case where a 10,000,000 NBT custodial grant is given for the purpose of distributing a shareholder dividend in Peercoin.  
Such a custodian will deposit 10,000,000 NBT in a single or multiple exchanges. In our use case we will use a single exchange. Once the NBT deposit is credited, the custodian will start the trading bot, indicate they are a sell side custodian and indicate that orders should be created, although nothing specific about the order should be entered by the user.  
The trading bot should offer the entire balance of NBT for sale using the formula of one USD + transaction fee + one pricing increment.  
Some exchanges allow the fee to be discovered through their API, while others do not. If the fee can be found through the API, it will. If not, the user should be asked to specify the transaction fee.  
Let's say our exchange has a transaction fee of 0.2% and supports 4 decimal places in its order book on the NBT/USD pair. Using our formula above, the trading bot would place an sell order for 10,000,000 NBT at a price of 1.0021.  
The reason it should be 1.0021 instead of 1.002 is that we want dual side sell orders to be executed first, so their funds can be returned to providing buy side liquidity.
Each time an order is placed, cancelled or filled (even partially), the *liquidityinfo* Nu client RPC method is called. Details about this method can be found in the Liquidity Pool Tracking section. 
Calling *liquidityinfo* will have the effect of transmitting the size of the buy and sell liquidity pool the local trading bot is managing to all known Nu peers. 


See [Attached Diagrams](#markdown-header-sell-side-logic) for a visual flowchart. 

##Other strategies

There might be adjusted versions of the two strategies explained above.  
To request a custom build of NuBot to fulfil a particular custodial grant, [get in touch](http://discuss.nubits.com/category/nubits/automated-trading).

Alternative strategies  :
* *Secondary Peg Strategy* : a strategy to let custodian trade of pairs different from NBT/USD .
* *KTms Strategy* : dual side Strategy with x% of proceeds from sales kept in balance. Link to [KTm's custodial proposal](http://discuss.nubits.com/t/proposal-to-operate-a-nubits-grant-to-provide-early-stage-dual-side-liquidity-and-shareholder-dividends/120/25) .


#Under the hood

##Adding support for an exchange
*coming soon*
##Design and diagrams

###Sell-side logic

![alt text](https://bytebucket.org/JordanLeePeershares/nubottrading/raw/faa3a5ebbb483372e176e4a8821d7835c2d404fd/readme-assets/bot-case-2.png "NuBot Sell-Side logic")

###Dual-side logic

![alt text](https://bytebucket.org/JordanLeePeershares/nubottrading/raw/faa3a5ebbb483372e176e4a8821d7835c2d404fd/readme-assets/bot-case-1.png "NuBot Dual-Side logic")

##Adding SSL certificates for an exchange
In order for the bot to communicate with the exchanges API via encrypted https, it is necessary that the SSL certificate of the exchange is added to the local store of trusted certificates.  
NuBot includes the keystore file in its build. The Java JVM uses this keystore, an encrypted file which contains a [file.jks](../master/NuBot/res/ssl/nubot_keystore.jks) with a collection of authorised certificates.

Using the keytool we can create the keystore 
```
keytool -genkey -alias signFiles -keystore nubot_keystore.jks
```
You will be prompted to enter passwords for the key and keystore.

To add a certificate we first need to get the SSL certificate (usually .cer) from the Exchange.  
An easy way is navigate with the browser to the API entry-point, click on the lock icon, and drag the certificate locally.  
After that the certificate can be added to the bot using keytool:

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

We are committed to keep the *java keystore* always updated with most recent certificates available.  
To get the latest available download the most recent version from the repository: [nubot_keystore.jks](https://bitbucket.org/JordanLeePeershares/nubottrading/src/f82fc9c98ff044afa4af0eaef95199a563cc6250/NuBot/res/ssl/nubot_keystore.jks?at=master) and place it in the *res/ssl* folder of NuBot.

#Price Feeds
See [FEEDS.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/FEEDS.md?at=master)

#Contribute
See [CONTRIBUTE.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/CONTRIBUTE.md?at=master)


#License 
NuBot is released under [GNU GPL v2.0](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/LICENSE.md?at=master)
