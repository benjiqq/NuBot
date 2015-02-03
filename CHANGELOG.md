## v0.1.5 (2015-01-29)

#### New Features:
  - Multi-custodian mode will sync with remote NTP and reset orders every three minutes
  - Support for NBT-denominated pairs (swapped logic and pricing)
  - Bitcoin.co.id API wrappers
  - exco.in API wrappers
  - Added config parameter(s) to manually adjust the  maximum wall sizes.
  - The bot accept a list of input json files as input and combines them internally
  - Bitfinex and Bitstamp price feeds
  - Submit liquidity info tier 1 and tier 2 using a different identifier


#### Improvements:
  - Reduced time between price-check and wall-shift to close-to-zero
  - Detecting a large price different will pause the bot for one cycle instead of shutting it down
  - BitSpark reporting tool 
  - Debug message at the end of order initialization
  - On shift, cancel orders in a random order to avoid revealing the following movement direction
  - Enhance wall_shifts reporting with .json
  - Reset liquidityinfo on quit
  - Implemented getLastTrades for all supported wrappers
  - Reduced wait time during wall shifts
  - Keep 1NBT-equivalent aside to avoid "not enough balance" error
  - Polished build.xml ant script
  - In HipChat and Emails round numbers to two decimal digits
  - Send critical notifications to a different hipchat room
  - Load fiat currencies from database in CurrencyPair.java
  - GetCurrencyPairFromString, add a lookup table for names
  - Refactored the Strategy extracting re-usable methods to a different class
  - Try to cancel all orders before shutting down the bot
  - Cancel existing orders on startup
  - Sleep time parametrized
  - Display bot version number in logs
  - Renamed price-Offset parameter into price-spread (and compute it as 50% per side)
  - Wait random time before placing an order to ensure competitivity with multiple bots
  - LOG timestamp in UTC
  - Logging format improvements

#### Bug fixes:
  - Looping on moving average detection
  - Bitspark wrong  liquidity reported
  - Reporting open orders in wrong unit
  - BTER handle small amounts of orders
  - BitSpark reporting tool ignoring timestamp 
  - Broken internal links in read.me

## v0.1.4 (2014-11-30)

#### New Features:
  - Implemented an additional system to prevent the risk of NuBot placing orders at low price.
  - Added a configuration parameter  ("keep-proceeds" ) that lets the custodian specify an amount of proceeds from sale to be put apart. This feature is designed specifically for custodians acting in a way similar to KTm.
  - Added support for multiple custodians on the same non-USD pair. 
  - Bitspark API wrappers
  - AllCoin API wrappers

#### Improvements:
  - On exchanges with a 0 transaction fee, force a price spread of 0.1% 
  - Removed the aggregate parameter and made it automatic.
  - NTP Client makes the bots sync.
  - Reset or cancel all orders after price failing to update
  - When computing the initial USD price, take the TX fee into account
  - Computed wait_time on wall shifts
  - Changed the API error-handling system
  - Refactored PriceFeedManager
  - Reduce damount of compulsory option parameters by introducing conditions
  - The bot now uses a dedicated SMTP server to send email notifications
  - Removed debug messages from bterwrapper
  - Removed printing warnings for non-global TX fee
  - Forced order reset on firstTime execution
  - Assigned a name to threads for better runtime debugging
  - Avoid printing buy prices if SELL side only

#### Bug fixes:
  - Bot doesn't stop when connection lost with Peatio. 
  - Solved a problm that led to wrong pricing with a sell-side and a dual-side custodian working on the same market/pair. 
  - Solved Peatio wrapper problems while parsing some date
  - Part of the balance was not placed on  (buy) order


## v0.1.3 (2014-10-28)

#### New Features:
  - Implemented the getLastTrades() method wrapper around exchange's API entry point to retrieve the list of executed trades.
  - Created a stand-alone program to execute the above mentioned method and save the output on a json file. This will facilitate a custodian's reporting capabilities.
  - Released the source of three additional stand-alone programs that respectively execute a list of orders from a csv input list, clear all the active orders on one's account and monitor the price of a secondary asset sending notifications when a threshold is reached. 
  - Added support for poloniex
  - Added support for ccex
  - Added a process that compiles a README.html from the markdown and includes it into the distributed binary packet.  


#### Improvements:
  -  Implemented and tested a solution that allows multiple custodians on the same (non-USD) pair at the same time without having them consuming each others' walls. All decentralized with no need of bot communicating with each other.
  -  Removed options that allowed custodian to control timing of executions to avoid one custodian's instance executing faster/slower than others
  -  Added a control which will prevent the bot to submit liquidityinfo during a wall shift. Thanks @Chronos for spotting this.
  -  Add log message on startup with initial PEG price used.
  -  Renamed sendrpc option parameter to submitliquidity
  -  Refactored the Order model
  -  moved logs assets outside the /logs folder
  - expose a testing option to avoid the wait-time on wall shift
  

#### Bug fixes:
  - CCEDK Nonce bug fixed.
  - Fixed a bug that was preventing the last of the 4 orders to be submitted.
  - Chinese locale settings conflict with some HTML parsing
  - NuBot on Bter - stuck in loop after remote host closed connection during handshake
  - RPC client fails to verify connection status


## v0.1.2 (2014-10-12)

#### New Features:
  - EUR and CNY price feed added ( see updated readme with the new price feeds)
  - Added method getLastTrades in trade interface to return trades happened in the last 24 hours. Implemented for bter and ccedk.
  - New csv output with list of active orders at each time (refreshed when submitting liquidity info)
  - The bot creates a different folder for logs of each session

#### Improvements:
  - Refactored cryptopeg to secondary peg
  - All the percentages parameters are expressed in 0..100 scale
  - Default price increment changed from 0.001 to 0.0003 USD
  - The bot now shows a warning if the nubits wallet is locked
  - Refactored the secondary peg strategy for reduce code duplicates and improve readability
  - Graceful wall shift instead of resetting all orders, it now updates walls one at the time. Need more testing
  - Before timing out, try to clear orders again.
  - In HTML logging, "Trades" became "Warnings/Trades" to accommodate another category of messages.
  - Temporarily disabled the executed order aggregation function, made possible by ad additional parameter : aggegate (boolean) in the options. Now the proceeds from sales and buys are updated on price shift. For NBT/USD pair, the aggregation is still untouched.
  - Hipchat notifications for wall shifts


#### Bug fixes:
  - Fixed the problem where the same custodian running multiple instance of the bot on different pair was overwriting the data. Now when sending RPC liquidity info, the bot sends an additional parameter with an unique identifier generated by combining custodian address with trading market and currency pair. Mike changed the client to aggregate info.
  - Fixed the bug that submitted liquidity info in the wrong unit for ccedk buy orders
  - Got rid of two important exit point that was causing the bot to quit in case of errors.
  - one price feed failing was displaying an error message, while now its just a regular warning.
  - It was required to run the secondary peg bot with at least 3 backup feeds, now its just two.
  - When checking existence of an order, do not return an error if its not found, show a warning instead.
  - A control prevented wallshifts to be saved on filesystem
  - Bter API errors sometime comes with boolean, sometime String . Patched the function handling errors .

## v0.1.1 (2014-09-22)

#### New Features:
  - Bter API
  - CCEDK Api

#### Improvements:
  - Reduced the amount of options necessary to launch the bot

#### Bug fixes:
  - LOG.fine messages now gets print on system output
  - Java .properties file to store general settings instead of static class


## v0.1.0 (2014-09-19)

####Features:

  - Bot NBT/USD pair for sell-side and dual-side custodians
  - Peatio API Wrappers
  - Btce API Wrappers
  - Aggregate orders 
  - HipChat notifications
  - Email notifications
  - RPC Client to interface with Nud
