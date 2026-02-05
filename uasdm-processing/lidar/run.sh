#!/bin/bash
#
# Copyright 2020 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

## set shell options ##
set -e

## ensure required parameters are set ##
if [ -z "$S3_INPUT_URI" ]; then
    echo "Missing required env var: S3_INPUT_URI (example: s3://bucket/path/file.copc.laz)"
    exit 1
fi
if [ -z "$S3_OUTPUT_URI" ]; then
    echo "Missing required env var: S3_OUTPUT_URI (example: s3://bucket/path/output-prefix/)"
    exit 1
fi

AWS_REGION="${AWS_REGION:-us-west-2}"

WORKDIR="${WORKDIR:-/work}"
INPUT_DIR="${INPUT_DIR:-$WORKDIR/input}"
OUTPUT_DIR="${OUTPUT_DIR:-$WORKDIR/output}"

mkdir -p "$INPUT_DIR"
mkdir -p "$OUTPUT_DIR"

## download input ##
# Expecting a single file URI, not a prefix.
case "$S3_INPUT_URI" in
  */)
    echo "S3_INPUT_URI looks like a prefix (ends with '/'). This container expects a single input file URI."
    exit 1
    ;;
esac

INPUT_BASENAME="$(basename "$S3_INPUT_URI")"
LOCAL_INPUT_FILE="$INPUT_DIR/$INPUT_BASENAME"

echo "Downloading: $S3_INPUT_URI"
aws s3 cp "$S3_INPUT_URI" "$LOCAL_INPUT_FILE" --region "$AWS_REGION" --no-progress

# sanity check
if [ ! -f "$LOCAL_INPUT_FILE" ]; then
    echo "Download failed: '$LOCAL_INPUT_FILE' not found."
    exit 1
fi

## Convert point cloud to COPC ##
if [[ "$LOCAL_INPUT_FILE" != *.copc.laz ]]; then
  /opt/lidar/idm_pdal_translate_copc.sh /opt/conda/etc/profile.d/conda.sh "$LOCAL_INPUT_FILE" "$OUTPUT_DIR/translated.copc.laz"
  LOCAL_INPUT_FILE="$OUTPUT_DIR/translated.copc.laz"
  
  # TODO : The copc converter is a ManagedDocument. Which means : It creates a document in the database and uploads generated files to S3.
  # This is interesting because the resultant database files depend upon what we decide here..
  # this.s3Path = ImageryComponent.PTCLOUD + "/pointcloud.copc.laz";
fi

## run silvimetric ##
/opt/lidar/silvimetric_idm.sh /opt/conda/etc/profile.d/conda.sh "$LOCAL_INPUT_FILE" "$OUTPUT_DIR"

##
## Convert selected outputs to COG (gdaladdo -> gdal_translate) and upload
##
COG_FILES=( \
  "m_Z_max.tif" \
  "m_Z_min.tif" \
  "m_Z_diff.tif" \
  "m_Classification_veg_density.tif" \
)

echo "Generating COGs for generated artifacts and uploading..."
for fname in "${COG_FILES[@]}"; do
  src="$OUTPUT_DIR/$fname"
  if [ ! -f "$src" ]; then
    echo "Warning: expected file not found, skipping: $src" >&2
    continue
  fi

  base="${fname%.tif}"
  overview="$OUTPUT_DIR/${base}.overview.tif"
  cog="$OUTPUT_DIR/${base}.cog.tif"

  # Make a working copy to build overviews on (keeps original untouched)
  cp -f "$src" "$overview"

  # Build internal overviews
  conda run -n silvimetric gdaladdo -r average "$overview" 2 4 8 16

  # Translate to COG using the overviewed input
  conda run -n silvimetric gdal_translate "$overview" "$cog" -of COG -co COMPRESS=LZW -co BIGTIFF=YES

  # (Optional) remove intermediate overview file
  rm -f "$overview"

  echo "Uploading: $(basename "$cog") -> $S3_OUTPUT_URI"
  aws s3 cp "$cog" "$S3_OUTPUT_URI" --region "$AWS_REGION" --no-progress
done

## upload everything else ##
#echo "Uploading (sync): $OUTPUT_DIR -> $S3_OUTPUT_URI"
#aws s3 sync "$OUTPUT_DIR" "$S3_OUTPUT_URI" --region "$AWS_REGION" --no-progress

echo "Done."
