#!/bin/bash
# Run this with sudo after you run build.sh
# Careful with tags. This will replace the 'latest' tag. (Look up nuainces with docker tags).
# You might need to change the current 'latest' tag to have some version number, and then add this new one as 'latest'

set -e

docker tag uasdm-micasense:latest 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense:latest

export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --no-include-email --region us-west-2)
docker push 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense:latest
