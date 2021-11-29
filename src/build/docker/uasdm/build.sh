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

# Run with elevated 'sudo' permissions as necessary

set -e

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../uasdm-web/target/uasdm.war target/uasdm.war
cp -R ../../../../envcfg/prod target/appcfg

docker build -t terraframe/uasdm:$tag .

if [ "$CGR_RELEASE_VERSION" != "latest" ]; then
  docker tag terraframe/uasdm:$tag terraframe/uasdm:latest
fi

docker save terraframe/uasdm:$tag | gzip > target/uasdm.dimg.gz
