# Gradle build info

## Install

install gradle, version at least 2.0, see http://gradle.org

to install gradle on linux:
 sudo add-apt-repository ppa:cwchien/gradle
 sudo apt-get update
 sudo apt-get install -y gradle

## Dependencies

dependencies online, except the jars in lib

dependency: Grip
 https://github.com/joeyespo/grip
 pip install grip
 
## commands

 gradle NuBotJar => package the jar
 
 gradle test => run Unit tests. HTML Testout under build/reports/tests/index.html

## dist layout

 * NuBot.jar
 * /config-sample
* /logs
* /res
* readme.html
* setup.html
* LICENSE.md
* CHANGELOG.md

## Notes

ant build could be imported with 
ant.importBuild 'NuBot/build.xml'
 

 