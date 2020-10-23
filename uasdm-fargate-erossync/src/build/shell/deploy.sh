#!/bin/bash

sudo docker tag uasdm-fargate-erossync:latest 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-fargate-erossync:latest

export AWS_ACCESS_KEY_ID=AKIA32XPOGK7S2QZHXFN
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --region us-east-1 --no-include-email)
sudo docker push 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-fargate-erossync:latest
