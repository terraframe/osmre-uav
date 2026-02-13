
# What is it?

Standalone container, compatible with IDM's reusable fargate autoscaling, which runs silvimetric to process LIDAR data into outputs.


## How to Run?

1. First, set AWS credentials to access IDM's aws workspace

export AWS_ACCESS_KEY_ID=$UASDM_S3_KEY
export AWS_SECRET_ACCESS_KEY=$UASDM_S3_SECRET

2. Now, you can run:

sudo docker build -t uasdm-lidar .

sudo -E docker run --rm \
  -e AWS_REGION=us-west-2 \
  -e S3_COMPONENT="s3://osmre-uas-dev/lidar/lidar/lidar/lidar" \
  -e JOB_ID="9f3c2e6b-7a4d-4c91-b8f2-6e5d1a0c3f77" \
  -e AWS_ACCESS_KEY_ID="$AWS_ACCESS_KEY_ID" -e AWS_SECRET_ACCESS_KEY="$AWS_SECRET_ACCESS_KEY" \
  uasdm-lidar

