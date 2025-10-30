#!/bin/bash

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
