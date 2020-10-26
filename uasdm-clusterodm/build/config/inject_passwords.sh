#!/bin/bash

set -e

[ -z "$UASDM_CLUSTER_KEY" ] && echo "UASDM_CLUSTER_KEY is null. Set this environment variable and then try running this script again." && exit 1;
[ -z "$UASDM_CLUSTER_SECRET" ] && echo "UASDM_CLUSTER_SECRET is null. Set this environment variable and then try running this script again." && exit 1;

rm -r target | true
mkdir target

cp ./aws-config-devdeploy.json target/aws-config-devdeploy.json
cp ./aws-config-prod.json target/aws-config-prod.json
cp ./aws-config-staging.json target/aws-config-staging.json

sed -i -e "s/UASDM_CLUSTER_KEY/$UASDM_CLUSTER_KEY/g" ./target/aws-config-devdeploy.json
sed -i -e "s/UASDM_CLUSTER_KEY/$UASDM_CLUSTER_KEY/g" ./target/aws-config-prod.json
sed -i -e "s/UASDM_CLUSTER_KEY/$UASDM_CLUSTER_KEY/g" ./target/aws-config-staging.json

sed -i -e "s/UASDM_CLUSTER_SECRET/$UASDM_CLUSTER_SECRET/g" ./target/aws-config-devdeploy.json
sed -i -e "s/UASDM_CLUSTER_SECRET/$UASDM_CLUSTER_SECRET/g" ./target/aws-config-prod.json
sed -i -e "s/UASDM_CLUSTER_SECRET/$UASDM_CLUSTER_SECRET/g" ./target/aws-config-staging.json

echo "Config files with variables replaced are now available in target."
