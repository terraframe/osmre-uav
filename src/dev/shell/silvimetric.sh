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

# Exit immediately if anything errors out
set -e

# Requires python 3
# Install conda
# Follow steps here to create silvimetric env : https://silvimetric.com/en/latest/quickstart.html

## new envcfg.properties ##
# silvimetric.cmd=/home/rrowlands/dev/projects/uasdm/uasdm/src/build/docker/uasdm/silvimetric_idm.sh /home/rrowlands/miniconda3/etc/profile.d/conda.sh
# pdal.bin=/home/rrowlands/miniconda3/envs/silvimetric/bin/pdal
# proj.data=/home/rrowlands/miniconda3/envs/silvimetric/share/proj
