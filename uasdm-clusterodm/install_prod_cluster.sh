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

export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --no-include-email --region us-west-2)


##### These commands are specific to the DEV container!! ####

docker run -d -p 4001:3000 -p 4501:8080 -p 10001:10000 -v /data/odm/config/aws-config-dev.json:/var/www/config-uasdm.json --link uasdm-nodeodm-dev --name uasdm-clusterodm-dev 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4001/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-dev 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4501


##### These commands are specific to the STAGING container!! ####

docker run -d -p 4002:3000 -p 4502:8080 -p 10002:10000 -v /data/odm/config/aws-config-staging.json:/var/www/config-uasdm.json --link uasdm-nodeodm-staging --name uasdm-clusterodm-staging 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4002/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-staging 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4502


##### These commands are specific to the PROD container!! ####

docker run -d -p 4000:3000 -p 4500:8080 -p 10000:10000 -v /data/odm/config/aws-config-prod.json:/var/www/config-uasdm.json --link uasdm-nodeodm-prod --name uasdm-clusterodm-prod 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4000/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-prod 3000"; sleep 1; echo "QUIT"; } | telnet localhost 4500
