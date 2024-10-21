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
     def diff(arr: np.ndarray):
          return np.max(arr) - np.min(arr)
     def veg_density(arr: np.ndarray):
          if (len(arr) == 0): return 0
          return float(len([x for x in arr if int(x) >= 3 and int(x) <= 5])) / float(len(arr))
     m_diff = Metric('diff', np.float32, diff)
     m_veg_density = Metric('veg_density', np.float32, veg_density)
     return [m_diff, m_veg_density, Metrics["min"], Metrics["max"]]
EOF

bounds=$(pdal info $2 --readers.copc.resolution=1 | jq -c '.stats.bbox.native.bbox')

crs=$(pdal info --metadata $2 --readers.copc.resolution=10 | jq -c '.metadata.srs.json.components[0].id.code')

# CRS fallback
if [ -z "$crs" ] || [ "$crs" = "null" ]; then
    crs=$(pdal info --metadata $2 --readers.copc.resolution=10 | jq -c '.metadata.srs.json.id.code')
fi

#rm -rf database.tdb
#silvimetric --database database.tdb \
#    initialize \
#    --bounds $bounds \
#    --crs $crs \
#    --metrics "silvimetric_metrics.py" \
#    -a "Z" -a "Classification"

#silvimetric -d database.tdb \
#   --threads 2 \
#   --workers 1 \
#   shatter \
#   --date 2008-12-01 \
#   $2

#silvimetric -d database.tdb extract -o $3

#rm -rf database.tdb


## BEGIN Ferry pipeline hacks ##
# Necessary because of https://github.com/hobuinc/silvimetric/issues/102

cat > ferry.pipeline<< EOF
{
  "pipeline": [
    {
      "type": "readers.copc",
      "filename": "$2"
    },
    {
      "type": "filters.ferry",
      "dimensions": "Classification=>UserData"
    },
    {
      "type": "writers.copc",
      "filename": "ferry.copc.laz"
    }
  ]
}
EOF

pdal pipeline ferry.pipeline

rm -rf database.tdb
silvimetric --database database.tdb \
    initialize \
    --bounds $bounds \
    --crs $crs \
    --metrics "silvimetric_metrics.py" \
    -a "Z" -a "UserData"

silvimetric -d database.tdb \
   --threads 2 \
   --workers 1 \
   shatter \
   --date 2008-12-01 \
   ferry.copc.laz

silvimetric -d database.tdb extract -o $3

rm -rf database.tdb

mv $3/m_UserData_veg_density.tif $3/m_Classification_veg_density.tif

rm ferry.copc.laz
