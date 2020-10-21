#!/bin/bash

docker run --rm --name uasdm-fargate-erossync-test --network host \
  -e EROSSYNC_FTP_TARGET_PATH="eros/test" -e EROSSYNC_FTP_SERVER=ftp.terraframe.com \
  -e EROSSYNC_FTP_USERNAME="terraframe@terraframe.com" -e EROSSYNC_FTP_PASSWORD="_Terra4Frame" \
  -e EROSSYNC_S3_BUCKET="terraframe-test-bucket" -e EROSSYNC_S3_SOURCE_PATH="TestFolder" \
  -e AWS_REGION=us-west-2 -e AWS_ACCESS_KEY_ID="AKIA575O47JSLYC3KOAH" -e AWS_SECRET_ACCESS_KEY="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" \
  uasdm-fargate-erossync
