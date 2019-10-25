#!/bin/bash
# Do not run with sudo

# Exit immediately if anything errors out
set -e

# Checkout ODM v0.9.1
git clone https://github.com/OpenDroneMap/ODM.git -b v0.9.1

# Checkout NodeODM v0.3.1
git clone https://github.com/OpenDroneMap/NodeODM.git
cd NodeODM
git checkout 911bf08b1b897bc31b19625b03b4e3a4f9c8f95b # NodeODM doesn't have tags so we must check out this hash
sed -i '1s/.*/FROM uasdm-odm/' Dockerfile

cd ../