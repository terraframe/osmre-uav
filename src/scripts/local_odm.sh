
docker rm -f $(docker ps -a -q --filter=name=odm)

sudo docker run -d -p 3000:3000 --name odm opendronemap/node-opendronemap:0.3.1
