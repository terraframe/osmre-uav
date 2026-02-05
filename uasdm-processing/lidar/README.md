
# What is it?

Standalone container, compatible with IDM's reusable fargate autoscaling, which runs silvimetric to process LIDAR data into outputs.


## How to Run?

1. First, run 'aws configure' and give it credentials to access IDM's aws workspace

export AWS_ACCESS_KEY_ID=$UASDM_S3_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_S3_SECRET

2. Now, you can run:

sudo docker build -t uasdm-lidar .

sudo -E docker run --rm \
  -e AWS_REGION=us-west-2 \
  -e S3_INPUT_URI="s3://osmre-uas-dev/lidar/lidar/lidar/lidar/raw/autzen.copc.laz" \
  -e S3_OUTPUT_URI="s3://osmre-uas-dev/lidartesting/" \
  -e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY_ID" -e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_ACCESS_KEY" \
  uasdm-lidar



docker run --rm \
  --entrypoint /usr/bin/env \
  -e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY_ID" \
  -e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_ACCESS_KEY" \
  uasdm-lidar | grep -E '^AWS_'

