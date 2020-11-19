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

# This tells the build which version of npm to use:
. $NVM_DIR/nvm.sh && nvm install lts/erbium

:
: ----------------------------------
:  CONFIGURE  
: ----------------------------------
:
# Configure the build based on Jenkins parameters
sed -i -e 's/ec2-52-33-51-128.us-west-2.compute.amazonaws.com/ip-172-31-2-248.us-west-2.compute.internal/g' $WORKSPACE/geoprism-platform/ansible/inventory/uasdm/$environment.ini

export ANSIBLE_HOST_KEY_CHECKING=false
export NODE_OPTIONS="--max_old_space_size=1500"


if [ "$build_artifact" == "true" ]; then
  :
  : ----------------------------------
  :  BUILD
  : ----------------------------------
  :
  ## Build angular source ##
  cd $WORKSPACE/uasdm/uasdm-web/src/main/ng2
  npm version
  [ -e ./node_modules ] && rm -r node_modules
  npm install
  npm install typings
  #typings install lodash
  node -v && npm -v
  node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile

  :
  : ----------------------------------
  :  TEST  
  : ----------------------------------
  :
  if [ "$run_tests" == "true" ]; then
  cd $WORKSPACE/uasdm
  #mvn clean install -B -P patch -Dgeoprism.basedir=$WORKSPACE/geoprism -Droot.clean=true -Ddatabase.port=5432
  #cd uasdm-test && mvn test -Dgeoprism.basedir=$WORKSPACE/geoprism -Ddatabase.port=5432
  fi


  :
  : ----------------------------------
  : DEPLOY ARTIFACT
  : ----------------------------------
  :
  cd $WORKSPACE/uasdm
  mvn clean deploy -B
fi


:
: ----------------------------------
:  DEPLOY
: ----------------------------------
:

cd $WORKSPACE/geoprism-cloud/ansible

[ -e ./roles ] && unlink ./roles
ln -s $WORKSPACE/geoprism-cloud/ansible/roles ./roles
[ -e ./uasdm.yml ] && unlink ./uasdm.yml
ln -s $WORKSPACE/geoprism-cloud/ansible/uasdm.yml ./uasdm.yml

ansible-playbook -v -i ./inventory/uasdm/$environment.ini ./uasdm.yml --extra-vars "clean_db=$clean_db clean_solr=$clean_solr clean_orientdb=$clean_orientdb artifact_version=$version"
