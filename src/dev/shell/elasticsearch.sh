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

# Run with super user
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

# Exit immediately if anything errors out
set -e

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=uasdm-es") > /dev/null || true

# TODO : This is required on Ubuntu. Not sure yet the best way to address it
sysctl -w vm.max_map_count=262144

# Run ElasticSearch
docker run -d -p 9200:9200 -p 9300:9300 -e ES_JAVA_OPTS="-Xms512m -Xmx512m" -e ELASTIC_PASSWORD=elastic -e xpack.security.enabled=false -e discovery.type=single-node --name uasdm-es docker.elastic.co/elasticsearch/elasticsearch:8.3.2
