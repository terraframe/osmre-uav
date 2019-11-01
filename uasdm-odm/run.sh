
docker rm -f $(docker ps -a -q --filter="name=uasdm-nodeodm") || true
docker run -d -p 3000:3000 -v /opt/odm-micasense-temp:/opt/micasense -e MICASENSE_HOST_BINDING=/opt/odm-micasense-temp -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock --name uasdm-nodeodm uasdm-nodeodm

echo "The server should be running at http://localhost:3000/"
