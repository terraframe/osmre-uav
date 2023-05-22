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
then echo "Please run as root (with -E flag if using sudo)"
  exit
fi

# Requires AWS CLI : pip3 install awscli --upgrade --user
# https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html

# Exit immediately if anything errors out
set -e

export S3_ENDPOINT=s3.us-east-1.amazonaws.com
export S3_BUCKET=osmre-uas-dev
export S3_ACCESSKEY=$UASDM_ECR_KEY
export S3_SECRETKEY=$UASDM_ECR_SECRET
export S3_ACL=none

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=nodeodm") > /dev/null || true

# Pull & Run the custom UASDM NodeODM container
docker run -d -p 3000:3000 --name nodeodm opendronemap/nodeodm:latest --log_level debug --s3_endpoint "$S3_ENDPOINT" --s3_bucket "$S3_BUCKET" --s3_access_key "$S3_ACCESSKEY" --s3_secret_key "$S3_SECRETKEY" --s3_acl "$S3_ACL"
