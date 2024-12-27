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

# Exit immediately if anything errors out
set -e

### The IDM test code intentionally looks quite a bit different than with GPR, and this is because it needs
# to build its own Dockerfile in order to run. This is because it has a bunch of additional tools which
# are required to function, and these tools must be built into a Docker image


## Hack to allow Jenkins to utilize test files which were created by a super user ##
sudo rm -rf /docker-tmp || true
sudo rm -rf $WORKSPACE/test-results || true
sudo mkdir -p /docker-tmp/workspace && sudo mkdir -p /docker-tmp/perms && sudo cp -r $WORKSPACE/* /docker-tmp/workspace
sudo mkdir -p /docker-tmp/test-results && sudo chmod -R 777 /docker-tmp/test-results

## Run any Docker containers that this program depends on ##
sudo docker rm -f $(docker ps -a -q --filter="name=postgres") || true
sudo docker run --name postgres -e POSTGRES_PASSWORD=postgres -d -p 5432:5432 postgis/postgis:14-3.2
  
sudo docker rm -f $(docker ps -a -q --filter="name=orientdb") || true
sudo docker run -d --name orientdb -p 2424:2424 -p 2480:2480  -e ORIENTDB_ROOT_PASSWORD=root orientdb:3.2

sudo docker rm -f $(docker ps -a -q --filter="name=elasticsearch") || true
# sysctl -w vm.max_map_count=262144 # Only works on Ubuntu. Our host OS is currently AL2
echo 262144 | sudo tee /proc/sys/vm/max_map_count > /dev/null # Should be the equivalent command for AL2
sudo docker run -d -p 9200:9200 -p 9300:9300 -e ES_JAVA_OPTS="-Xms512m -Xmx512m" -e ELASTIC_PASSWORD=elastic -e xpack.security.enabled=false -e discovery.type=single-node --name elasticsearch docker.elastic.co/elasticsearch/elasticsearch:8.3.2

sudo docker rm -f $(docker ps -a -q --filter="name=nodeodm") || true
docker run -d -p 3000:3000 --name nodeodm opendronemap/nodeodm:latest --log_level debug

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

## Hack to allow Jenkins to utilize test files which were created by a super user ##
mkdir -p "$WORKSPACE/test-results"
sudo cp -r /docker-tmp/test-results/. "$WORKSPACE/test-results/"
sudo chmod 777 -R $WORKSPACE/test-results
sudo chown ec2-user:ec2-user -R "$WORKSPACE/test-results"

sudo ls -al /docker-tmp/test-results
ls -al "$WORKSPACE/test-results"

set -e
[ "$ecode" != 0 ] && exit $ecode;
exit 0;
