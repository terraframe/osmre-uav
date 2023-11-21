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

:
: ----------------------------------
:  CONFIGURE  
: ----------------------------------
:
# Configure the build based on Jenkins parameters
sed -i -e 's/ec2-52-33-51-128.us-west-2.compute.amazonaws.com/ip-172-31-2-248.us-west-2.compute.internal/g' $WORKSPACE/geoprism-platform/ansible/inventory/uasdm/$environment.ini

export ANSIBLE_HOST_KEY_CHECKING=false
export NODE_OPTIONS="--max_old_space_size=1500"

export DOCKER_CLIENT_TIMEOUT=120
export COMPOSE_HTTP_TIMEOUT=120

:
: ----------------------------------
:  BUILD
: ----------------------------------
:
  cd $WORKSPACE/uasdm
  mvn clean install -B
  
  # Build a Docker image
  cd $WORKSPACE/uasdm/src/build/docker/uasdm
  ./build.sh

# Run Ansible deploy
cd $WORKSPACE/geoprism-platform/ansible/idm-user-migrate

ansible-playbook -v -i ./inventory/$environment.ini ./migrate.yml --extra-vars "docker_image_path=../../uasdm/src/build/docker/uasdm/target/uasdm.dimg.gz"
