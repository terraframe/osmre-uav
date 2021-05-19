#!/bin/bash
#
# Copyright (c) 2019 TerraFrame, Inc. All rights reserved.
#
# This file is part of Geoprism Registry(tm).
#
# Geoprism Registry(tm) is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Geoprism Registry(tm) is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Geoprism Registry(tm).  If not, see <http://www.gnu.org/licenses/>.
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

