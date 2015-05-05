function getBaseUrl() {
    return "http://" + location.host;
}

var baseurl = getBaseUrl();
var orderEndPoint = "orders";
var balanceEndPoint = "balances";

var hook = true; //flag that prevents/allow the alert on page change to show up

var refreshStatusInterval = 2 * 1000; //ms

/*
 No matter how fast we set the refreshOrders,
 it will be capped by Server
 in Settings.ORDER_MIN_UPDATE and BALANCE_MIN_UPDATE
*/
var refreshOrders = 4 * 1000;
var refreshBalances = 4 * 1000;

var refreshTablesInterval = 500;
var refreshLogInterval = 300;

var pageName = "operation";
var debug = true;

var dotCounter = 1;
var botRunning = false;

var logLine = 0;
var requestedStop = false;

var laddaToggleBtn; //ladda button for starting and stopping the bot

var laddaSavecfgBtn; //ladda button for saving the config
var laddaClearcfgBtn; //ladda button for clearing config

var progressPB = 0; //current status of progressbar (min 0 max 1)
var incrementPB;
var currentAnimID;
var animatingButton = false;

var serverDown = false;
var mode = "stopped";


function handleFailServer() {
    botRunning = false;
    serverDown = true;
    $('#maincontainer').html("NuBot engine is down. Relaunch it and refresh this page");
}

function clearTables() {
    $("#ordertable").find("tr:gt(0)").remove();
    $("#balancetable").find("tr:gt(0)").remove();
}

function setStateRunning() {
    if( mode != "running")
    {
        console.log("set running");
        $('#togglebot-text').html(" Stop Bot");
        $('#togglebot-text').addClass("glyphicon-off");
        $('#togglebot-text').removeClass("glyphicon-play");

        $("#togglebot").attr("data-color", "red");

        $('#logarea').removeClass("stopped-logarea").addClass("running-logarea");

        document.title = 'Running! - NuBot GUI'
        stopAnimation();
        setNavBarActive();
    }
}

function setStateHalted() {
    if( mode != "halted")
    {
        console.log("set halted");
        $('#togglebot-text').html(" Start Bot");
        $('#togglebot-text').addClass("glyphicon-play");
        $('#togglebot-text').removeClass("glyphicon-off");

        $("#togglebot").attr("data-color", "blue");

        setTimeout(clearTables, refreshTablesInterval);

        document.title = 'NuBot GUI - Stopped';
        $('#logarea').removeClass("running-logarea").addClass("stopped-logarea");
        stopAnimation();
        setNavBarActive();
    }
}

function setStateStarting(){
    if( mode != "starting")
    {
        console.log("set starting");
        // Start loading button
        incrementPB = 0.006; //determines speed of progressbar
        laddaToggleBtn.ladda('start');
        currentAnimID = setInterval(animateProgressBar, 50, true);
        $('#togglebot-text').html(" Starting Bot");

        setNavBarInactive();
    }
}

function setStateHalting(){
    if( mode != "halting")
    {
        console.log("set halting");
        incrementPB = 0.008; //determines speed of progressbar
        laddaToggleBtn.ladda('start');
        currentAnimID = setInterval(animateProgressBar, 50, false);
        $('#togglebot-text').html(" Stopping Bot");

        setNavBarInactive();
    }
}

function startBot() {
    if (confirm("Are you sure you want to start the bot?")) {
        if (debug) console.log("calling start on server");

        setStateStarting();

        var jsondata = JSON.stringify({
            "operation": "start"
        });

        $.ajax(baseurl + "/startstop", {
            data: jsondata,
            dataType: "json",
            type: 'POST',
            success: function(data) {
                console.log("starting bot ok " + data);
                var success = data["success"];

                if (success) {

                } else {
                    alert(data["error"]);
                    stopProgressBarAnimation(false);
                }
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert("startBot - error posting to server " + textStatus + " " + errorThrown);
            }
        });
    }
}


function stopBot() {
    if (debug) console.log("calling stop on server");
    if (confirm("Are you sure you want to stop the bot?")) {

        setStateHalting();

        var jsondata = JSON.stringify({
            "operation": "stop"
        });

        $.ajax(baseurl + "/startstop", {
            data: jsondata,
            dataType: "json",
            type: 'POST',
            success: function(data) {
                var success = data["success"];
                console.log("stopped bot. success " + success);
                var cbtn = $('#togglebot');
                if (success) {
                    //on success of post change the color of the button
                    $('#duration').html("");
                } else {
                    alert(data["error"]);
                    stopProgressBarAnimation(false);
                }
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert("stopBot - error posting to server " + textStatus + " " + errorThrown);
            }
        });
    }
}

function setNavBarInactive(){
    $('#operation-nav').html('<a>Dashboard</a>');
    $('#config-nav').html('<a>Configuration</a');
    $('#docu-nav').html('<a>Documentation</a>');
    $('#disclaimer-nav').html('<a>Disclaimer</a>');

    //<a href='#' onclick="changePage('operation')">Dashboard</a>
      //<a href='#' onclick="changePage('config')">Configuration</a>
        //<a href='#' onclick="changePage('docu')">Documentation</a>
          //><a href='#' onclick="changePage('disclaimer')">Disclaimer</a>
}

function setNavBarActive(){
    $('#operation-nav').html("<a href='#' onclick=\"changePage('operation')\">Dashboard</a>");
    $('#config-nav').html("<a href='#' onclick=\"changePage('config')\">Configuration</a>");
    $('#docu-nav').html("<a href='#' onclick=\"changePage('docu')\">Documentation</a>");
    $('#disclaimer-nav').html("<a href='#' onclick=\"changePage('disclaimer')\">Disclaimer</a>");
}

function updateNavbar(page) {
    if (mode == "halting"){
        alert("Wait for Bot to halt");
        return;
    }

    if (mode == "starting"){
        alert("Wait for Bot to start");
        return;
    }

    $('#operation-nav').removeClass('active');
    $('#config-nav').removeClass('active');
    $('#docu-nav').removeClass('active');

    switch (page) {
        case "operation":
            $('#operation-nav').addClass('active')
            break;
        case "config":
            $('#config-nav').addClass('active')
            break;
        case "docu":
            $('#docu-nav').addClass('active')
            break;
        case "disclaimer":
            $('#disclaimer-nav').addClass('active')
            break;
    }
}

function updateConfigFile() {
    $.ajax({
            type: "GET",
            dataType: "json",
            url: baseurl + "/configfile"
        })
        .fail(function() {
            console.log("error loading configfile");
            handleFailServer();
        })
        .done(function(data) {
            //console.log("config data: " + data);
            $('#configfile').html(data["configfile"]);
        });
}


var first = true;

function updateBalances() {
    //console.log("updatebalance, botrunning="+botRunning);
    if (botRunning) {
        $.ajax({
                type: "GET",
                dataType: "json",
                url: baseurl + "/" + balanceEndPoint
            })
            .fail(function() {
                console.log("error loading info");
                handleFailServer();
            })
            .done(function(data) { //For a sample data see [1] at the bottom of this file :
                //console.log(JSON.stringify(data));
                // prevent update at shutdown

                if (data.hasOwnProperty("pegBalance")) {

                    $("#balancetable").find("tr:gt(0)").remove();

                    var pegTotal = data["pegBalance"]["balanceTotal"];
                    var pegAvailable = data["pegBalance"]["balanceAvailable"];
                    var pegLocked = data["pegBalance"]["balanceLocked"];
                    var pegCurrencyCode = data["pegBalance"]["currencyCode"];

                    var pegHTMLrow = '<tr>' +
                        '<td>' + pegCurrencyCode + '</td>' +
                        '<td align=\'right\'>' + pegTotal + '</td>' +
                        '<td align=\'right\'>' + pegLocked + '</td>' +
                        '<td align=\'right\'>' + pegAvailable + '</td>' +
                        '</tr>';

                    $("#balancetable").find('tbody').after(pegHTMLrow);

                    var nbtTotal = data["nbtBalance"]["balanceTotal"];
                    var nbtAvailable = data["nbtBalance"]["balanceAvailable"];
                    var nbtLocked = data["nbtBalance"]["balanceLocked"];
                    var nbtCurrencyCode = data["nbtBalance"]["currencyCode"];

                    var nbtHTMLrow = '<tr>' +
                        '<td>' + nbtCurrencyCode + '</td>' +
                        '<td align=\'right\'>' + nbtTotal + '</td>' +
                        '<td align=\'right\'>' + nbtLocked + '</td>' +
                        '<td align=\'right\'>' + nbtAvailable + '</td>' +
                        '</tr>';

                    $("#balancetable").find('tbody').after(nbtHTMLrow);
                }
            });

    }
    //pipeline the call
    var updatetime = refreshBalances;
    if (first)
        updatetime += 0.5 * refreshBalances;

    setTimeout(updateBalances, updatetime);
    first = false;
}

function updateOrders() {
    //console.log("updateorders, botrunning="+botRunning);

    if (botRunning) {
        $.ajax({
                type: "GET",
                dataType: "json",
                url: baseurl + "/" + orderEndPoint
            })
            .fail(function() {
                console.log("error loading info");
                handleFailServer();
            })
            .done(function(data) {

                //console.log("buys: " + data["buys"]);
                //console.log("BuyCurrency: " + data["BuyCurrency"]);
                //console.log("SellCurrency: " + data["SellCurrency"]);

                if (data.hasOwnProperty("orders")) {

                    $("#ordertable").find("tr:gt(0)").remove();

                    var orders = data["orders"];
                    //updateFavico(orders.length);
                    //console.log("update badge + "+orders.length);
                    for (var i = 0; i < orders.length; i++) {
                        var order = orders[i];
                        var type = order["type"];
                        var qty = order["amount"]["quantity"];
                        var price = order["price"]["quantity"];
                        var rowhtml = '<tr><td>' + type + '</td><td align=\'right\'>' + qty + '</td><td align=\'right\'>' + price + '</td></tr>';
                        $("#ordertable").find('tbody').after(rowhtml);
                    }
                }

            });
    }
    setTimeout(updateOrders, refreshOrders);
}

function updateStatus() {
    $.ajax({
            type: "GET",
            dataType: "json",
            url: baseurl + "/opstatus"
        })
        .fail(function() {
            handleFailServer();
        })
        .done(function(data) {
            console.log("status data: " + JSON.parse(JSON.stringify(data)));
            console.log("status : " + data["status"]);

            $('#botstatus').html(data["status"]);

            $('#sessionstart').html(data["sessionstart"]);

            $('#duration').html(data["duration"]);

            //we polled the server and now set the client status
            //we set the state depending on the status with the setState functions
            var newmode = data["status"];

            console.log("newmode " + newmode + " oldmode " + mode);

            if (newmode == "running" && mode != "running"){
                setStateRunning();
            }

            if (newmode == "starting" && mode != "starting"){
                setStateStarting();
            }

            if (newmode == "halting" && mode != "halting") {
                setStateHalting();
            }

            if (newmode == "halted" && mode != "halted") {
                setStateHalted();
            }

            mode = newmode;

            /*if (pageName == "operation" && !animatingButton) {
                toggleBotButton(run);
            } else if (pageName == "config") {
                updateConfigElements(run);
            }*/
        });

    setTimeout(updateStatus, refreshStatusInterval);
}


$(document).ready(function() {

    updateConfigFile();

    $('#togglebot').click(function() {

        if (mode == "running"){
           stopBot();
        }

        if (mode == "halted") {
            startBot();
        }

        //ignore starting and halting


    });
}); //end document.ready function

function autoScroll() {
    //autoscrolling. not used - how to make log readable also?
    var psconsole = $('#logarea');
    if (psconsole.length)
        psconsole.scrollTop(psconsole[0].scrollHeight - psconsole.height());

}

function updateLog() {

    var url = baseurl + "/logdump";

    //console.log("url  " + url);
    $.ajax({
            type: "GET",
            dataType: "json",
            url: url,
        })
        .fail(function() {
            console.log("error loading log");
            handleFailServer();
        })
        .done(function(data) {
            $('#logarea').val(data["log"]);
        });

    logLine++;


    if ($('#autoscrollCheckbox').prop('checked'))
        autoScroll();

    setTimeout(updateLog, refreshLogInterval);

}

function loadconfig() {

    var url = baseurl + "/config";

    $.ajax({
            type: "GET",
            dataType: "json",
            url: url,
        })
        .done(function(data) {

            $("#exchangename").attr("value", data["exchangeName"]);
            $("#apikey").attr("value", data["apiKey"]);
            $("#apisecret").attr("value", data["apiSecret"]);
            $("#txfee").attr("value", data["txFee"]);
            $("#pair").attr("value", data["pair"]);
            $("#dualside").attr("checked", data["dualSide"]);
            $("#multiplecustodians").attr("checked", data["multipleCustodians"]);
            $("#executeorders").attr("checked", data["executeOrders"]);
            $("#verbose").attr("checked", data["verbose"]);
            $("#hipchat").attr("checked", data["hipchat"]);
            $("#mailnotifications").attr("value", data["mailnotifications"]);
            $("#mailrecipient").attr("value", data["mailRecipient"]);
            $("#emergencytimeout").attr("value", data["emergencyTimeout"]);
            $("#keepproceeds").attr("value", data["keepProceeds"]);
            $("#maxsellvolume").attr("value", data["maxSellVolume"]);
            $("#maxbuyvolume").attr("value", data["maxBuyVolume"]);
            $("#priceincrement").attr("value", data["priceIncrement"]);
            $("#submitliquidity").attr("checked", data["submitliquidity"]);
            $("#nubitaddress").attr("value", data["nubitAddress"]);
            $("#nudip").attr("value", data["nudIp"]);
            $("#nudport").attr("value", data["nudPort"]);
            $("#rpcpass").attr("value", data["rpcPass"]);
            $("#rpcuser").attr("value", data["rpcUser"]);
            $("#wallchangethreshold").attr("value", data["wallchangeThreshold"]);
            $("#spread").attr("value", data["spread"]);
            $("#mainfeed").attr("value", data["mainFeed"]);
            $("#backupfeeds").attr("value", data["backupFeeds"]);

        });
}

function postall() {
    var posturl = baseurl + "/config";
    laddaSavecfgBtn.ladda('start');
    //get all vars and post them

    var exchangename = $("#exchangename").attr("value");
    var apikey = $("#apikey").attr("value");
    var apisecret = $("#apisecret").attr("value");
    var txfee = $("#txfee").attr("value");
    var pair = $("#pair").attr("value");
    var dualside = $("#dualside").prop("checked");
    var multiplecustodians = $("#multiplecustodians").prop("checked");
    var executeorders = $("#executeorders").prop("checked");
    var verbose = $("#verbose").prop("checked");
    var hipchat = $("#hipchat").prop("checked");
    var mailnotifications = $("#mailnotifications").attr("value");
    var mailrecipient = $("#mailrecipient").attr("value");
    var emergencytimeout = $("#emergencytimeout").attr("value");
    var keepproceeds = $("#keepproceeds").attr("value");
    var maxsellvolume = $("#maxsellvolume").attr("value");
    var maxbuyvolume = $("#maxbuyvolume").attr("value");
    var priceincrement = $("#priceincrement").attr("value");
    var submitliquidity = $("#submitliquidity").prop("checked");
    var nubitaddress = $("#nubitaddress").attr("value");
    var nudip = $("#nudip").attr("value");
    var nudport = $("#nudport").attr("value");
    var rpcpass = $("#rpcpass").attr("value");
    var rpcuser = $("#rpcuser").attr("value");
    var wallchangethreshold = $("#wallchangethreshold").attr("value");
    var spread = $("#spread").attr("value");
    var mainfeed = $("#mainfeed").attr("value");

    var backupfeedsString = $("#backupfeeds").attr("value");
    var backupfeedsarray = backupfeedsString.split(',');

    //could check on the client side if valid post

    var jsondata = JSON.stringify({
        "exchangename": exchangename,
        "apikey": apikey,
        "apisecret": apisecret,
        "txfee": txfee,
        "pair": pair,
        "dualside": dualside,
        "multiplecustodians": multiplecustodians,
        "executeorders": executeorders,
        "verbose": verbose,
        "hipchat": hipchat,
        "mailrecipient": mailrecipient,
        "mailnotifications": mailnotifications,
        "emergencytimeout": emergencytimeout,
        "keepproceeds": keepproceeds,
        "maxsellvolume": maxsellvolume,
        "maxbuyvolume": maxbuyvolume,
        "priceincrement": priceincrement,
        "submitliquidity": submitliquidity,
        "nubitaddress": nubitaddress,
        "nudip": nudip,
        "nudport": nudport,
        "rpcpass": rpcpass,
        "rpcuser": rpcuser,
        "wallchangethreshold": wallchangethreshold,
        "spread": spread,
        "mainfeed": mainfeed,
        "backupfeeds": backupfeedsarray

    });

    makePostConfig(posturl, jsondata);
}

function makePostConfig(url, jsondata) {
    //make HTTP post to server

    $.ajax(url, {
        data: jsondata,
        dataType: "json",
        type: 'POST',
        success: function(data) {

            console.log("got data " + data);

            // server returns custom error message
            var success = data["success"];
            console.log("success " + success);
            var errormsg = data["error"];
            console.log("error " + errormsg);

            if (success) {
                    //stop loading after 1000 msec
                    setTimeout(function() {
                            laddaSavecfgBtn.ladda('stop');
                    }, 1000);
            } else {
                alert(errormsg);
                //Stop loading right away
                laddaSavecfgBtn.ladda('stop');
            }
        },

        error: function(xhr, textStatus, errorThrown) {
            alert("makePostConfig - error posting to server " + textStatus + " " + errorThrown);
            alert(xhr.responseText);
        }
    });
}

function postreset() {
    if (confirm("The bot will use a clean config saved in config/nubot-config.json. Are you sure?")) {
        laddaClearcfgBtn.ladda('start');
        var posturl = baseurl + "/configreset";

        $.ajax(posturl, {

            contentType: 'application/json',
            type: 'POST',
            success: function(data) {

                //on success of post change the color of the button
                var rbtn = $('#resetbutton');


                //stop loading after 1000 msec
                setTimeout(function() {
                        laddaClearcfgBtn.ladda('stop');
                }, 1000);


                //reset values from server
                updateConfigFile();
                loadconfig();
            },

            error: function(xhr, textStatus, errorThrown) {

                laddaClearcfgBtn.ladda('stop');

                alert("PostReset - error posting to server " + textStatus + " " + errorThrown);
                alert(xhr.responseText);
            }
        });
    }
}

function updateIframe(pageName) {
    $('#docu-iframe').attr('src', 'docs/' + pageName + '.html');
}

function startupPage(page_name) {
    pageName = page_name;
    switch (page_name) {
        case "operation":
            updateNavbar("operation");
            // Create a new instance of ladda for the specified button
            laddaToggleBtn = $('#togglebot').ladda();

            updateStatus("operation");
            updateLog();

            updateBalances();
            updateOrders();
            break;
        case "config":
            updateNavbar("config");

            laddaSavecfgBtn = $('#saveconfigbutton').ladda();
            laddaClearcfgBtn = $('#resetbutton').ladda();

            updateStatus("config");
            loadconfig();
            break;
        case "docu":
            updateNavbar("docu");
            break;
        case "disclaimer":
            updateNavbar("disclaimer");
            break;
    }
}

function changePage(page_name) {
    hook = false; //this flag will prevent the alert to show up on page change
    var newUrl = window.location.href; // default to same
    switch (page_name) {
        case "operation":
            newUrl = '/';
            break;
        case "config":
            newUrl = '/configui';
            break;
        case "docu":
            newUrl = '/docu';
            break;
        case "disclaimer":
            newUrl = '/disclaimer';
            break;
    }
    document.location.href = baseurl + newUrl;
}

function animateProgressBar(toggle) {

    progressPB += incrementPB;
    if (progressPB >= 1) {
        stopProgressBarAnimation(toggle);
    } else {
        animatingButton = true;
        laddaToggleBtn.ladda('setProgress', progressPB);
    }
}

function stopAnimation() {
    clearInterval(currentAnimID);
    progressPB = 0;
    laddaToggleBtn.ladda('stop');
    animatingButton = false;
}

function stopProgressBarAnimation(toggle) {
    stopAnimation();
}

function updateConfigElements(running) {
    $('#saveconfigbutton').prop('disabled', running);
    $('#resetbutton').prop('disabled', running);
}

function stopServer() {
      if (confirm("Are you sure you want to shutdown the server?")) {
          var url = baseurl + "/stopserver";

          //console.log("url  " + url);
          $.ajax({
                  type: "GET",
                  dataType: "json",
                  url: url,
              })
              .fail(function() {
                   hook = false;
                   alert("Server stopped.");
                   location.reload();

              })
              .done(function(data) {
                  //It cannot succed
              });
          }
}


window.onbeforeunload = function(e) {
    if (botRunning && hook) {
        e = e || window.event;
        var confirmMessage = "The bot is still running."
            // For IE and Firefox prior to version 4
        if (e) {
            e.returnValue = confirmMessage;
        }
        // For Safari
        return confirmMessage;
    } else if (!serverDown && hook) {
        e = e || window.event;
        var confirmMessage = "Closing this window will not stop the server. Use the red button if you want to shut down server before leaving this page."
            // For IE and Firefox prior to version 4
        if (e) {
            e.returnValue = confirmMessage;
        }
        // For Safari
        return confirmMessage;
    }

};


/*

    1. Sample Data -------------------

    {
      "pegBalance": {
        "balanceTotal": "0.02201121",
        "balanceLocked": "0",
        "balanceAvailable": "0.02201121",
        "currencyCode": "BTC"
      },
      "buys": 0,
      "sells": 1,
      "nbtBalance": {
        "balanceTotal": "9.53076606",
        "balanceLocked": "2.5",
        "balanceAvailable": "7.03076606",
        "currencyCode": "NBT"
      },
      "orders": [
        {
          "id": "46533",
          "insertedDate": "Apr 23, 2015 12:13:37 PM",
          "type": "SELL",
          "pair": {
            "orderCurrency": {
              "fiat": false,
              "code": "NBT",
              "extendedName": "NuBit"
            },
            "paymentCurrency": {
              "fiat": false,
              "code": "BTC",
              "extendedName": "Bitcoin"
            }
          },
          "amount": {
            "quantity": 2.5,
            "currency": {
              "fiat": false,
              "code": "NBT",
              "extendedName": "NuBit"
            }
          },
          "price": {
            "quantity": 0.00439,
            "currency": {
              "fiat": false,
              "code": "BTC",
              "extendedName": "Bitcoin"
            }
          },
          "completed": true
        }
      ]
    }
 <end[1]> -------------
 */