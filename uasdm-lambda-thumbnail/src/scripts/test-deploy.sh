aws lambda create-function --function-name uasdm-lambda-thumbnail2 \
--zip-file fileb://$THUMBNAIL_WORKSPACE/target/uasdm-lambda-thumbnail.zip --handler index.handler --runtime nodejs8.10 \
--timeout 10 --memory-size 1024 \
--role arn:aws:iam::961902606948:role/uasdm-test-lambda-s3-role
