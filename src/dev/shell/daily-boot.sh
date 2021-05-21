#!/bin/bash
#
# Copyright 2020 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


# This script is designed to run when your computer boots at the beginning of the day. It should
# launch all relevant programs necessary for development.
# This script should be idempotent, which should allow the script to be run in update contexts.
# This script has been tested on Ubuntu

# This script should be run as your home user (i.e. NOT sudo)

# Prerequisite software must be installed:
#  git, nvm, docker, mvn, java
# Additionally, your envcfg.properties must be created first before running this.

# Required environment variables must be set before running:
# UASDM = /path/to/uasdm-git-checkout/../

######

UASDM_PROJECT=$UASDM/uasdm

# Exit on error
set -ex

# Set proper version of npm
source ~/.nvm/nvm.sh
nvm install lts/erbium

# Update git
cd $UASDM_PROJECT
git pull

# Run Docker containers
sudo docker start orientdb
sudo docker start solr
sudo docker start uasdm-nodeodm

# Kill any running tomcat
pkill -f -SIGINT catalina || true
sleep 2

# Run the ng2 server
cd $UASDM_PROJECT/uasdm-web/src/main/ng2
gnome-terminal -x sh -c "npm run start"

# Run the cgr webserver
cd $UASDM_PROJECT
mvn clean
gnome-terminal -x sh -c "mvn install -P ng2-dev,cargo-run-uasdm"

# Open a web browser to view the app
sleep 15
google-chrome https://localhost:8443/uasdm

