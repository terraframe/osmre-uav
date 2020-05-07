#
# Copyright (c) 2015 TerraFrame, Inc. All rights reserved.
#
# This file is part of Runway SDK(tm).
#
# Runway SDK(tm) is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# Runway SDK(tm) is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with Runway SDK(tm).  If not, see <http://www.gnu.org/licenses/>.
#

# This script is for testing patching a local solr instance. Only useful for ansible script development
# Run it with sudo

set -e
set -x

BASEDIR=$(pwd)

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
