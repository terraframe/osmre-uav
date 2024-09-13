#!/bin/bash

sudo docker build . -t uasdm-clusterlidar

sudo docker run --rm uasdm-clusterlidar

