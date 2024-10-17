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

# Silvimetric versions 1.2.1 and 1.2.3 were found to throw an error when specifying custom metrics
# More info here: https://github.com/hobuinc/silvimetric/issues/101
pip install silvimetric==1.1.1
pip install line-profiler # Silvimetric v1.1.1 requires this (but newer versions don't)

## begin script ##

cat > silvimetric_metrics.py<< EOF
import numpy as np
from silvimetric.resources.metric import Metric, Metrics
def metrics() -> list[Metric]:
     def z_diff(arr: np.ndarray):
          return np.max(arr) - np.min(arr)
     m_z_diff = Metric('diff', np.float32, z_diff)
     return [m_z_diff, Metrics["min"], Metrics["max"]]
EOF

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
    --crs $crs \
    -m "silvimetric_metrics.py"

silvimetric -d database.tdb \
   --threads 2 \
   --workers 1 \
   shatter \
   --date 2008-12-01 \
   $2

silvimetric -d database.tdb extract -o $3

rm -rf database.tdb
