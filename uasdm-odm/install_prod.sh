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

# Assumes superuser

#### IMPORTANT ####
# This file currently serves more as documentation as to how one would deploy. It cannot currently be run in it's entirety, you need to intelligently
#   pick and choose which commands to run based on what it is that you're doing. At some point this script will probably be converted into ansible.
#
# If you're redeploying only the Node ODM container, you have to also remake the Cluster ODM container. The reason for this is because the Cluster ODM
# docker run configuration includes a link to the Node ODM container. If the Node ODM container is removed, then this link is no longer valid and must
# be re-created.
###################

# Exit immediately if anything errors out
set -e

# Install Docker (RHEL)
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install -y --setopt=obsoletes=0 docker-ce-17.03.2.ce-1.el7.centos.x86_64 docker-ce-selinux-17.03.2.ce-1.el7.centos.noarch
service docker start


# Consider using (if all containers are live and running)
docker system prune --volumes # TODO : Be careful since this may delete the micasense image
docker image prune -a # TODO : Be careful since this may delete the micasense image

# Requires AWS CLI : pip install awscli --upgrade --user
# https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html

export AWS_ACCESS_KEY_ID=$UASDM_ECR_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_ECR_SECRET
#aws ecr get-login --region us-east-1 | docker login --username AWS --password-stdin 813324710591.dkr.ecr.us-east-1.amazonaws.com
#$(aws ecr get-login --no-include-email --region us-east-1)
aws ecr get-login-password --region us-east-1 | sudo docker login --username AWS --password-stdin 813324710591.dkr.ecr.us-east-1.amazonaws.com

# Pull the latest docker containers
docker pull 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-nodeodm:latest
docker pull 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-micasense:latest
docker tag 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-micasense uasdm-micasense



##### These commands are specific to the DEV container!! ####

docker rm -f uasdm-nodeodm-dev
docker rm -f uasdm-clusterodm-dev
docker run -d -p 3001:3000 --restart always -v /data/odm/dev/micasense:/opt/micasense -v /data/odm/dev/data/data:/var/www/data -v /data/odm/dev/data/tmp:/var/www/tmp -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/dev/micasense --name uasdm-nodeodm-dev 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-nodeodm:latest

##### These commands are specific to the STAGING container!! ####

docker rm -f uasdm-nodeodm-staging
docker rm -f uasdm-clusterodm-staging
docker run -d -p 3002:3000 --restart always -v /data/odm/staging/micasense:/opt/micasense -v /data/odm/staging/data/data:/var/www/data -v /data/odm/staging/data/tmp:/var/www/tmp -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/staging/micasense --name uasdm-nodeodm-staging 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-nodeodm:latest

##### These commands are specific to the PROD container!! ####

docker rm -f uasdm-nodeodm-prod
docker rm -f uasdm-clusterodm-prod
docker run -d -p 3000:3000 --restart always -v /data/odm/prod/micasense:/opt/micasense -v /data/odm/prod/data/data:/var/www/data -v /data/odm/prod/data/tmp:/var/www/tmp -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/prod/micasense --name uasdm-nodeodm-prod 813324710591.dkr.ecr.us-east-1.amazonaws.com/uasdm-nodeodm:latest


### Clean up the space again afterwards
# TODO : Won't this remove the micasense image? Since it's only used on demand. Seems like a bad idea?
docker system prune --volumes
docker image prune -a

