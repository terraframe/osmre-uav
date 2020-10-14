
# How to Running

serverless deploy
serverless invoke -f uasdm-lambda-thumbnail-py --log


# Creating the required libvips layer
cd layer-libvips
docker run --rm -v "$PWD"/layer:/lambda/opt lambci/yumda:2 yum install -y vips.x86_64


# Environment thumbnail3
conda create -n thumbnail3 -c conda-forge -c bioconda -c defaults python=3.7 pip pyvips 'imagemagick<7.0.9' boto3
conda activate thumbnail3


# After adding a pip dependency:
pip list --format=freeze > requirements.txt


## REFERENCES

# https://www.serverless.com/blog/serverless-python-packaging
# https://dev.to/aws-heroes/aws-lambda-microservice-workshop-using-s3-libvips-ruby-4o96
