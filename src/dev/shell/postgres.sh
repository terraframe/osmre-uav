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

# Run with super user
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

CONTAINER_NAME=idm-postgres
POSTGRES_PORT=5442
POSTGRES_ROOT_PASS=postgres
CONTAINER_DATA_DIR=/data/idm/postgres

# Exit immediately if anything errors out
set -e

# Delete any existing data directory
rm -rf $CONTAINER_DATA_DIR

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=$CONTAINER_NAME") > /dev/null || true

# Pull & Run the container
docker run --name $CONTAINER_NAME -e POSTGRES_PASSWORD=$POSTGRES_ROOT_PASS -v $CONTAINER_DATA_DIR:/var/lib/postgresql/data -d -p $POSTGRES_PORT:5432 postgis/postgis:14-3.2
