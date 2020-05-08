#!/bin/sh
# Run this script as root.

# Requires docker-machine installed first (https://docs.docker.com/machine/install-machine/)
# First, run local_odm.sh

sudo docker rm -f $(docker ps -a -q --filter="name=uasdm-clusterodm") || true
docker run -d -p 4000:3000 -p 4500:8080 -p 10000:10000 -v /home/rich/dev/projects/uasdm/git/uasdm/uasdm-clusterodm/aws-config-tftest.json:/var/www/config-uasdm.json --link uasdm-nodeodm --name uasdm-clusterodm uasdm-clusterodm --asr /var/www/config-uasdm.json --public-address http://67.165.199.18:4000/

sleep 2;
{ sleep 1; echo "NODE ADD uasdm-nodeodm 3000"; sleep 1; echo "NODE LOCK 1"; sleep 1; echo "QUIT"; } | telnet localhost 4500 # echo "NODE LOCK 1"; sleep 1; 

echo "The cluster-odm server should now be accepting http requests at http://localhost:10000/"
