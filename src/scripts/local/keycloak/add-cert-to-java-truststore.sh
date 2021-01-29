#!/usr/bin/env bash
#CACERTS="/etc/pki/java/cacerts"

set -e

#rm /etc/pki/java/cacerts
#rm /etc/pki/ca-trust/extracted/java/cacerts

#keytool \
#    -import -trustcacerts \
#    -alias "tomcat" -file /opt/keycloak/ssl/dev.crt \
#    -keystore -cacerts \
#    -storepass changeit \
#    -noprompt

# This file is assumed to be the entrypoint for the original Dockerfile. In this way, we can additively chain our command to the beginning of the file
/opt/jboss/tools/docker-entrypoint.sh



# Find ip https://stackoverflow.com/questions/24319662/from-inside-of-a-docker-container-how-do-i-connect-to-the-localhost-of-the-mach
# sudo ip addr show docker0
# docker exec -u root keycloak bash -c "echo '172.17.0.1 localhost' > /etc/hosts"
