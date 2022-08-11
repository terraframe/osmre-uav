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

# This tells the build which version of npm to use:
. $NVM_DIR/nvm.sh && nvm install lts/erbium

export ANSIBLE_HOST_KEY_CHECKING=false

:
: ----------------------------------
:  Build and test
: ----------------------------------
:

## Build angular source ##
npm version
cd $WORKSPACE/uasdm/uasdm-web/src/main/ng2
npm install
node -v && npm -v
node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile

## Run the tests ##
cd $WORKSPACE/uasdm
mvn install -B
cd $WORKSPACE/uasdm/uasdm-server
mvn install -B -P database -Ddb.clean=true -Ddatabase.port=5432 -Ddb.patch=false -Ddb.rootUser=postgres -Ddb.rootPass=postgres -Ddb.rootDb=postgres
cd $WORKSPACE/uasdm/uasdm-test
set +e
mvn test -Dappcfg=$WORKSPACE/uasdm/envcfg/dev -Ddatabase.port=5432 -Dproject.basedir=$WORKSPACE/uasdm
ecode=$?
mkdir -p $TEST_OUTPUT/uasdm-test/surefire-reports && cp $WORKSPACE/uasdm/uasdm-test/target/surefire-reports/* $TEST_OUTPUT/uasdm-test/surefire-reports/ && chmod 777 -R $TEST_OUTPUT
set -e
[ "$ecode" != 0 ] && exit $ecode;

exit 0;