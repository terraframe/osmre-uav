#!/bin/bash

## set shell options ##
set -e

## ensure required parameters are set ##
# Check if the file exists and is a regular file
if [ ! -f "$1" ]; then
    echo "The specified input file '$1' does not exist or is not a regular file."
    exit 1
fi
if [ ! -f "$2" ]; then
    echo "The specified input file '$2' does not exist or is not a regular file."
    exit 1
fi
if [ ! -d "$3" ]; then
    echo "The specified output directory '$3' does not exist or is not a directory."
    exit 1
fi

## initialize conda ##
source $1
conda activate silvimetric
pip install silvimetric

## begin script ##

bounds=$(pdal info $2 --readers.copc.resolution=1 | jq -c '.stats.bbox.native.bbox')

crs=$(pdal info --metadata $2 --readers.copc.resolution=10 | jq -c '.metadata.srs.json.components[0].id.code')

# CRS fallback
if [ -z "$crs" ] || [ "$crs" = "null" ]; then
    crs=$(pdal info --metadata $2 --readers.copc.resolution=10 | jq -c '.metadata.srs.json.id.code')
fi

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
   $2

silvimetric -d database.tdb extract -o $3

rm -rf database.tdb
