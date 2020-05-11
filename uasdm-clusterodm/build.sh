#!/bin/bash
# run with sudo

set -e

cd ./ClusterODM
sudo docker build -t uasdm-clusterodm .

cd ../
