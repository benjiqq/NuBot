# NuBot contributions

## Development process

The git repository for NuBot is located at https://bitbucket.org/JordanLeePeershares/nubottrading

To be able to provide pull requests an account with Bitbucket is needed.

NuBot development process follows the git-flow model. For more information see http://nvie.com/posts/a-successful-git-branching-model/

Features are developed in new branches assigned to a Bitbucket issue, i.e. branch feature/123 is development with regards to issue 123.

## Dependencies

Dependecies are currently located here: https://bitbucket.org/mj2p/nubot-dependancies . This repo has to be cloned into the lib directory. This might change in the near future with introduction of a build-tool.

Grip is used to compile markdown into HTML, see https://github.com/joeyespo/grip

## Language and IDE

NuBot is developed in Java7. There is no recommendation about an IDE. Commonly used ones are Eclipse, Netbeans and IntelliJ.

## Compiling

After clone nubot and preparing the dependencies an exchange account is needed.

Also the following password settings have to be set. Create a new class under src/global/Passwords.java and add:

public static final String SMTP_USERNAME = "nubot@example.com"; //used to send mail notifications
public static final String SMTP_PASSWORD = ":asdfasdf:";
public static final String SMTP_HOST = "mail.example.com";
public static final String HIPCHAT_KEY = "xyz"; //not admin
public static final String HIPCHAT_NOTIFICATIONS_ROOM_ID = "826590";
public static final String HIPCHAT_CRITICAL_ROOM_ID = "1016112"; //use for critical notifications
public static final String OPEN_EXCHANGE_RATES_APP_ID = "abc"; //https://openexchangerates.org/api/latest.json?app_id=<here>
public static final String EXCHANGE_RATE_LAB = "xyz"; //http://api.exchangeratelab.com/api/current?apikey=<here>

## Test Exchange

A test exchange is located at http://178.62.186.229. Accounts for testing can be created on request.

## Nofications

HipChat is used for notifications.

## Jenkins

Jenkins is used. TODO: more info here.

## Trello

currently not in use. Management takes place over the forum or Hipchat or Bitbucket.

## AssitantBot

see https://bitbucket.org/mj2p/assistantbot
