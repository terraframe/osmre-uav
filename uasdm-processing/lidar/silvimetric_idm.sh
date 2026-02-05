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
          return float(len([x for x in arr if int(x) >= 4 and int(x) <= 5])) / float(len(arr))
     m_diff = Metric('diff', np.float32, diff)
     m_veg_density = Metric('veg_density', np.float32, veg_density)
     return [m_diff, m_veg_density, Metrics["min"], Metrics["max"]]
EOF


## Ferry pipeline hack ##
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

bounds=$(pdal info $2 --readers.copc.resolution=1 | jq -c '.stats.bbox.native.bbox')

## Read the SRS from the input pointcloud
crs=$(pdal info --metadata $2 | jq -r '.metadata.srs.json.wkt // 
    .metadata.srs.wkt // 
    .metadata.srs.prettywkt // 
    .metadata.srs.compoundwkt // 
    .metadata.srs.prettycompoundwkt // 
    empty')

# Display nice error messages if we couldn't calculate bounds or CRS
if [ -z "$crs" ] || [ "$crs" = "null" ]; then
	echo "Error: Could not determine CRS (Coordinate Reference System) for supplied pointcloud." >&2
	exit 1
fi
if [ -z "$bounds" ] || [ "$bounds" = "null" ]; then
	echo "Error: Could not determine bounds for supplied pointcloud." >&2
	exit 1
fi

rm -rf database.tdb
silvimetric --database database.tdb \
    initialize \
    --bounds $bounds \
    --crs "$crs" \
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
