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

# Exit immediately if anything errors out
set -e

[ -z "$UASDM" ] && echo "UASDM is null. Set this environment variable and then try running this script again." && exit 1;

# Run with super user
if [ "$EUID" -ne 0 ]; then
  echo "Please run as root"
  exit
fi

# Requires docker-machine installed first (https://docs.docker.com/machine/install-machine/)
if ! [ -x "$(command -v docker-machine)" ]; then 
	echo "You must install docker-machine."
    exit
fi

# Requires nodeodm running first
if [ ! "$(sudo docker ps -a | grep uasdm-nodeodm)" ]; then 
	echo "docker container uasdm-nodeodm must be running"
    exit
fi

# config files must be built
if [ ! -f "$UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-devdeploy.json" ]; then 
	echo "You must build the clusterodm config files at $UASDM/uasdm/uasdm-clusterodm/build/config"
    exit
fi

export AWS_ACCESS_KEY_ID=$UASDM_ECR_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_ECR_SECRET

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 813324710591.dkr.ecr.us-east-1.amazonaws.com

sudo docker rm -f $(docker ps -a -q --filter="name=uasdm-clusterodm") || true
docker run -d -p 4000:3000 -p 4500:8080 -p 10000:10000 -v $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-devdeploy.json:/var/www/config-uasdm.json --link uasdm-nodeodm --name uasdm-clusterodm 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-clusterodm --asr /var/www/config-uasdm.json --public-address http://67.165.199.18:4000/

sleep 2;

# If you decided to lock the node here then it will always spin up a new cloud instance.
{ sleep 1; echo "NODE ADD uasdm-nodeodm 3000"; sleep 1; echo "NODE LOCK 1"; echo "QUIT"; } | telnet localhost 4500

echo "The cluster-odm server should now be accepting http requests at http://localhost:10000/. Make sure to change your odm.url to use port 4000 in envcfg.properties."
