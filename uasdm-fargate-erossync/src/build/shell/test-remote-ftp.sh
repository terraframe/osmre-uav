#!/bin/bash

# The purpose of this file is to document how a ftp server was setup on an ec2
# instance for purposes of testing.


sudo docker rm -f uasdm-fargate-erossync-test-ftp-server || true

# Boot a test ftp server
sudo docker run -d --rm --name uasdm-fargate-erossync-test-ftp-server \
            -p 39538:21 \
            -e FTP_USER=uasdm-dev-test -e FTP_PASSWORD=dy6hZm6V -e HOST=52.0.230.226 \
            -p 65000-65004:65000-65004 \
            -e PASV_MIN_PORT=65000 -e PASV_MAX_PORT=65004 \
            -v /data/ftp:/data \
            teezily/ftpd
