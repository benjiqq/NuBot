# Gradle build info

## Usage

After gradle is installed, run gradle tasks. The relevant tasks appear at the end:

* NuBotJar - create the jar in the folder dist

* runui - run the UI

## Directory layout

The project uses the standard Gradle directory layout:

src : all source files
- main
-- java
-- resources : resource files for production

- test
-- java : test files
-- resources : 

dist

md : markdown documents

config

testconfig

ssltools


## Install

Install gradle, version at least 2.0, see http://gradle.org

To install gradle on linux:
 sudo add-apt-repository ppa:cwchien/gradle
 sudo apt-get update
 sudo apt-get install -y gradle

"For running Gradle, add GRADLE_HOME/bin to your PATH environment variable. Usually, this is sufficient to run Gradle."

## IDE

Gradle can be used inside most IDE's.

For Netbeans integration see:
http://plugins.netbeans.org/plugin/44510/gradle-support

## Dependencies

dependencies online, except the jars in lib

dependency: Grip
 https://github.com/joeyespo/grip
 pip install grip

## Adding gradle dependencies

go to => http://search.maven.org
 
search for the appropriate project.

Click on latest version.

On the left, click on gradle/grails

Copy the compile 'xyz' line
