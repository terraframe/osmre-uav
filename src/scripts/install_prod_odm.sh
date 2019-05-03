# RUN AS SUDO

# Install Docker (RHEL)
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
yum install -y --setopt=obsoletes=0 docker-ce-17.03.2.ce-1.el7.centos.x86_64 docker-ce-selinux-17.03.2.ce-1.el7.centos.noarch
service docker start

# Docker setup
docker run -v /data/odm:/var/www/datavol -d -p 3000:3000 --restart always --name odm opendronemap/node-opendronemap:0.3.1

docker exec odm bash -c 'rm -r /var/www/data'
docker exec odm bash -c 'rm -r /var/www/tmp'

docker exec odm bash -c 'mkdir /var/www/datavol/data'
docker exec odm bash -c 'mkdir /var/www/datavol/tmp'

docker exec odm bash -c 'ln -s /var/www/datavol/data /var/www/data'
docker exec odm bash -c 'ln -s /var/www/datavol/tmp /var/www/tmp'