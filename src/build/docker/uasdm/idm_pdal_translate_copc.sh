#!/bin/bash

# This file exists as a wrapper around 'pdal translate' because PDAL translate was found to drop the SRS information when converting to COPC in older SRS formats.

# The high-level logic here is that PDAL is capable of successfully reading these older formats, but during copc translation it needs to be specified explicitly what the format is. 
# This gives us a two step process for copc translation: 1. read the SRS information;  2. Do pdal translate and include the SRS 

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
if [ -z "$3" ]; then
    echo "The specified output file parameter '$3' was not set."
    exit 1
fi

## initialize conda ##
source $1
conda activate silvimetric

## Read the SRS from the input pointcloud
srs=$(pdal info --metadata $2 | jq -r '.metadata.srs.json.wkt // 
    .metadata.srs.wkt // 
    .metadata.srs.prettywkt // 
    .metadata.srs.compoundwkt // 
    .metadata.srs.prettycompoundwkt // 
    empty')
    
# Display nice error messages if we couldn't calculate an SRS
if [ -z "$srs" ] || [ "$srs" = "null" ]; then
	echo "Error: Could not determine SRS (Spatial Reference System) for supplied pointcloud." >&2
	exit 1
fi

## Do the COPC translation, and include the SRS, potentially rewriting the header into a more modern SRS format.
pdal translate -i $2 -o $3 -r readers.las -w writers.copc --overwrite --writers.copc.a_srs "$srs"
