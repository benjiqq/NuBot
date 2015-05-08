#!/bin/bash
#This script downloads the latest keystore 
#first rename existing keystore
mv nubot_keystore.jks nubot_keystore_old.jks
#then download from repository (branch develop)
wget -O nubot_keystore.jks https://bitbucket.org/JordanLeePeershares/nubottrading/src/develop/res/ssl/nubot_keystore.jks