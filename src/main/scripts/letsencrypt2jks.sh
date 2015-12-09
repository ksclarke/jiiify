#! /bin/bash

#
# Script to convert the Let's "Encrypt" output to a Java Keystore
#
#   Usage: ./letsencrypt2jks.sh [DOMAIN] [JKS_PASSWORD]
#

hash openssl 2>/dev/null || { echo >&2 "I require openssl but it's not installed.  Aborting."; exit 1; }

if [ "$#" -ne 2 ]; then
  "Usage: target/letsencrypt2jks.sh [DOMAIN] [PASSWORD]"
fi

# Delete the JKS from our previous run (if it's still around)
sudo rm -f le_jiiify.jks

sudo openssl pkcs12 -export \
  -in "/etc/letsencrypt/live/$1/fullchain.pem" \
  -inkey "/etc/letsencrypt/live/$1/privkey.pem" \
  -out "/tmp/jiiify_cert_and_key.p12" \
  -name "jiiify" \
  -caname "root" \
  -password "pass:$2"

sudo keytool -importkeystore \
  -deststorepass "$2" \
  -destkeypass "$2" \
  -destkeystore "le_jiiify.jks" \
  -srckeystore "/tmp/jiiify_cert_and_key.p12" \
  -srcstoretype "PKCS12" \
  -srcstorepass "$2" \
  -alias "jiiify"
