#!/bin/bash
#This script downloads the latest keystore 
#first rename existing keystore
mv nubot_keystore.jks nubot_keystore_old.jks
#then download from repository (branch develop)
wget -O nubot_keystore.jks https://bitbucket.org/JordanLeePeershares/nubottrading/src/d2309528bea99e7521d05c11ac79f4e3bb2d1f30/res/ssl/nubot_keystore.jks?at=develop