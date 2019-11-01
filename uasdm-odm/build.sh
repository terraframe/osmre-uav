#!/bin/bash
# run with sudo

# Exit immediately if anything errors out
set -e

# Copy our modified source to ODM
cp -f ./src/opendm/config.py ODM/opendm/config.py
cp -f ./src/stages/dataset.py ODM/stages/dataset.py
cp -f ./src/stages/odm_app.py ODM/stages/odm_app.py
cp -f ./src/stages/odm_micasense.py ODM/stages/odm_micasense.py

# Copy modified source to NodeODM
cp -f ./src/nodeodm/libs/Task.js NodeODM/libs/Task.js

# Build ODM
cd ./ODM
sudo docker build -t uasdm-odm .

cd ../NodeODM
sudo docker build -t uasdm-nodeodm .

cd ../
