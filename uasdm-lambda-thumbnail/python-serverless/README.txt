
# Info

This library does not produce a working deployment. The reasons being:

1. Our actual deployment is in a different AWS region. This project works off the TF region
2. Our actual deployment has existing S3 buckets that cannot be managed by serverless. This is unsupported.

For these reasons, this project is used simply to build and upload the source and create a basic test environment
for development purposes. Once a source deployment is created, that is manually copied to our actual prod environment.

Additionally, libvips, which is required to exist on your box if you ever hope of running this code locally, is very
difficult to get running on your box. For this reason, it is recommended to do all your testing / development on AWS
Lambda, because the runtime is actually up and running there with a working version of libvips. 

# How to Running

serverless deploy
serverless invoke -f uasdm-lambda-thumbnail-py --log


# Creating the required libvips layer
cd layer-libvips
docker run --rm -v "$PWD"/layer:/lambda/opt lambci/yumda:2 yum install -y vips.x86_64


# Environment thumbnail3
conda create -n thumbnail3 -c conda-forge -c bioconda -c defaults python=3.7 pip pyvips 'imagemagick<7.0.9' boto3 smart-open
conda activate thumbnail3


# After adding a pip dependency:
pip list --format=freeze > requirements.txt


# Compiling libvips

A libvips layer was manually built and compiled using this fork:
https://github.com/terraframe/libvips-lambda

The libvips lambda layer project is very brittle and liable to get out of date quickly, so if you need to compile a new version
of libvips, all I can say is good luck. You'll probably need to do a fair amount of updating of code and such.

That being said, I have built and deployed a libvips layer which can be accessed here:
https://github.com/terraframe/libvips-lambda/releases/tag/v8.10.2



## REFERENCES

# https://www.serverless.com/blog/serverless-python-packaging
# https://dev.to/aws-heroes/aws-lambda-microservice-workshop-using-s3-libvips-ruby-4o96
