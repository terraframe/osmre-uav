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

# This script will start a local docker instance that you can use for testing and development
# Run it with sudo

docker rm -f $(docker ps -a -q --filter=name=solr)

set -e
set -x

BASEDIR=$(dirname "$0")


# Install preqreqs (assumes you're on Ubuntu!)
#apt-get -y update
#apt-get -y install tesseract-ocr libgdal-java


docker run -d -p 8983:8983 --name solr solr:6.6.5
sleep 8

docker exec solr bash -c '/opt/solr/bin/solr create_core -c uasdm'

# Install our USDM core
docker cp $BASEDIR/../../solr-core/uasdm 'solr:/opt/solr/server/solr/uasdm/../staging'
docker exec -u root solr bash -c 'cp -rf /opt/solr/server/solr/uasdm/../staging/* /opt/solr/server/solr/uasdm'
docker exec -u root solr bash -c 'rm -rf /opt/solr/server/solr/uasdm/../staging'
docker exec -u root solr bash -c 'chown -R solr:solr /opt/solr/server/solr/uasdm'

docker restart solr
sleep 8
docker logs solr

echo "Solr should now be running at http://127.0.0.1:8983 . Don't forget to run the indexer!"