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

# Run with elevated 'sudo' permissions as necessary

set -e

# If tag is not set, then set it to 'latest' as a default value.
tag=${tag:-'latest'}

([ -d target ] && rm -rf target) || true
mkdir target
cp ../../../../uasdm-web/target/uasdm.war target/uasdm.war
cp -R ../../../../envcfg/osmre-dev target/appcfg
cp -R ../../../../envcfg/envcfg.properties target/appcfg

docker build -t terraframe/uasdm:$tag .

if [ "$CGR_RELEASE_VERSION" != "latest" ]; then
  docker tag terraframe/uasdm:$tag terraframe/uasdm:latest
fi

# docker save terraframe/uasdm:$tag | gzip > target/uasdm.dimg.gz
