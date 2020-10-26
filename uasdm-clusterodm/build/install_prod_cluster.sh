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


### You should do a docker pull if you've updated the image ###

docker pull 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest


### Make sure to add credentials back into the config files! They've been scraped out for open sourcing purposes
### If Fresh Install
sudo mkdir -p /data/odm/config
# Copy over the cluster-odm config file
### If Patch : you've made changes to the aws-config make sure to propagate them ###

vim /data/odm/config/aws-config-dev.json
vim /data/odm/config/aws-config-staging.json
vim /data/odm/config/aws-config-prod.json


##### These commands are specific to the DEV container!! ####

docker run -d -p 4001:3000 --restart always -p 4501:8080 -p 10001:10000 -v /data/odm/config/aws-config-dev.json:/var/www/config-uasdm.json -v /data/odm-cluster/dev/tmp:/var/www/tmp -v /data/odm-cluster/dev/data:/var/www/data --link uasdm-nodeodm-dev --name uasdm-clusterodm-dev 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4001/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-dev 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4501


##### These commands are specific to the STAGING container!! ####

docker run -d -p 4002:3000 --restart always -p 4502:8080 -p 10002:10000 -v /data/odm/config/aws-config-staging.json:/var/www/config-uasdm.json -v /data/odm-cluster/staging/tmp:/var/www/tmp -v /data/odm-cluster/staging/data:/var/www/data --link uasdm-nodeodm-staging --name uasdm-clusterodm-staging 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4002/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-staging 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4502


##### These commands are specific to the PROD container!! ####

docker run -d -p 4000:3000 --restart always -p 4500:8080 -p 10000:10000 -v /data/odm/config/aws-config-prod.json:/var/www/config-uasdm.json -v /data/odm-cluster/prod/tmp:/var/www/tmp -v /data/odm-cluster/prod/data:/var/www/data --link uasdm-nodeodm-prod --name uasdm-clusterodm-prod 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest --asr /var/www/config-uasdm.json --public-address http://10.120.10.50:4000/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-prod 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4500
