====
    Copyright 2020 The Department of Interior

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

# HEADS UP #

This project is now deprecated / legacy and only included here for archival purposes.
The actual thumbnail code exists in the "python-serverless" directory.

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
