#!/bin/bash

WORKSPACE=/home/rich/dev/projects/uasdm/uasdm

SSL=$WORKSPACE/uasdm-web/src/test/resources
RESOURCES=$WORKSPACE/src/scripts/local/keycloak

[ -d "$SSL/keycloak" ] && rm -r $SSL/keycloak
mkdir $SSL/keycloak
cp $SSL/dev.key.nopass $SSL/keycloak/dev.key.nopass && chmod 777 $SSL/keycloak/dev.key.nopass
cp $SSL/dev.crt $SSL/keycloak/dev.crt && chmod 777 $SSL/keycloak/dev.crt

docker rm -f $(docker ps -a -q --filter="name=keycloak") || true

docker run --name keycloak -d -p 8021:8443 \
  -v $SSL/tomcat.truststore:/opt/keycloak/ssl/tomcat.truststore \
  -v $RESOURCES/realm-export.json:/opt/keycloak/realm-export.json \
  -v $SSL/keycloak/dev.crt:/etc/x509/https/tls.crt \
  -v $SSL/keycloak/dev.key.nopass:/etc/x509/https/tls.key \
  -e KEYCLOAK_IMPORT=/opt/keycloak/realm-export.json \
  -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin \
  quay.io/keycloak/keycloak:12.0.2

echo "Keycloak is now running at https://localhost:8021/. You will need to create a user (and password) in the ream 'myrealm'."
