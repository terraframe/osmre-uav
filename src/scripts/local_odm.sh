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

# Run this with sudo

# Requires AWS CLI : pip3 install awscli --upgrade --user
# https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html

# Exit immediately if anything errors out
set -e

# Used for pulling the images from TerraFrame's AWS ECR
export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --region us-west-2 --no-include-email)

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=uasdm-nodeodm") || true

# Pull the micasense container (our NodeODM might launch it at runtime)
docker pull 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense:latest

# Pull & Run the custom UASDM NodeODM container
docker run -d -p 3000:3000 -v $(pwd)/micasense:/opt/micasense -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=$(pwd)/micasense --name uasdm-nodeodm uasdm-nodeodm
