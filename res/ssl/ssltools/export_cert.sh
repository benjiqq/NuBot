openssl s_client -showcerts -connect alts.trade:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >alts_cert.pem
