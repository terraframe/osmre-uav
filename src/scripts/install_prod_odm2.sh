#!/bin/bash
# Assumes superuser
# Don't run this script blindly! Intelligently pick out what you need.

# Exit immediately if anything errors out
set -e

# Install Docker (RHEL)
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install -y --setopt=obsoletes=0 docker-ce-17.03.2.ce-1.el7.centos.x86_64 docker-ce-selinux-17.03.2.ce-1.el7.centos.noarch
service docker start

# Requires AWS CLI : pip install awscli --upgrade --user
# https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html

# export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
# export AWS_SECRET_ACCESS_KEY=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
# eval $(aws ecr get-login --no-include-email --region us-west-2)

# Pull the micasense container (our NodeODM might launch it at runtime)
docker pull 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense:latest



##### These commands are specific to the DEV container!! ####

docker run -d -p 3001:3000 --restart always -v /data/odm/dev/micasense:/opt/micasense -v /data/odm/dev/data:/var/www/datavol -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/dev/micasense --name uasdm-nodeodm-dev 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-nodeodm:latest

docker exec uasdm-nodeodm-dev bash -c 'rm -r /var/www/data'
docker exec uasdm-nodeodm-dev bash -c 'rm -r /var/www/tmp'

docker exec uasdm-nodeodm-dev bash -c 'mkdir /var/www/datavol/data'
docker exec uasdm-nodeodm-dev bash -c 'mkdir /var/www/datavol/tmp'

docker exec uasdm-nodeodm-dev bash -c 'ln -s /var/www/datavol/data /var/www/data'
docker exec uasdm-nodeodm-dev bash -c 'ln -s /var/www/datavol/tmp /var/www/tmp'



##### These commands are specific to the STAGING container!! ####

docker run -d -p 3002:3000 --restart always -v /data/odm/staging/micasense:/opt/micasense -v /data/odm/staging/data:/var/www/datavol -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/staging/micasense --name uasdm-nodeodm-staging 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-nodeodm:latest

docker exec uasdm-nodeodm-staging bash -c 'rm -r /var/www/data'
docker exec uasdm-nodeodm-staging bash -c 'rm -r /var/www/tmp'

docker exec uasdm-nodeodm-staging bash -c 'mkdir /var/www/datavol/data'
docker exec uasdm-nodeodm-staging bash -c 'mkdir /var/www/datavol/tmp'

docker exec uasdm-nodeodm-staging bash -c 'ln -s /var/www/datavol/data /var/www/data'
docker exec uasdm-nodeodm-staging bash -c 'ln -s /var/www/datavol/tmp /var/www/tmp'
