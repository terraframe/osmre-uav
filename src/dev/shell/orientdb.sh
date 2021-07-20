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

# Run this with sudo

# Requires AWS CLI : pip3 install awscli --upgrade --user
# https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html

# Exit immediately if anything errors out
set -e

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=orientdb") > /dev/null || true

# Pull & Run the orientdb container
docker run -d -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=root --name orientdb orientdb:3.0
