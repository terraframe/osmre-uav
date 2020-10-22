#!/bin/bash

sudo docker rm -f uasdm-fargate-erossync-test-ftp-server || true

# Boot a test ftp server
sudo docker run -d --rm --name uasdm-fargate-erossync-test-ftp-server \
            -p 39538:21 \
            -e FTP_USER=richtest -e FTP_PASSWORD=richtest5697 -e HOST=52.13.165.218 \
            -p 65000-65004:65000-65004 \
            -e PASV_MIN_PORT=65000 -e PASV_MAX_PORT=65004 \
            teezily/ftpd
