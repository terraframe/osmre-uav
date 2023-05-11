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

#### IMPORTANT ####
# This file currently serves more as documentation as to how one would deploy. It cannot currently be run in it's entirety, you need to intelligently
#   pick and choose which commands to run based on what it is that you're doing. At some point this script will probably be converted into ansible.
###################








########

Config changes needed for next deploy:
1. Make sure the dev-deploy bucket is updated since its currently on dev (not dev-deploy)
2. Add `acl: none` to s3
3. `vpcId` changed to `vpc`
4. `additionalMachineSetupCmd` changed to `nodeSetupCmd`
5. Change nodeodm:latest tag to prod/staging/devdeploy


6??. Change the deployed image for nodeodm, and also the image referenced in the config for staging/prod to use the opendronemap docker image, and not our custom micasense one.

########

















# Before running, ensure uasdm-odm is setup and running currently (start with install_prod_odm.sh)

# Assumes superuser

# Exit immediately if anything errors out
set -e


# Install docker-machine (only done once) (https://docs.docker.com/machine/install-machine/)
base=https://github.com/docker/machine/releases/download/v0.16.0 &&
  curl -L $base/docker-machine-$(uname -s)-$(uname -m) >/tmp/docker-machine &&
  sudo mv /tmp/docker-machine /usr/local/bin/docker-machine &&
  chmod +x /usr/local/bin/docker-machine


### You should do a docker pull if you've updated the image ###
export AWS_ACCESS_KEY_ID=$UASDM_ECR_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_ECR_SECRET
#aws ecr get-login --region us-east-1 | docker login --username AWS --password-stdin 813324710591.dkr.ecr.us-east-1.amazonaws.com
$(aws ecr get-login --no-include-email --region us-east-1)

docker pull 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-clusterodm:latest


### If Fresh Install: Create data directories
sudo mkdir -p /data/odm-cluster/dev/config
sudo mkdir -p /data/odm-cluster/dev/data
sudo mkdir -p /data/odm-cluster/dev/tmp
sudo mkdir -p /data/odm-cluster/staging/config
sudo mkdir -p /data/odm-cluster/staging/data
sudo mkdir -p /data/odm-cluster/staging/tmp
sudo mkdir -p /data/odm-cluster/prod/config
sudo mkdir -p /data/odm-cluster/prod/data
sudo mkdir -p /data/odm-cluster/prod/tmp
sudo chown 1000:1000 -R /data/odm-cluster


### Make sure to add credentials back into the config files! They've been scraped out for open sourcing purposes
### If Fresh Install
# Copy over the cluster-odm config file
### If Patch : you've made changes to the aws-config make sure to propagate them ###

vim /data/odm-cluster/dev/config/aws-config-dev.json
vim /data/odm-cluster/staging/config/aws-config-staging.json
vim /data/odm-cluster/prod/config/aws-config-prod.json

### If patch : You need to reboot the clusterodm server

# Note the public-address at the end. It MUST be set to allow the spawned cluster nodes to talk back to the cluster odm.
# If this is not working properly you will silently leak nodes and eat up budget!

##### These commands are specific to the DEV container!! ####

docker run -d -p 4001:3000 --restart always -p 4501:8080 -p 10001:10000 \
  -v /data/odm-cluster/dev/config/aws-config-dev.json:/var/www/config-uasdm.json -v /data/odm-cluster/dev/tmp:/var/www/tmp \
  -v /data/odm-cluster/dev/data:/var/www/data \
  --link uasdm-nodeodm-dev \
  --name uasdm-clusterodm-dev 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-clusterodm:devdeploy \
  --debug --log-level debug \
  --asr /var/www/config-uasdm.json --public-address http://10.120.10.21:4001/

# If fresh install: (skip if patch)
sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-dev 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4501
# If patch, run telnet localhost 4501 and do a NODE LIST just to make sure the expected nodes are there, with a [L] on node 1 which denotes that it is locked


##### These commands are specific to the STAGING container!! ####

docker run -d -p 4002:3000 --restart always -p 4502:8080 -p 10002:10000 \
  -v /data/odm-cluster/staging/config/aws-config-staging.json:/var/www/config-uasdm.json -v /data/odm-cluster/staging/tmp:/var/www/tmp \
  -v /data/odm-cluster/staging/data:/var/www/data \
  --link uasdm-nodeodm-staging \
  --name uasdm-clusterodm-staging 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-clusterodm:staging \
  --debug --log-level debug \
  --asr /var/www/config-uasdm.json --public-address http://10.120.10.21:4002/

# If fresh install: (skip if patch)
sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-staging 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4502
# If patch, run telnet localhost 4502 and do a NODE LIST just to make sure the expected nodes are there, with a [L] on node 1 which denotes that it is locked


##### These commands are specific to the PROD container!! ####

docker run -d -p 4000:3000 --restart always -p 4500:8080 -p 10000:10000 \
  -v /data/odm-cluster/prod/config/aws-config-prod.json:/var/www/config-uasdm.json -v /data/odm-cluster/prod/tmp:/var/www/tmp \
  -v /data/odm-cluster/prod/data:/var/www/data \
  --link uasdm-nodeodm-prod \
  --name uasdm-clusterodm-prod 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-clusterodm:prod \
  --asr /var/www/config-uasdm.json --public-address http://10.120.10.21:4000/

# If fresh install: (skip if patch)
sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm-prod 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4500
# If patch, run telnet localhost 4500 and do a NODE LIST just to make sure the expected nodes are there, with a [L] on node 1 which denotes that it is locked
