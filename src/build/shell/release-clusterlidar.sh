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

[ -z "$UASDM_ECR_KEY" ] && echo "UASDM_ECR_KEY is null. Set this environment variable and then try running this script again." && exit 1;
[ -z "$UASDM_ECR_SECRET" ] && echo "UASDM_ECR_SECRET is null. Set this environment variable and then try running this script again." && exit 1;

cd $WORKSPACE/uasdm/uasdm-clusterlidar/worker
sh -xe ./build.sh
sudo -E sh -xe ./deploy.sh
