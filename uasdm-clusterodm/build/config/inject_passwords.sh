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


set -e

[ -z "$UASDM_CLUSTER_KEY" ] && echo "UASDM_CLUSTER_KEY is null. Set this environment variable and then try running this script again." && exit 1;
[ -z "$UASDM_CLUSTER_SECRET" ] && echo "UASDM_CLUSTER_SECRET is null. Set this environment variable and then try running this script again." && exit 1;

rm -r $UASDM/uasdm/uasdm-clusterodm/build/config/target | true
mkdir $UASDM/uasdm/uasdm-clusterodm/build/config/target

cp $UASDM/uasdm/uasdm-clusterodm/build/config/aws-config-dev.json $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-dev.json
cp $UASDM/uasdm/uasdm-clusterodm/build/config/aws-config-devdeploy.json $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-devdeploy.json
cp $UASDM/uasdm/uasdm-clusterodm/build/config/aws-config-prod.json $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-prod.json
cp $UASDM/uasdm/uasdm-clusterodm/build/config/aws-config-staging.json $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-staging.json

sed -i -e "s~UASDM_CLUSTER_KEY~$UASDM_CLUSTER_KEY~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-dev.json
sed -i -e "s~UASDM_CLUSTER_KEY~$UASDM_CLUSTER_KEY~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-devdeploy.json
sed -i -e "s~UASDM_CLUSTER_KEY~$UASDM_CLUSTER_KEY~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-prod.json
sed -i -e "s~UASDM_CLUSTER_KEY~$UASDM_CLUSTER_KEY~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-staging.json

sed -i -e "s~UASDM_CLUSTER_SECRET~$UASDM_CLUSTER_SECRET~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-dev.json
sed -i -e "s~UASDM_CLUSTER_SECRET~$UASDM_CLUSTER_SECRET~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-devdeploy.json
sed -i -e "s~UASDM_CLUSTER_SECRET~$UASDM_CLUSTER_SECRET~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-prod.json
sed -i -e "s~UASDM_CLUSTER_SECRET~$UASDM_CLUSTER_SECRET~g" $UASDM/uasdm/uasdm-clusterodm/build/config/target/aws-config-staging.json

echo "Config files with variables replaced are now available in target."
