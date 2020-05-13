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

# CLEAN
rm -r $THUMBNAIL_WORKSPACE/target
mkdir $THUMBNAIL_WORKSPACE/target

# COMPILE
cp $THUMBNAIL_WORKSPACE/src/index.js $THUMBNAIL_WORKSPACE/target/index.js

cd $THUMBNAIL_WORKSPACE/target
npm install async gm

# PACKAGE
cd $THUMBNAIL_WORKSPACE/target
zip -r uasdm-lambda-thumbnail.zip .


cd $THUMBNAIL_WORKSPACE
ls target
