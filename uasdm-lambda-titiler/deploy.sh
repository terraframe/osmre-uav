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


# Run as sudo
# Not yet fully automated! Commands in this file must be run manually.

# https://devseed.com/titiler/deployment/aws/lambda/

#### Only necessary once on an account ####
# npm run cdk bootstrap aws://813324710591/us-east-1
####

set -e

cd ./titiler/deployment/aws

# Run these once
python -m pip install -r requirements-cdk.txt
npm run cdk bootstrap

export AWS_ACCESS_KEY_ID=$UASDM_TITILER_LAMBDA_DEPLOY_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_TITILER_LAMBDA_DEPLOY_SECRET

export TITILER_STACK_MEMORY=2048
export TITILER_STACK_TIMEOUT=900
export AWS_DEFAULT_REGION=us-east-1
export AWS_REGION=us-east-1

DeployStage () {
  export TITILER_STACK_NAME="titiler-private"
  export TITILER_STACK_BUCKETS="[\"osmre-uas-${TITILER_STACK_STAGE}\"]"
  npm run cdk deploy "${TITILER_STACK_NAME}-lambda-${TITILER_STACK_STAGE}" --require-approval never
  
  # TODO : This cannot be automated (yet) because it requires output from the cdk deploy to be piped into here (in the route-id)
  # https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-access-control-iam.html
  # aws apigatewayv2 get-apis
  # aws apigatewayv2 get-routes --api-id xxxx
  aws apigatewayv2 update-route --api-id xxxx --route-id yyyyy --authorization-type AWS_IAM
  
  # TODO : Cannot be automated! Make sure that the system's IAM user has access to this route, ala:
  # https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-control-access-using-iam-policies-to-invoke-api.html


  # Public Titiler instances were destroyed. This code is their only legacy. Delete at some point if it becomes obvious we no longer need
  #export TITILER_STACK_NAME="titiler-public"
  #export TITILER_STACK_BUCKETS="[\"osmre-uas-${TITILER_STACK_STAGE}-public\"]"
  # TODO : This command cannot be automated yet because it requires user input.
  #npm run cdk deploy "${TITILER_STACK_NAME}-lambda-${TITILER_STACK_STAGE}"
}

export TITILER_STACK_STAGE="dev"
DeployStage

export TITILER_STACK_STAGE="dev-deploy"
#DeployStage

export TITILER_STACK_STAGE="staging"
#DeployStage

export TITILER_STACK_STAGE="prod"
#DeployStage

cd ../../../
