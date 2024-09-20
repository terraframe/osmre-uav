#!/bin/bash

set -e

sudo docker build . -t uasdm-clusterlidar

sudo docker run -v /home/rrowlands/.aws:/root/.aws --rm uasdm-clusterlidar

