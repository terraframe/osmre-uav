#
# Copyright (c) 2022 TerraFrame, Inc. All rights reserved.
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

FROM maven:3-openjdk-8

ENV LOG_LEVEL=warning

ENV POSTGRES_HOST=localhost
ENV POSTGRES_PORT=5432
ENV ORIENTDB_HOST="remote:localhost"

ENV WORKSPACE=/workspace

ENV MAVEN_OPTS="-Xmx3500M -Xms256M -XX:+HeapDumpOnOutOfMemoryError"
ENV MAVEN_TEST_FORK_COUNT=0

RUN mkdir $WORKSPACE
WORKDIR $WORKSPACE

# Install GDAL and COG Tiff validator
RUN apt-get update && apt-get -y install libgdal-dev gdal-bin python3 pip wget
RUN pip install --global-option=build_ext --global-option="-I/usr/include/gdal" GDAL=="$(gdal-config --version)"
RUN wget https://raw.githubusercontent.com/OSGeo/gdal/master/swig/python/gdal-utils/osgeo_utils/samples/validate_cloud_optimized_geotiff.py -O validate_cloud_optimized_geotiff.py

# Copy the source in
COPY uasdm-server $WORKSPACE/uasdm-server
COPY uasdm-test $WORKSPACE/uasdm-test
COPY uasdm-web $WORKSPACE/uasdm-web
COPY envcfg $WORKSPACE/envcfg
COPY pom.xml $WORKSPACE/pom.xml

RUN ls

# Log4j properties
RUN mkdir -p $WORKSPACE/uasdm-test/src/main/resources
RUN wget -nv -O $WORKSPACE/uasdm-test/src/main/resources/log4j2.xml https://raw.githubusercontent.com/terraframe/geoprism-cloud/dev/ansible/roles/webserver/files/log4j2.xml
RUN sed -i -e "s/<Root level=\"error\">/<Root level=\"$LOG_LEVEL\">/g" $WORKSPACE/uasdm-test/src/main/resources/log4j2.xml

# Configure properties files
RUN echo "appcfg=osmre-dev\nproject.basedir=$WORKSPACE" > $WORKSPACE/envcfg/envcfg.properties

# Generate class files
RUN mvn clean install -B

RUN mkdir $WORKSPACE/bin
RUN wget -nv -O $WORKSPACE/bin/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
RUN chmod +x $WORKSPACE/bin/wait-for-it.sh

CMD $WORKSPACE/bin/wait-for-it.sh -t 60 $POSTGRES_HOST:$POSTGRES_PORT -- \
  && cd $WORKSPACE/uasdm-server && mvn process-resources -P database -Dappcfg=$WORKSPACE/uasdm/envcfg/osmre-dev -Dorientdb.db.url=$ORIENTDB_HOST -Ddatabase.hostURL=$POSTGRES_HOST -Ddatabase.port=$POSTGRES_PORT -Ddb.rootUser=postgres -Ddb.rootPass=postgres -Ddb.rootDb=postgres -Droot.clean=true -Dpatch=false \
  && cd $WORKSPACE/uasdm-test && mvn test -Dorientdb.db.url=$ORIENTDB_HOST -Ddatabase.hostURL=$POSTGRES_HOST -Ddatabase.port=$POSTGRES_PORT -Dappcfg=$WORKSPACE/uasdm/envcfg/osmre-dev -Dproject.basedir=$WORKSPACE
