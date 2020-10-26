#!/bin/sh
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

# Run this script as root.

BASEDIR=$(cd `dirname $0` && pwd)

# Requires docker-machine installed first (https://docs.docker.com/machine/install-machine/)
# First, run local_odm.sh

sudo docker rm -f $(docker ps -a -q --filter="name=uasdm-clusterodm") || true
docker run -d -p 4000:3000 -p 4500:8080 -p 10000:10000 -v $BASEDIR/aws-config-tftest.json:/var/www/config-uasdm.json --link uasdm-nodeodm --name uasdm-clusterodm 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm --asr /var/www/config-uasdm.json --public-address http://67.165.199.18:4000/

sleep 2;

# If you decided to lock the node here then it will always spin up a new cloud instance.
{ sleep 1; echo "NODE ADD uasdm-nodeodm 3000"; sleep 1; echo "QUIT"; } | telnet localhost 4500 # echo "NODE LOCK 1"; sleep 1; 

echo "The cluster-odm server should now be accepting http requests at http://localhost:10000/"
