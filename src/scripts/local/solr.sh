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

# This script will start a local docker instance that you can use for testing and development
# Run it with sudo

# docker rm -f $(docker ps -a -q --filter=name=solr)

set -e
set -x

BASEDIR=$(pwd)

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=solr") || true
#docker system prune


# Install preqreqs (assumes you're on Ubuntu!)
#apt-get -y update
#apt-get -y install tesseract-ocr libgdal-java

rm -rf ../../target/solr


docker run -d -p 8983:8983 -v $BASEDIR/../../src/solr/configsets/uasdm:/opt/solr/server/solr/configsets/uasdm:ro --name solr solr:6.6.5 solr-precreate uasdm /opt/solr/server/solr/configsets/uasdm

echo "Solr should now be running at http://127.0.0.1:8983."
