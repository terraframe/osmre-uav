#!/bin/bash

sudo docker tag uasdm-fargate-erossync:latest 170862717399.dkr.ecr.us-east-1.amazonaws.com/uasdm-fargate-erossync:latest

export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
eval $(aws ecr get-login --region us-east-1 --no-include-email)
sudo docker push 170862717399.dkr.ecr.us-east-1.amazonaws.com/uasdm-fargate-erossync:latest
