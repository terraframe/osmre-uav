#!/bin/bash

mvn clean install

docker build -t uasdm-fargate-erossync .
