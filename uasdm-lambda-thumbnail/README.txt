# Thanks to:
https://docs.aws.amazon.com/lambda/latest/dg/with-s3-example.html


# Initial Setup Commands:

1. https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html
2. export THUMBNAIL_WORKSPACE=$(pwd)
3. aws configure

# Compile:
./src/scripts/build.sh

# Deploy a new version (on TerraFrame's AWS):
./src/scripts/test-deploy.sh
