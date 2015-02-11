# Gradle build info

## Install

 to use, install gradle, version at least 2.0, see http://gradle.org

 dependencies online, except the jars in lib
 (lib-repo not needed https://bitbucket.org/mj2p/nubot-dependancies)

 to install gradle on linux:
 sudo add-apt-repository ppa:cwchien/gradle
 sudo apt-get update
 sudo apt-get install -y gradle

## Dependencies

 dependency: Grip
 https://github.com/joeyespo/grip
 pip install grip
 
## commands

 gradle NuBotJar => package the jar
 
 gradle test => run Unit tests

## dist layout

 NuBot.jar
 /config-sample
 /logs
 /res
 readme.html
 setup.html
 LICENSE.md
 CHANGELOG.md

## Notes

 ant build could be imported with 
 ant.importBuild 'NuBot/build.xml'
 

 