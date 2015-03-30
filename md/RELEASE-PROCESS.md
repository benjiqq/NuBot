## Release process

 - Use gitflow to create a new release branch out of develop, naming it with the name of the release (0.2.0)
 - Update the changelog.md using the resolved issues on github
 - Edit the file build.gradle and change the value of the *version* variable to current release name (0.2.0)
 - Tag the commit with a new tag, including the release candidate number starting from one (0.2.0-RC1) and push to upstream.  
 - Run the gradle task *NuBotDist* which will create a new bundle under the */dist* folder
 - Rename the folder to [0.2.0-RC1], and zip it
 - Upload the zip file to (bitbucket)[https://bitbucket.org/JordanLeePeershares/nubottrading/downloads]
 - Let QA test the RC using the (table checklist)[https://docs.google.com/spreadsheets/d/1WWb7cNstuE_BlJsINLNamO-ozvlDCgBFbm3y_9DbjDU/edit?usp=drive_web]
 - Fix bugs reported by QA and if needed create a new RC repeating the steps above (and deleting older RC).
 - When a stable RC is selected, rename the zip file and upload.
 - Tag the repo with the final version number (0.2.0)
 - Use gitflow to merge the release branch into master and back into develop
