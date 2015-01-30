# Setting up NuBot
A tutorial for custodians

##Disclaimer . Use NuBot at your own risk

*PLEASE BE AWARE THAT AUTOMATED TRADING WITH NUBOT MAY BE RISKY, ADDICTIVE, UNETHICAL OR ILLEGAL. ITS MISUSE MAY ALSO CAUSE FINANCIAL LOSS. NONE OF THE AUTHORS, CONTRIBUTORS, ADMINISTRATORS, OR ANYONE ELSE CONNECTED WITH NUBITS, IN ANY WAY WHATSOEVER, CAN BE RESPONSIBLE FOR THE USE YOU MAKE OF NUBOT*

By using NuBot you declare to have accepted the afore-mentioned risks.

##Requirements for a correct functioning 

* The machine that runs NuBot must be connected to the internet
* The custodian must provide the bot with access to market exchanges where NuBits are traded
* The custodian must avoid manual interaction with the exchange while the automated bot is operating. Do not try to trade, do not try to deposit/withdraw funds. 
* To communicate *liquidityinfo*, the custodian should possess an unlocked NuBit client which controls the custodial grant address. 
* Before running the bot on a market where another instance of NuBot is operating, make sure to reach an agreement with the other operator on the price-feed to be used.

### Setup overview.
TL; DR  Make sure you run both the NuBits client and NuBot  on a machine connected to the internet 24/7, configure NuBits client to accept RCP commands, configure NuBot, launch it and monitor it.

Detailed tutorial below : 

###0) Setup the machine

NuBot can run on a computer, on a raspberry pi, or a remote VPN, as long as it has a permanent connection. The choice is up to you. It only requires a machine with 24/7 connection and recent Java Runtime Environment to be installed.  
NuBot comes as a cross platform executable jar file to run from command line. 

Type `java -version` in your terminal to make sure you have JRE >= 1.7 installed on your machine, otherwise download Java JRE:1.7 [from oracle's download page](http://www.oracle.com/technetwork/java/javase/downloads/java-se-jre-7-download-432155.html)

In the future we will make available a docker container pre-prepared. 

###1) Prepare the NuBits client 

To function correctly, NuBot needs to communicate with the NuBit client of the custodian. The bot needs to broadcast liquidity notification to NuNet. By doing so, it allow shareholders to have real-time information about active orders in the network across custodians, and react to it.  It does so by interacting via the *NuBits client of the custodian*. 

Install the latest version of the NuBit client [from the official website](https://nubits.com/download) and let the client sync with the network (downloading the whole blockchain from scratch can take a while). 

*NOTE: Is it also possible to test NuBot without being a custodian and setting the `"submit-liquidity":false` in the configuration file. In this case you can ignore this section.*

To allow the bot to communicate with the Nu client via RCP over HTTP, open your *nu.conf* and add make sure you have it properly configured: [Read this bitcoin tutorial to locate the data directory](https://en.bitcoin.it/wiki/Data_directory#Default_Location).  

*nu.conf*  sample configuration
```
server=1
rpcuser=<choose-a-username>
rpcpassword=<choose-a-password>
port=9090
rpcport=9091
rpcallowip=<ip_of_machine_running_the_bot>*
```

*NOTE: if you plan using NuBot and NuBits on the same machine, the last parameter can be omitted. If you plan to run the bot on a different machine, you must authorise remote calls to your Nu client.

Restart the NuBits client to make changes effective. Make sure that the NuBit client controls a public address which received a custodial grant.  If the NuBits wallet is encrypted with a passphrase, make sure to unlock it, otherwise it won't accept RPC calls to *liquidityinfo*. Unlocking the wallet *for minting only* is not enough.  
  
The two Nu wallets are locked/unlocked separately. To unlock the NuBits wallets, first make sure the client is displaying the NuBits units, then open the console and type:
```
walletpassphrase <your passphrase> 9999999999 
```
The command above will unlock the NBT wallet for 9999999999 seconds, ~ 300 years. That should be enough time for your bot to keep the peg!

###2) Configure the NuBot

**Download and unzip the latest stable build from the [download page](https://bitbucket.org/JordanLeePeershares/nubottrading/downloads).**

The bot reads configuration options from one or multiple *.json* files.  Below you can find a list of all the available options parameter along a brief description. 
Please make sure you fully understand in which way changing a setting will affect the bot before doing anything that can lead to losses. 

Its good practice separating configuration parameters in different files. In this tutorial we will use the following grouping : 
* market options: exchange keys and market-related settings ;  
* misc options: miscellaneus configuration parameters ;
* liquidity-info options :  define nubits client communication ;
* price-tracking options:  for non-USD pairs, define price feeds ;

The same structure is used in the `config-sample`files provided with the bot.  You can edit the provided sample file or create new configuration files.


#### market options 

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| exchangename     | / | Name of the exchange where the bots operates |  see list of accepted exchangenames* |
| apikey      |  / | Custodian's public key to access the exchange . *this param is optional for ccex*     |  String |
| apisecret |  / |  Custodian's secret key to access the exchange    | String |
| txfee    | 0.2  |  If transaction fee not available from the exchange via api, this value will be used  |  double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5% |
| pair |  The currency pair where the bot operates     |   valid currency pair for the specified  eg. "nbt_usd" |

** List of accepted names : "bter" ; "ccedk" ; "btce" ; "peatio" ; "poloniex" ;  "ccex" ; "excoin" ;  "bitcoinid" 

Sample file : *markets.json*: 
```json
{"options": 
    {
    "exchangename":"bter",
    "apikey": "xxx",
    "apisecret": "xxx",
    "txfee": 0.2,
    "pair":"nbt_btc"
 }
}
  
```

#### misc options

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| dualside | / |  If set to true, the bot will behave as a dual side custodian, if false as a sell side custodian.     | true,false |
| multiple-custodians    | false |  if set to true, will sync with remote NPT and reset orders often  | boolean |
| executeorders    | true |  if set to false the bot will print a warning instead of executing orders  | boolean |
| verbose    | false |  if set to true, will print on screen additional debug messages  | boolean |
| hipchat    | true |  f set to false will disable hipchat notifications | boolean |
| mail-notifications    | true |  if set to false will disable email notifications | boolean |
| mail-recipient | / |  the email at which emergency email are sent  |  String  |
| emergency-timeout    | 60 | max amount of minutes of consecutive failure. After those minute elapses, emergency prodecure starts |  int (minutes) |
| keep-proceeds    | 0 |  Specific setting for KTm's proposal. Will keep the specified procees from sales apart instead of putting 100% of balance on buy . |  double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%|
| max-sell-order-volume | 0 | maximum volume to put on sell walls.  |  double , expressed in NBT  |
| max-buy-order-volume | 0 | maximum volume to put on buy walls.  |  double , expressed in the peg currency  |
| priceincrement    | 0.0003 |  if working in sell-side mode, this value (considered USD) will be added to the sell price | double , price increment in expressed USD |

Sample file : *misc.json*: 
```json
{"options": 
    {
    "dualside": true,  
    "multiple-custodians":false,
    "submit-liquidity":true,
    "executeorders":false,
    "verbose":false,
    "hipchat":true,
    "mail-notifications":false,
    "mail-recipient":"xxx@xxx.xxx",
    "emergency-timeout":60,
    "keep-proceeds":0,
    "max-sell-order-volume" : 0,
    "max-buy-order-volume" : 0,
    "priceincrement": 0.1,
 }
}
  
```


#### liquidity-info options

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| submit-liquidity    | true  |  if set to false, the bot will not try to submit liquidity info. If set to false, it will also allow the custodian to omit the declaration of *nubitaddress* ,  *nudport* , *rpcuser* and *rpcpass*  | boolean |
| nubitaddress | / | The public address where the custodial grant has been received    |   valid custodian NBT addresses (String) |
| nudip    | "127.0.0.1"  |  The IP address of the machine that hosts the Nu Client |  IP address (String) |
| nudport | / |The RPC port of the Nu daemon |   1024...65535 |
| rpcpass | / |  The RPC password of the Nu daemon    |  String |
| rpcuser | / |  The RPC username  of the Nu daemon    |    String |

Sample file : *liquidity-info.json*: 
```json
{"options": 
    {
    "submit-liquidity":true,
    "nubitaddress": "xxx",
    "nudip": "127.0.0.1",
    "nudport": 9091,
    "rpcpass": "xxx",
    "rpcuser": "xxx"
 }
}
  
```

#### price-tracking options 
When running the bot against a currency pair different from NBT/USD, the bot needs additional parameters to run.   
The custodian must specify the price feeds that the bot must use to track the price of a currency (both fiat and crypto) different from USD.   
The price feed will track the live rate between the *secondary peg* currency and USD.

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| wallshift-threshold | / | how much the price need to change to trigger a wall-shift.    | double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%   |
| spread  | / | the spread between buy and sell walls | double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%|
| main-feed  | / | the name of the main price feed that has priority over the others | **see following table |
| backup-feeds  | / |  a json array containing an arbitrary number (>2) of backup price feed names    |   **see following table |

See [FEEDS.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/5ef7ead8a435ef0e142dc07de3a0405569da0ecc/FEEDS.md?at=master) for an updated list of valid feed names.


Sample file : *price-tracking.json*: 
```json
{"options": 
    {
     "secondary-peg-options":
    {
            "wallshift-threshold": 0.3,
            "spread": 0,
            "main-feed":"bitfinex",
            "backup-feeds": {
                "backup1" : { "name" : "blockchain"},
                "backup2" : { "name" : "coinbase"},
                "backup3" : { "name" : "bitstamp"}
                }
    }
 }
}
  
```


You can also merge all the configuration parameter under a unique .json file, although we do not recommend it.

###3) Run NuBot


Now open a terminal, navigate to the folder of NuBot and execute the jar, specifying the path to the *.json* file(s) you want to use as configuration.
This is the syntax : 
```
java -jar NuBot.jar <path/to/options.json> [path/to/options-part2.json] ... [path/to/options-partN.json]
```

You can also use nohup in *nix system to redirect the output, and run it in background with the `&` char. For, example, if you followed the structured configuration files explained above you can run nubot with :  

```
nuhop java -jar NuBot.jar market.json misc.json liquidity-info.jon price-tracking.json  & 
```

The bot will start and write output in the */logs* folder. 

To terminate the bot, exit the process with "Ctrl+C" : the bot will clear our liquidityinfo and orders. 


##Logging on HTML and csv

The bot produces different output log files, all stored in a special folder created for each session under *logs/*.  
The bot creates a csv and html log for each session. There 4 levels of log messages : *severe*, *warning*, *info* and *fine*. 
*fine* are never logged to file (only to console).
*info* are logged to file if we set`"verbose"=true`. 
*warning* are used for logging trading-related messages.
*severe* are used for errors.

Additionally there are two other logs that trace the history of wall shifts and a history of snapshots of active orders. 
