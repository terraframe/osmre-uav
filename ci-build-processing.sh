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

sudo rpm -i https://packagecloud.io/github/git-lfs/packages/el/6/git-lfs-2.9.0-1.el6.x86_64.rpm/download || true

if [ "$build_micasense" == "true" ]; then
	cd $WORKSPACE/uasdm/uasdm-micasense
	sh -xe ./init.sh
	sh -xe ./build.sh
	sudo sh -xe ./deploy.sh
fi

if [ "$build_odm" == "true" ]; then
	cd $WORKSPACE/uasdm/uasdm-odm
	sh -xe ./init.sh
	sh -xe ./build.sh
	sudo sh -xe ./deploy.sh
fi

if [ "$build_clusterodm" == "true" ]; then
	cd $WORKSPACE/uasdm/uasdm-clusterodm/build
	sh -xe ./init.sh
	sh -xe ./build.sh
	sudo sh -xe ./deploy.sh
fi
