# Setting up NuBot
A tutorial for custodians and users of NuBot.

##Disclaimer . Use NuBot at your own risk

*PLEASE BE AWARE THAT AUTOMATED TRADING WITH NUBOT MAY BE RISKY, ADDICTIVE, UNETHICAL OR ILLEGAL. ITS MISUSE MAY ALSO CAUSE FINANCIAL LOSS. NONE OF THE AUTHORS, CONTRIBUTORS, ADMINISTRATORS, OR ANYONE ELSE CONNECTED WITH NUBITS, IN ANY WAY WHATSOEVER, CAN BE RESPONSIBLE FOR THE USE YOU MAKE OF NUBOT*

By using NuBot you declare to have accepted the afore-mentioned risks. See the DISCLAIMER.md for detailed terms.

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

NuBot can run on a computer, on a raspberry pi, or a remote VPS, as long as it has a permanent connection. The choice is up to you. It only requires a machine with 24/7 connection and recent Java Runtime Environment to be installed.
NuBot comes as a cross platform executable jar file to run from command line. 

Type `java -version` in your terminal to make sure you have JRE >= 1.8 installed on your machine, otherwise download Java JRE:1.8[from oracle's download page](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)

Or install via apt-get 
```
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer
```

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

*Download and unzip the latest stable build from the [download page](https://bitbucket.org/JordanLeePeershares/nubottrading/downloads).*

The bot reads configuration options from one *.json* file.  Below you can find a list of all the available options parameter along a brief description. 
Please make sure you fully understand in which way changing a setting will affect the bot before doing anything that can lead to losses. 

In this tutorial we will use the following grouping : 
* market options: exchange keys and market-related settings ;  
* misc options: miscellaneous configuration parameters ;
* liquidity-info options: define nubits client communication ;
* price-tracking options: for non-USD pairs, define price feeds ;

Refer to the `sample-config.json` file provided with the bot. You can edit the provided sample file or create a new configuration file.

Sample options:
```json
{
  "exchangename":"poloniex",
  "apikey": "xxx",
  "apisecret": "xxx",
  "txfee": 0.2,
  "pair":"nbt_btc",

  "dualside": true,
  "multiplecustodians":false,
  "executeorders":true,
  "verbose":false,
  "hipchat":true,
  "mailnotifications":"severe",
  "mailrecipient":"xxx@xxx.xxx",
  "emergencytimeout":60,
  "keepproceeds":0,
  "maxsellordervolume" : 0.0,
  "maxbuyordervolume" : 0.0,
  "priceincrement": 0.1,

  "submitliquidity":true,
  "nubitaddress": "xxx",
  "nudip": "127.0.0.1",
  "nudport": 9091,
  "rpcpass": "xxx",
  "rpcuser": "xxx",

  "wallchangeThreshold": 0.1,
  "spread":0.0,
  "mainfeed":"blockchain",
  "backupfeeds": ["coinbase", "btce"]
}

```
---
#### Market options 

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| exchangename     | / | Name of the exchange where the bots operates |  **see list of accepted exchange names |
| apikey      |  / | Custodian's public key to access the exchange . *this param is optional for ccex*     |  String |
| apisecret |  / |  Custodian's secret key to access the exchange    | String |
| txfee    | 0.2  |  If transaction fee not available from the exchange via api, this value will be used  |  double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5% |
| pair |  The currency pair where the bot operates     |   valid currency pair for the specified  eg. "nbt_usd" |

See EXCHANGES.md for an updated list of valid exchange names.

---

#### Misc options

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| dualside | / |  If set to true, the bot will behave as a dual side custodian, if false as a sell side custodian.     | true,false |
| multiplecustodians    | false |  if set to true, will sync with remote NPT and reset orders often  | boolean |
| executeorders    | true |  if set to false the bot will print a warning instead of executing orders  | boolean |
| verbose    | false |  if set to true, will print on screen additional debug messages  | boolean |
| hipchat    | true |  if set to false will disable hipchat notifications | boolean |
| mailnotifications    | severe |  set notification level: none at all, all: including non-critical, severe: only critical | String ("none", "all", "severe") |
| mailrecipient | / |  the email to which emergency email are sent  |  String  |
| emergencytimeout    | 60 | max amount of minutes of consecutive failure. After those minute elapse, emergency procedure starts |  int (minutes) |
| keepproceeds    | 0 |  Specific setting for KTm's proposal. Will keep the specified proceeds from sales apart instead of putting 100% of balance on buy . |  double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%|
| maxsellordervolume | 0 | maximum volume to put on sell walls.  |  double , expressed in NBT . 0=no limit; |
| maxbuyordervolume | 0 | maximum volume to put on buy walls.  |  double , expressed NBT. 0=no limit;  |
| priceincrement    | 0.0003 |  if working in sell-side mode, this value (considered USD) will be added to the sell price | double , price increment in expressed USD |


---

#### Liquidity-info options

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| submitliquidity    | true  |  if set to false, the bot will not try to submit liquidity info. If set to false, it will also allow the custodian to omit the declaration of *nubitaddress* ,  *nudport* , *rpcuser* and *rpcpass*  | boolean |
| nubitaddress | / | The public address where the custodial grant has been received    |   valid custodian NBT addresses (String) |
| nudip    | "127.0.0.1"  |  The IP address of the machine that hosts the Nu Client |  IP address (String) |
| nudport | / |The RPC port of the Nu daemon |   1024...65535 |
| rpcpass | / |  The RPC password of the Nu daemon    |  String |
| rpcuser | / |  The RPC username  of the Nu daemon    |    String |


---

#### Price-tracking options 
When running the bot against a currency pair different from NBT/USD, the bot needs additional parameters to run.   
The custodian must specify the price feeds that the bot must use to track the price of a currency (both fiat and crypto) different from USD.   
The price feed will track the live rate between the *secondary peg* currency and USD.

Parameters : 

| Parameter      |  Default value  |  Description  |   Admitted values  | 
| ------------- |:-------------:| -------------:| -------------:| 
| wallchangeThreshold | / | how much the price need to change to trigger a wall-shift.    | double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%   |
| spread  | / | the spread between buy and sell walls | double. Expressed in absolute percentage. 10 = 10% , 0.5 = 0.5%|
| mainfeed  | / | the name of the main price feed that has priority over the others | **see following table |
| backupfeeds  | / |  a json array containing an arbitrary number (>2) of backup price feed names    |   **see following table |

See [FEEDS.md](https://bitbucket.org/JordanLeePeershares/nubottrading/src/03332b2637c5a67d9abd31b1c7bf1f37c239bc71/md/FEEDS.md?at=master) for an updated list of valid feed names.

---


###3) Run NuBot


Now open a terminal, navigate to the folder of NuBot and execute the jar, specifying the path to the *.json* file(s) you want to use as configuration.
This is the syntax : 
```
java -jar NuBot.jar -cfg=sample-config.json [-GUI]
```

You can also use nohup in *nix system to redirect the output, and run it in background with the `&` char. For, example, if you followed the structured configuration files explained above you can run nubot with :  

```
nohup java -jar NuBot.jar sample-config.json
```

The [optional] flug -GUI will spin up the UI. 

The bot will start and write output in the */logs* folder. 

To terminate the bot, exit the process with "Ctrl+C" : the bot will clear our liquidityinfo and orders.

##Logging files

The bot produces different output log files, all stored in a special folder created for each session under *logs/*.  
On startup, NuBot prints out the name of the folder it is using to log:

example: 
```
INFO  - defined session path logs/session_1427479041022
```

Log files: 

| filename    |  Description  | 
| ------------- |:-------------:| 
| standard.html    | Standard output of the bot  | 
| verbose.html    | Verbose output of the bot with additional messages | 
| orders_history.(csv;json) | snapshots of active orders (taken every minute) |
| balance_history.json    | snapshots of balances (taken every minute)   | 
| wall_shifts.(csv;json) | list of wall shifts | 


NOTE: to avoid huge files, html files gets rotated at 50MB.

Additional messages are logged to console if the option `"verbose"=true` is set. Useful for debug

Additionally there are two other logs that trace the history of wall shifts and a history of.

For additional control over logging, the user can also manually edit the *config/logging/logback.xml* configuration file.

