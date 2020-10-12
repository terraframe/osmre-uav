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

aws lambda create-function --function-name uasdm-lambda-thumbnail2 \
--zip-file fileb://$THUMBNAIL_WORKSPACE/target/uasdm-lambda-thumbnail.zip --handler index.handler --runtime nodejs8.10 \
--timeout 10 --memory-size 1024 \
--role arn:aws:iam::961902606948:role/uasdm-test-lambda-s3-role
