#!/bin/bash

## script params ##
out="/opt/silvimetric/metrics"
input="/opt/silvimetric/input.copc.laz"

## set shell options ##
set -e
set -x

## ensure required parameters are set ##
# Check if the file exists and is a regular file
if [ ! -f "$1" ]; then
    echo "The specified input file '$1' does not exist or is not a regular file."
    exit 1
fi
if [ ! -d "$2" ]; then
    echo "The specified output directory '$2' does not exist or is not a directory."
    exit 1
fi

## initialize conda ##
source /opt/conda/etc/profile.d/conda.sh
conda activate silvimetric
pip install silvimetric

file $1

## begin script ##

bounds=$(pdal info $1 --readers.copc.resolution=1 | jq -c '.stats.bbox.native.bbox')

crs=$(pdal info --metadata $1 --readers.copc.resolution=10 | jq -c '.metadata.srs.json.components[0].id.code')

rm -rf database.tdb
silvimetric --database database.tdb \
    initialize \
    --bounds $bounds \
    --crs $crs

silvimetric -d database.tdb \
   --threads 4 \
   --workers 4 \
   shatter \
   --date 2008-12-01 \
   $input

silvimetric -d database.tdb extract -o $2

rm -rf database.tdb
