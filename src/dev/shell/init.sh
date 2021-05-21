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


# This script is designed to be used by developers when setting up / updating a new environment.
# This script should be idempotent, which should allow the script to be run in update contexts.
# This script has been tested on Ubuntu

# This script should be run as your home user (i.e. NOT sudo)

# Prerequisite software must be installed:
#  git, nvm, docker, mvn, java
# Additionally, your envcfg.properties must be created first before running this.

# Required environment variables must be set before running:
# UASDM = /path/to/uasdm/git/repo/../
# UASDM_ECR_KEY = <uasdm ECR access key>
# UASDM_ECR_SECRET = <uasdm ECR access key secret>

# The ECR access keys are required for pulling/running the uasdm-nodeodm docker container.
# This container can alternatively be built using the source found in uasdm-odm.


######

# Exit on error
set -e

# Set proper version of npm
source ~/.nvm/nvm.sh
nvm install lts/erbium

# Update git
cd $UASDM/uasdm
git fetch

# Build front-end code
cd $UASDM/uasdm/uasdm-web/src/main/ng2
rm -rf node_modules
npm install
npm run build

# Build Java / webapp code
cd $UASDM/uasdm
mvn clean install -U
	
# Run Docker containers
sudo -E $UASDM/uasdm/src/dev/shell/postgres.sh
sudo -E $UASDM/uasdm/src/dev/shell/odm.sh
sudo -E $UASDM/uasdm/src/dev/shell/orientdb.sh
sudo -E $UASDM/uasdm/src/dev/shell/solr.sh
sudo -E $UASDM/uasdm/uasdm-clusterodm/build/local_cluster.sh

# Build database
cd $UASDM/uasdm/uasdm-server
mvn validate -P database -Ddb.clean=true -Ddb.rootPass=postgres -Ddb.rootUser=postgres -Ddb.rootDb=postgres -Ddb.patch=false
