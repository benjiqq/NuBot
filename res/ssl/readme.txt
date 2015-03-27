certificate password : h4rdc0r_
keytool -importcert -file certificate.cer -keystore Nubot_keystore.jks -alias "Alias"

Example:
keytool -importcert -file poloniex-dec.cer -keystore nubot_keystore.jks -alias “poloniexdec2014”


keytool -importcert -file ourSmtpSSL.cer -keystore nubot_keystore.jks -alias “webmaail”

keytool -importcert -file peatioselfsigned.cer -keystore nubot_keystore.jks -alias “peatiointernal2015”


keytool -importcert -file bter.cer -keystore nubot_keystore.jks -alias “bter-march-2015”
