#!/bin/bash
#
# Copyright 2020 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# This file explains how the keys used for development were created.
# This process is the recommended way to do it again, if you need to.
# This process was followed AS OPPOSED TO the keytool way of doing things
# (i.e. with `keytool -genkey`) because this way the dev key can be used
# in contexts outside of tomcat (i.e. with jboss / keycloak). This process
# is ultimately more flexible since the private key is not hidden/locked
# purely inside of a keystore.


export PASSWORD=2v8hVW2rPFncN6m
export ALIAS=tomcat
export FACTORING_MODULUS=4096 # https://www.keylength.com/

set -e
set -x


# These steps based on https://www.akadia.com/services/ssh_test_certificate.html
# 1. Generate a private key (dev.key)
[ -f dev.key ] && rm dev.key
openssl genrsa -passout env:PASSWORD -des3 -out dev.key $FACTORING_MODULUS


# 2. CSR was generated with `openssl req -new -key server.key -out server.csr`
[ -f dev.csr ] && rm dev.csr
openssl req -new -passin env:PASSWORD -key dev.key -out dev.csr -subj "/C=US/ST=CO/O=TerraFrame/CN=localhost"


# 3. Generate a self-signed X509 certificate based on our private key
[ -f dev.crt ] && rm dev.crt
openssl x509 -req -passin env:PASSWORD -days 9999 -in dev.csr -signkey dev.key -out dev.crt


# These steps based on https://stackoverflow.com/questions/906402/how-to-import-an-existing-x-509-certificate-and-private-key-in-java-keystore-to
# 4. Generate a pkcs12 from the x509
openssl pkcs12 -passin env:PASSWORD -passout env:PASSWORD -export -in dev.crt -inkey dev.key \
               -out dev.p12 -name $ALIAS \
               -CAfile ca.crt -caname root


# 5. Import the pkcs12 into a keystore
yes | keytool -importkeystore \
        -deststorepass $PASSWORD -destkeypass $PASSWORD -destkeystore keystore.ks \
        -srckeystore dev.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD \
        -alias $ALIAS


# 6. Import the certificate into the truststore
yes | keytool -importkeystore \
        -deststorepass $PASSWORD -destkeypass $PASSWORD -destkeystore tomcat.truststore \
        -srckeystore dev.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD \
        -alias $ALIAS


# 7. Create a certificate which does not have a password (not encrypted)
[ -f dev.key.nopass ] && rm dev.key.nopass
openssl rsa -passin env:PASSWORD -in dev.key -out dev.key.nopass

