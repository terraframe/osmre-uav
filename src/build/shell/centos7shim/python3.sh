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


# This file exists as documentation for how to upgrade a raw centos7 server to have python3.8 on it, which is required for newer ansible versions.
# These commands were run on dev+staging+prod IDM production servers (on Oct27 2025) to install python3 on them, since CentOS7 doesn't come with python3 natively.


# 1️⃣ Make sure you’re root or using sudo
sudo -i

# 2️⃣ Update package metadata
yum -y update

# 3️⃣ Install Oracle Software Collections repo (SCL) – this is where Python 3.8 lives
yum install -y oracle-softwarecollection-release-el7

# 4️⃣ Install scl-utils (manages SCL environments) and Python 3.8
sudo yum install -y scl-utils rh-python38

# 6️⃣ Verify it’s there
/opt/rh/rh-python38/root/usr/bin/python3.8 --version
# Expected output: Python 3.8.x

# 7️⃣ Add a helper symlink if you want `python3` in PATH
ln -s /opt/rh/rh-python38/root/usr/bin/python3.8 /usr/local/bin/python3
ln -s /opt/rh/rh-python38/root/usr/bin/pip3.8 /usr/local/bin/pip3

## NOTE : This doesn't setup python3 for root automatically. But whatever. ChatGPT says you can either use the full path or 'enable SCL' to use python as root
