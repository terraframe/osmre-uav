#!/bin/bash

mvn clean install

sudo docker build -t uasdm-fargate-erossync .
