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

#if curl --user ansible:sPc0*059 -f -s --head "http://nexus.terraframe.com/service/local/artifact/maven/redirect?r=private&g=gov.osmre.uasdm&a=uasdm-web&p=war&v=$IDM_VERSION" | head -n 1 | grep "HTTP/1.[01] [23].." > /dev/null; then
#    echo "The release version $IDM_VERSION has already been deployed! Please ensure you are releasing the correct version of geoprism."
#    exit 1
#fi

git config --global user.name $GIT_TF_BUILDER_USERNAME
git config --global user.email builder@terraframe.com

. $NVM_DIR/nvm.sh && nvm install lts/erbium

if [ "$release_uasdm" == "true" ]; then
  ## Update IDM Version in System Component and Commit Compiled NodeJS Source
  cd $WORKSPACE
  rm -rf builderdev
  mkdir builderdev
  cd builderdev
  git clone -b master git@github.com:terraframe/osmre-uav.git
  cd osmre-uav
  git checkout dev
  sed -i -E "s_<span id=\"automated-version-replace\">.*</span>_<span id=\"automated-version-replace\">$IDM_VERSION</span>_g" uasdm-web/src/main/ng2/src/app/admin/component/system/system-info.component.html
  cd uasdm-web/src/main/ng2
  npm install
  node -v && npm -v
  node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile
  cd $WORKSPACE/builderdev/osmre-uav
  git add -A
  git diff-index --quiet HEAD || git commit -m 'Preparing for release'
  git push
  cd $WORKSPACE
  rm -rf builderdev
  
  ## License Headers
  cd $WORKSPACE/uasdm
  git checkout dev
  git pull
  mvn license:format -B
  git add -A
  git diff-index --quiet HEAD || git commit -m 'License headers'
  git push
  git checkout master
  git merge dev
  git push
  
  cd $WORKSPACE/uasdm
  
  mvn release:prepare -B -Dtag=$IDM_VERSION \
                   -DreleaseVersion=$IDM_VERSION \
                   -DdevelopmentVersion=$IDM_NEXT
                   
  mvn release:perform -B -Darguments="-Dmaven.javadoc.skip=true -Dmaven.site.skip=true"
  
  
  cd ..
  rm -rf rwdev
  mkdir rwdev
  cd rwdev
  git clone -b master git@github.com:terraframe/osmre-uav.git
  cd osmre-uav
  git checkout dev
  git merge master
  git push
fi

if [ "$tag_platform" == "true" ]; then
  cd $WORKSPACE
  git clone -b master git@github.com:terraframe/geoprism-platform.git
  cd geoprism-platform
  git merge origin/dev
  git push
  git tag -a idm-$IDM_VERSION -m "Deployment scripts for IDM version $IDM_VERSION"
  git push origin idm-$IDM_VERSION
fi

if [ "$tag_cloud" == "true" ]; then
  cd $WORKSPACE
  git clone -b master git@github.com:terraframe/geoprism-cloud.git
  cd geoprism-cloud
  git merge origin/dev
  git push
  git tag -a idm-$IDM_VERSION -m "Deployment scripts for IDM version $IDM_VERSION"
  git push origin idm-$IDM_VERSION
fi

if [ "$release_github" == "true" ]; then
  cd $WORKSPACE/uasdm
  
  gh config set prompt disabled
  
  # TODO : Downloadable artifacts
  
  gh release create $IDM_VERSION
fi
