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

export DOCKER_CLIENT_TIMEOUT=120
export COMPOSE_HTTP_TIMEOUT=120

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
else
  if [ "$tag" == "latest" ]; then
    # As far as I can tell Cloudsmith doesn't support fetching the latest version of an artifact from their REST API. So we're using Maven dependency:copy plugin.
    mkdir -p $WORKSPACE/uasdm/uasdm-web/target/artifact-download
    cp $WORKSPACE/uasdm/src/build/shell/artifact-download.pom.xml $WORKSPACE/uasdm/uasdm-web/target/artifact-download/pom.xml
    cd $WORKSPACE/uasdm/uasdm-web/target/artifact-download
    
    mvn dependency:copy -B -Dartifact=gov.osmre.uasdm:uasdm-web:LATEST:war -DoutputDirectory=../ -Dmdep.stripVersion=true
    mv ../uasdm-web.war ../uasdm.war
  else
    mkdir -p $WORKSPACE/uasdm/uasdm-web/target && wget -nv -O $WORKSPACE/uasdm/uasdm-web/target/uasdm.war "https://dl.cloudsmith.io/public/terraframe/osmre-uav/maven/gov/osmre/uasdm/uasdm-web/$tag/uasdm-web-$tag.war"
  fi
fi

:
: ----------------------------------
:  DEPLOY
: ----------------------------------
:

if [ "$deploy" == "true" ]; then
  # Build a Docker image
  cd $WORKSPACE/uasdm/src/build/docker/uasdm
  ./build.sh

  # Run Ansible deploy
  cd $WORKSPACE/geoprism-cloud/ansible

  [ -h ./inventory ] && unlink ./inventory
  [ -d ./inventory ] && rm -r ./inventory
  ln -s $WORKSPACE/geoprism-platform/ansible/inventory ./inventory

  [ -h ../permissions ] && unlink ../permissions
  ln -s $WORKSPACE/geoprism-platform/permissions ../permissions

  ansible-playbook -v -i ./inventory/uasdm/$environment.ini ./uasdm.yml --extra-vars "clean_db=$clean_db clean_solr=$clean_solr clean_orientdb=$clean_orientdb elasticsearch.clean=$elasticsearch.clean webserver_docker_image_tag=$tag docker_image_path=../../uasdm/src/build/docker/uasdm/target/uasdm.dimg.gz"
fi
