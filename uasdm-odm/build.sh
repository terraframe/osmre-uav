#!/bin/bash
# run with sudo

# Exit immediately if anything errors out
set -e

# Copy our modified source to ODM
cp -f ./src/opendm/config.py ODM/opendm/config.py
cp -f ./src/stages/dataset.py ODM/stages/dataset.py
cp -f ./src/stages/odm_app.py ODM/stages/odm_app.py
cp -f ./src/stages/odm_micasense.py ODM/stages/odm_micasense.py

# Build ODM
cd ./ODM
docker build -t uasdm-odm .

cd ../NodeODM
docker build -t uasdm-nodeodm .

cd ../
docker rm -f $(docker ps -a -q --filter="name=uasdm-nodeodm") || true
docker run -d -p 3000:3000 -v /opt/odm-micasense-temp:/opt/micasense -e MICASENSE_HOST_BINDING=/opt/odm-micasense-temp -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock --name uasdm-nodeodm uasdm-nodeodm

echo "The server should be running at http://localhost:3000/"