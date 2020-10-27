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

# Run this with sudo after you run build.sh
# Careful with tags. This will replace the 'latest' tag. (Look up nuainces with docker tags).
# You might need to change the current 'latest' tag to have some version number, and then add this new one as 'latest'

set -e

[ -z "$UASDM_ECR_KEY" ] && echo "UASDM_ECR_KEY is null. Set this environment variable and then try running this script again." && exit 1;
[ -z "$UASDM_ECR_SECRET" ] && echo "UASDM_ECR_SECRET is null. Set this environment variable and then try running this script again." && exit 1;

docker tag uasdm-micasense:latest 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-micasense:latest

export AWS_ACCESS_KEY_ID=$UASDM_ECR_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_ECR_SECRET
eval $(aws ecr get-login-password --region us-east-1)

docker push 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-micasense:latest
