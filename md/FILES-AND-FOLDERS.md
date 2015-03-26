#####Convention for files and folders

| Description                 | nubot/ path                      | dist/ path                 | example                     | status     |
|-----------------------------|----------------------------------|----------------------------|-----------------------------|------------|
| Java source files           | nubot/src/                       | dist/res/sources/          | all Java source files       | done       |
| Options JSON files          | nubot/config/options/            | Include only               | sample-config.json          | done       |
|                             |                                  | dist/sample-options.json   | myconfig/poloniex.json      |            |
| Logging configuration files | nubot/config/logging/            | dist/config                | test_logback.xml            | done       |
|                             |                                  |                            | logback.xml                 |            |
|                             |                                  |                            | ...                         |            |
| RPC settings                | nubot/config/rpc                 | ?                          | ?                           | TODO       |
| Documentation files         | nubot/md/                        | it depends:                | LICENCE.md                  | done       |
|                             |                                  | dist/readme.html           | SETUP.md                    |            |
|                             |                                  | dist/src/LICENCE.md        | CONTRIBUTE.md               |            |
|                             |                                  | dist/docs/*all-the-others  | gradle.md                   |            |
| Static libraries            | nubot/lib/                       | embedded in the jar        | spark-core-2.2-SNAPSHOT.jar | done       |
| SSL tools                   | nubot/res/ssl                    | include only the script    | listCertificates.sh         | done       |
|                             |                                  | dist/ssl/updateKeystore.sh | export_cert.sh              |            |
| SSL keystore                | nubot/res/ssl/nubot_keystore.jks | dist/res/ssl/              |                             | done       |
| Frozen files                | nubot/res/frozen-funds           | dist/res/frozen-funds      |                             | done       |
| Currencies.csv              | nubot/res                        | dist/res/currencies.csv    | Currencies.csv              | done       |
| UI templates                | nubot/UI                         | dist/res/UI                | operation.mustache          | TODO       |
| Readme assets               | nubot/res/readme-assets          | //                         |                             | done       |
| Scripts                     | nubot/resources/scripts          | dist/res/scripts  (?)      | runNubot.sh                 | not in use |