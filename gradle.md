# Gradle build info

## Install

Install gradle, version at least 2.0, see http://gradle.org

To install gradle on linux:
 sudo add-apt-repository ppa:cwchien/gradle
 sudo apt-get update
 sudo apt-get install -y gradle

"For running Gradle, add GRADLE_HOME/bin to your PATH environment variable. Usually, this is sufficient to run Gradle."

## Usage

After gradle is installed, run gradle tasks. The relevant tasks appear at the end:

Other tasks
-----------
compileReadme - compile the HTML readme
NuBotJar - create the jar in .../dist-packaged
runCollector - run the collector service on a server


## IDE

Gradle can be used inside most IDE's.

* Eclipse

* IntelliJ

* Netbeans
http://plugins.netbeans.org/plugin/44510/gradle-support


## Dependencies

dependencies online, except the jars in lib

dependency: Grip
 https://github.com/joeyespo/grip
 pip install grip

## commands

gradle tasks => show the tasks available

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


