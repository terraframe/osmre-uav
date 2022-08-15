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

# Run with super user
if [ "$EUID" -ne 0 ]
  then echo "Please run as root (with -E flag to include user variables)"
  exit
fi

set -e

WORKSPACE=$UASDM/uasdm

SSL=$WORKSPACE/uasdm-web/src/test/resources
RESOURCES=$WORKSPACE/src/dev/shell/keycloak

[ -d "$SSL/keycloak" ] && rm -r $SSL/keycloak
mkdir $SSL/keycloak
cp $SSL/dev.key.nopass $SSL/keycloak/dev.key.nopass && chmod 777 $SSL/keycloak/dev.key.nopass
cp $SSL/dev.crt $SSL/keycloak/dev.crt && chmod 777 $SSL/keycloak/dev.crt

docker rm -f $(docker ps -a -q --filter="name=keycloak") || true

docker run --user root --name keycloak -d -p 8021:8443 \
  -v $SSL/dev.crt:/opt/keycloak/ssl/dev.crt \
  -v $RESOURCES/realm-export.json:/opt/keycloak/realm-export.json \
  -v $SSL/keycloak/dev.crt:/etc/x509/https/tls.crt \
  -v $SSL/keycloak/dev.key.nopass:/etc/x509/https/tls.key \
  -v $RESOURCES/add-cert-to-java-truststore.sh:/opt/keycloak/ssl/add-cert-to-java-truststore.sh \
  --entrypoint="/opt/keycloak/ssl/add-cert-to-java-truststore.sh" \
  -e X509_CA_BUNDLE=/etc/x509/https/tls.crt \
  -e KEYCLOAK_IMPORT=/opt/keycloak/realm-export.json \
  -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin \
  quay.io/keycloak/keycloak:12.0.2

#  --entrypoint="/opt/keycloak/ssl/add-cert-to-java-truststore.sh" \

#docker exec -u root keycloak /opt/keycloak/ssl/add-cert-to-java-truststore.sh

# TODO : This wrote into the host's file instead of the containers file
#docker exec -u root keycloak bash -c "echo '172.17.0.1 localhost' > /etc/hosts"
#echo "Backchannel set to ip 172.17.0.1. If backchanneling is not working, it might be because this ip is wrong. You can verify with 'sudo ip addr show docker0'"

echo "Keycloak is now running at https://localhost:8021/. Default admin credentials are admin/admin. A default user has been created as cgrkc/cgrkc, but you might need to log into keycloak and set a password.
