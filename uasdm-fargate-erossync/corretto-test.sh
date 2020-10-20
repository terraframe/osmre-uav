#!/bin/bash

cd ~/dev/projects/uasdm/git/uasdm/uasdm-fargate-erossync

docker run --rm --name uasdm-fargate-erossync-test --network host -e FTP_USERNAME=terraframe@terraframe.com -e FTP_PASSWORD=_Terra4Frame -e AWS_REGION=us-west-2 -e AWS_ACCESS_KEY_ID=AKIA575O47JSLYC3KOAH -e AWS_SECRET_ACCESS_KEY=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 -v $(pwd)/target:/target amazoncorretto:11 java -cp /target/lib/*:/target/classes gov.osmre.uasdm.erossync.App
