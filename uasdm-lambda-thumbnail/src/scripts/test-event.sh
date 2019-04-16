aws lambda invoke --function-name uasdm-lambda-thumbnail --invocation-type Event \
--payload file://$THUMBNAIL_WORKSPACE/src/test/geodashboard_map.txt $THUMBNAIL_WORKSPACE/test-results.txt
