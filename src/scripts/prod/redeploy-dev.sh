
# docker system prune -a

export AWS_ACCESS_KEY_ID=AKIAIKFVZC4DZ3NIGP4A
export AWS_SECRET_ACCESS_KEY=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
eval $(aws ecr get-login --no-include-email --region us-west-2)

docker pull 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense:latest
docker tag 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-micasense uasdm-micasense:latest

docker rm -f uasdm-nodeodm-dev

docker run -d -p 3001:3000 --restart always -v /data/odm/dev/micasense:/opt/micasense -v /data/odm/dev/data:/var/www/datavol -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock -e MICASENSE_HOST_BINDING=/data/odm/dev/micasense --name uasdm-nodeodm-dev 961902606948.dkr.ecr.us-west-2.amazonaws.com/uasdm-nodeodm:latest

docker exec uasdm-nodeodm-dev bash -c 'rm -r /var/www/data'
docker exec uasdm-nodeodm-dev bash -c 'rm -r /var/www/tmp'

docker exec uasdm-nodeodm-dev bash -c 'mkdir /var/www/datavol/data'
docker exec uasdm-nodeodm-dev bash -c 'mkdir /var/www/datavol/tmp'

docker exec uasdm-nodeodm-dev bash -c 'ln -s /var/www/datavol/data /var/www/data'
docker exec uasdm-nodeodm-dev bash -c 'ln -s /var/www/datavol/tmp /var/www/tmp'