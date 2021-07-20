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

# This script is for testing patching a local solr instance. Only useful for ansible script development
# Run it with sudo

set -e
set -x

BASEDIR=$(dirname "$0")

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=solr") || true
docker system prune


# Install preqreqs (assumes you're on Ubuntu!)
#apt-get -y update
#apt-get -y install tesseract-ocr libgdal-java

rm -rf ../../target/solr
docker cp solr:/opt/solr/server/solr/mycores/uasdm/data/ ../../target/solr/


docker run -d -p 8983:8983 -v $BASEDIR/../../src/solr/configsets/uasdm:/opt/solr/server/solr/configsets/uasdm:ro -v $BASEDIR/../../target/solr/mycores/uasdm/data:/data --name solr solr:6.6.5 solr-precreate uasdm /opt/solr/server/solr/configsets/uasdm

sleep 3;


docker exec --user root solr bash -c 'mv /opt/solr/server/solr/mycores/uasdm/data/* /data/ && rm -rf /opt/solr/server/solr/mycores/uasdm/data && ln -s /data /opt/solr/server/solr/mycores/uasdm/data && chown -R solr:solr /opt/solr/server/solr/mycores/uasdm/data && chown -R solr:solr /data'
# docker exec --user root solr bash -c 'mkdir /script && printf "if [ -d /data/index ]; then \n rm -r /opt/solr/server/solr/mycores/uasdm/data \n echo deleting \n else \n mv /opt/solr/server/solr/mycores/uasdm/data/* /data/ \n echo moving \n fi \n if [ -d /opt/solr/server/solr/mycores/uasdm/data ]; then \n rm -rf /opt/solr/server/solr/mycores/uasdm/data \n fi \n ln -s /data /opt/solr/server/solr/mycores/uasdm/data \n chown -R solr:solr /opt/solr/server/solr/mycores/uasdm/data \n chown -R solr:solr /data \n" > /script/solr-data.sh && chmod +x /script/solr-data.sh && /script/solr-data.sh' 


echo "Solr should now be running at http://127.0.0.1:8983."
