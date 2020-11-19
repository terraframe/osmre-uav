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


sudo docker rm -f uasdm-fargate-erossync-test-ftp-server || true

# Boot a test ftp server
sudo docker run -d --rm --name uasdm-fargate-erossync-test-ftp-server \
            -p 11021:21 \
            -e FTP_USER=test -e FTP_PASSWORD=test -e HOST=127.0.0.1 \
            -p 65000-65004:65000-65004 \
            -e PASV_MIN_PORT=65000 -e PASV_MAX_PORT=65004 \
            teezily/ftpd

# Run the App by uploading some very basic test data
sudo docker run --rm --name uasdm-fargate-erossync-test --network host \
  -e EROSSYNC_FTP_TARGET_PATH="eros/test" -e EROSSYNC_FTP_SERVER=127.0.0.1 \
  -e EROSSYNC_FTP_USERNAME="test" -e EROSSYNC_FTP_PASSWORD="test" \
  -e EROSSYNC_FTP_PORT="11021" -e EROSSYNC_FTP_PASSIVE="true" \
  -e EROSSYNC_S3_BUCKET="terraframe-test-bucket" -e EROSSYNC_S3_SOURCE_PATH="TestFolder" \
  -e AWS_REGION=us-west-2 -e AWS_ACCESS_KEY_ID="$TF_BUILDER_KEY" -e AWS_SECRET_ACCESS_KEY="$TF_BUILDER_SECRET" \
  uasdm-fargate-erossync
