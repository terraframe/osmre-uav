#!/bin/bash
# Run this with sudo

# Requires AWS CLI : pip3 install awscli --upgrade --user
# https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html

# Exit immediately if anything errors out
set -e

# Kill any running containers by name of what we're about to run
docker rm -f $(docker ps -a -q --filter="name=orientdb") || true

# Pull & Run the orientdb container
docker run -d -p 2424:2424 -p 2480:2480 -e ORIENTDB_ROOT_PASSWORD=root --name orientdb orientdb:3.0
