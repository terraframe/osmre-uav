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

docker tag uasdm-clusterodm:latest 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest

export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --no-include-email --region us-west-2)
docker push 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-clusterodm:latest