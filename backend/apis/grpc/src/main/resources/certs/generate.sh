#!/bin/bash

openssl genrsa -des3 -out serverprivate.key 2048
# enter passphrase: server

openssl req -new -key serverprivate.key -out server.csr -config server-csr.conf

openssl x509 -req -days 3650 -in server.csr -signkey serverprivate.key -out server.crt -extfile server-csr.conf -extensions v3_req

keytool -import -file server.crt -alias serverCA -keystore server-truststore.jks
# enter passphrase: server

openssl pkcs12 -export -in server.crt -inkey serverprivate.key -certfile server.crt -name "servercert" -out server-keystore.p12
# enter passphrase: server