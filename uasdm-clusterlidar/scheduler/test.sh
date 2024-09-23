#!/bin/bash

set -e

sudo docker build . -t uasdm-clusterlidar

sudo docker run -v /home/rrowlands/.aws:/root/.aws --entrypoint mamba --rm uasdm-clusterlidar run -n silvimetric /bin/bash -c "python autoscale.py"

# sudo docker run -v /home/rrowlands/.aws:/root/.aws --rm uasdm-clusterlidar
