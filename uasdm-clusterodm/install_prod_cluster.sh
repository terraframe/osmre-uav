#!/bin/bash

# Before running, ensure uasdm-odm is setup and running currently (start with install_prod_odm.sh)

# Assumes superuser
# Don't run this script blindly! Intelligently pick out what you need.

# Exit immediately if anything errors out
set -e


# Install docker-machine (only done once) (https://docs.docker.com/machine/install-machine/)
base=https://github.com/docker/machine/releases/download/v0.16.0 &&
  curl -L $base/docker-machine-$(uname -s)-$(uname -m) >/tmp/docker-machine &&
  sudo mv /tmp/docker-machine /usr/local/bin/docker-machine &&
  chmod +x /usr/local/bin/docker-machine

# Copy over the cluster-odm config file
# at /data/odm/config/aws-config-prod.json


##### These commands are specific to the DEV container!! ####

docker run -d -p 4001:3000 -p 4501:8080 -p 10001:10000 -v /data/odm/config/aws-config-prod.json:/var/www/config-uasdm.json --link uasdm-nodeodm --name uasdm-clusterodm uasdm-clusterodm --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4001/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm 3001"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4501


##### These commands are specific to the STAGING container!! ####

docker run -d -p 4002:3000 -p 4502:8080 -p 10002:10000 -v /data/odm/config/aws-config-prod.json:/var/www/config-uasdm.json --link uasdm-nodeodm --name uasdm-clusterodm uasdm-clusterodm --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4002/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm 3002"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4502


##### These commands are specific to the PROD container!! ####

docker run -d -p 4000:3000 -p 4500:8080 -p 10000:10000 -v /data/odm/config/aws-config-prod.json:/var/www/config-uasdm.json --link uasdm-nodeodm --name uasdm-clusterodm uasdm-clusterodm --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4000/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm 3000"; sleep 1; echo "QUIT"; } | telnet localhost 4500
