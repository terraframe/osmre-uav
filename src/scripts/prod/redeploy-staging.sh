
# docker system prune -a

export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --no-include-email --region us-west-2)

docker pull 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense:latest
docker tag 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense uasdm-micasense:latest

docker rm -f uasdm-nodeodm-staging

docker run -d -p 3002:3000 --restart always -v /data/odm/staging/micasense:/opt/micasense -v /data/odm/staging/data:/var/www/datavol -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/staging/micasense --name uasdm-nodeodm-staging 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-nodeodm:latest

docker exec uasdm-nodeodm-staging bash -c 'rm -r /var/www/data'
docker exec uasdm-nodeodm-staging bash -c 'rm -r /var/www/tmp'

docker exec uasdm-nodeodm-staging bash -c 'mkdir /var/www/datavol/data'
docker exec uasdm-nodeodm-staging bash -c 'mkdir /var/www/datavol/tmp'

docker exec uasdm-nodeodm-staging bash -c 'ln -s /var/www/datavol/data /var/www/data'
docker exec uasdm-nodeodm-staging bash -c 'ln -s /var/www/datavol/tmp /var/www/tmp'
