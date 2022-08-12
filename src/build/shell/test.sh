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

sudo rm -rf /docker-tmp || true
sudo rm -rf $WORKSPACE/test-results || true
sudo mkdir -p /docker-tmp/workspace && sudo mkdir -p /docker-tmp/perms && sudo cp -r $WORKSPACE/* /docker-tmp/workspace
sudo mkdir -p /docker-tmp/test-results && sudo chmod -R 777 /docker-tmp/test-results

sudo docker rm -f $(docker ps -a -q --filter="name=postgres") || true
sudo docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 mdillon/postgis:9.5
  
sudo docker rm -f $(docker ps -a -q --filter="name=orientdb") || true
sudo docker run -d --name orientdb -p 2424:2424 -p 2480:2480  -e ORIENTDB_ROOT_PASSWORD=root orientdb:3.0.25

## Docker Setup ##
cd $WORKSPACE/uasdm
[ -h ./Dockerfile ] && unlink ./Dockerfile
ln -s uasdm-test/src/build/docker/Dockerfile Dockerfile
[ -h ./.dockerignore ] && unlink ./.dockerignore
ln -s uasdm-test/src/build/docker/.dockerignore .dockerignore

## Docker Build ##
sudo docker build -t uasdm-test .

## Docker Run ##
set +e
sudo -E docker run --name uasdm-test --rm --network=host \
-v /docker-tmp/test-results:/workspace/uasdm-test/target/surefire-reports \
-e MAVEN_OPTS="-Xmx3500M -Xms256M -XX:+HeapDumpOnOutOfMemoryError" \
uasdm-test
ecode=$?
mkdir -p "$WORKSPACE/test-results"
sudo cp -r /docker-tmp/test-results/. "$WORKSPACE/test-results/"
sudo chmod 777 -R $WORKSPACE/test-results
sudo chown ec2-user:ec2-user -R "$WORKSPACE/test-results"

sudo ls -al /docker-tmp/test-results
ls -al "$WORKSPACE/test-results"

set -e
[ "$ecode" != 0 ] && exit $ecode;
exit 0;
