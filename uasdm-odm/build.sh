#!/bin/bash
# run with sudo

set -e

cd ./ODM
sudo docker build -t uasdm-odm .

cd ../NodeODM
sudo docker build -t uasdm-nodeodm .

cd ../
