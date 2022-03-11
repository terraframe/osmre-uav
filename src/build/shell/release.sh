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

#if curl -f -s --head "https://dl.cloudsmith.io/public/terraframe/osmre-uav/maven/gov/osmre/uasdm/uasdm-server/$CGR_RELEASE_VERSION/uasdm-server-$CGR_RELEASE_VERSION.jar" | head -n 1 | grep "HTTP/1.[01] [23].." > /dev/null; then
#    echo "The release version $IDM_VERSION has already been deployed! Please ensure you are releasing the correct version."
#    exit 1
#fi

git config --global user.name "$GIT_TF_BUILDER_USERNAME"
git config --global user.email builder@terraframe.com

. $NVM_DIR/nvm.sh && nvm install lts/erbium

if [ "$release_uasdm" == "true" ]; then
  ## Update IDM Version in System Component and Commit Compiled NodeJS Source
  cd $WORKSPACE/uasdm
  
  git checkout $release_branch
  git pull
  sed -i -E "s_<span id=\"automated-version-replace\">.*</span>_<span id=\"automated-version-replace\">$IDM_VERSION</span>_g" uasdm-web/src/main/ng2/src/app/admin/component/system/system-info.component.html
  cd uasdm-web/src/main/ng2
  npm install
  node -v && npm -v
  node --max_old_space_size=4096 ./node_modules/webpack/bin/webpack.js --config config/webpack.prod.js --profile
  cd $WORKSPACE/uasdm
  git add -A
  git diff-index --quiet HEAD || git commit -m 'Preparing for release'
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
  
  ## License Headers
  cd $WORKSPACE/uasdm
  git checkout $release_branch
  mvn license:format -B
  git add -A
  git diff-index --quiet HEAD || git commit -m 'License headers'
  if [ "$dry_run" == "false" ]; then
    git push
  else
    git reset --hard
    git clean -fdx
  fi
  
  # Release
  cd $WORKSPACE/uasdm
  git checkout $release_branch
  mvn release:prepare -B -DdryRun=$dry_run -Dtag=$IDM_VERSION \
                   -DreleaseVersion=$IDM_VERSION \
                   -DdevelopmentVersion=$IDM_NEXT
                   
  mvn release:perform -B -DdryRun=$dry_run -Darguments="-Dmaven.javadoc.skip=true -Dmaven.site.skip=true"
fi

if [ "$release_docker" == "true" ]; then
  cd $WORKSPACE/uasdm/src/build/docker/uasdm
  export tag=$IDM_VERSION
  ./build.sh
  
  if [ "$dry_run" == "false" ]; then
    ./release.sh
  fi
fi

if [ "$tag_platform" == "true" ]; then
  cd $WORKSPACE/geoprism-platform
  
  git checkout master
  git merge origin/dev
  
  if [ "$dry_run" == "false" ]; then
    git push
    git tag -a idm-$IDM_VERSION -m "Deployment scripts for IDM version $IDM_VERSION"
    git push origin idm-$IDM_VERSION
  else
    git tag -a idm-$IDM_VERSION -m "Deployment scripts for IDM version $IDM_VERSION"
  fi
fi

if [ "$tag_cloud" == "true" ]; then
  cd $WORKSPACE/geoprism-cloud
  
  git checkout master
  git merge origin/dev
  
  if [ "$dry_run" == "false" ]; then
    git push
    git tag -a idm-$IDM_VERSION -m "Deployment scripts for IDM version $IDM_VERSION"
    git push origin idm-$IDM_VERSION
  else
    git tag -a idm-$IDM_VERSION -m "Deployment scripts for IDM version $IDM_VERSION"
  fi
fi

if [ "$release_github" == "true" ]; then
  cd $WORKSPACE/uasdm
  
  gh config set prompt disabled
  
  if [ "$dry_run" == "false" ]; then
    # TODO : We really should be using the artifacts we compiled earlier.
    sleep 180 # Cloudsmith takes a little bit to process the artifact before its downloadable.
    
    wget https://dl.cloudsmith.io/public/terraframe/osmre-uav/maven/gov/osmre/uasdm/uasdm-web/$IDM_VERSION/uasdm-web-$IDM_VERSION.war -O uasdm-web-$IDM_VERSION.war
    gh release create $IDM_VERSION "uasdm-web-$IDM_VERSION.war#Webapp War Artifact"
  fi
fi
