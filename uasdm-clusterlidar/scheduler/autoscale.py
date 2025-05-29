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


import os
from pathlib import Path
import numpy as np
import pdal
import json
from dask.distributed import Client
import dask.config
from dask_cloudprovider.aws import EC2Cluster
import webbrowser

# from silvimetric.resources import Storage, Metric, Metrics, Bounds, Pdal_Attributes
# from silvimetric.resources import StorageConfig, ShatterConfig, ExtractConfig
# from silvimetric.commands import scan, shatter, extract

from silvimetric.resources.storage import Storage
from silvimetric.resources.metric import Metric, Metrics
from silvimetric.resources.bounds import Bounds
from silvimetric.resources.entry import Pdal_Attributes
from silvimetric.resources.config import StorageConfig, ShatterConfig, ExtractConfig
from silvimetric.commands import scan, shatter, extract

import boto3

########## Setup #############

# Here we create a path for our current working directory, as well as the path
# to our forest data, the path to the database directory, and the path to the
# directory that will house the raster data.
curpath = Path(os.path.dirname(os.path.realpath(__file__)))
filename = "https://s3-us-west-2.amazonaws.com/usgs-lidar-public/MT_RavalliGraniteCusterPowder_4_2019/ept.json"
db_dir_path = Path(curpath  / "western_us.tdb")

db_dir = str(db_dir_path)
out_dir = str(curpath / "westsern_us_tifs")
resolution = 10 # 10 meter resolution

# we'll use PDAL python bindings to find the srs of our data, and the bounds
reader = pdal.Reader(filename)
p = reader.pipeline()
qi = p.quickinfo[reader.type]
bounds = Bounds.from_string((json.dumps(qi['bounds'])))
srs = json.dumps(qi['srs']['json'])

######## Create Metric ########
# Metrics give you the ability to define methods you'd like applied to the data
# Here we define, the name, the data type, and what values we derive from it.

def make_metric():
    def p75(arr: np.ndarray):
        return np.percentile(arr, 75)

    return Metric(name='p75', dtype=np.float32, method = p75)

###### Create Storage #####
# This will create a tiledb database, same as the `initialize` command would
# from the command line. Here we'll define the overarching bounds, which may
# extend beyond the current dataset, as well as the CRS of the data, the list
# of attributes that will be used, as well as metrics. The config will be stored
# in the database for future processes to use.

def db():
    perc_75 = make_metric()
    attrs = [
        Pdal_Attributes[a]
        for a in ['Z', 'NumberOfReturns', 'ReturnNumber', 'Intensity']
    ]
    metrics = [
        Metrics[m]
        for m in ['mean', 'min', 'max']
    ]
    metrics.append(perc_75)
    st_config = StorageConfig(db_dir, bounds, resolution, srs, attrs, metrics)
    storage = Storage.create(st_config)

###### Perform Shatter #####
# The shatter process will pull the config from the database that was previously
# made and will populate information like CRS, Resolution, Attributes, and what
# Metrics to perform from there. This will split the data into cells, perform
# the metric method over each cell, and then output that information to TileDB

def sh():
    sh_config = ShatterConfig(db_dir, filename, tile_size=200)
    with Client(n_workers=10, threads_per_worker=3, timeout=100000) as client:
        webbrowser.open(client.cluster.dashboard_link)
        shatter(sh_config, client)


###### Perform Extract #####
# The Extract step will pull data from the database for each metric/attribute combo
# and store it in an array, where it will be output to a raster with the name
# `m_{Attr}_{Metric}.tif`. By default, each computed metric will be written
# to the output directory, but you can limit this by defining which Metric names
# you would like
def ex():
    ex_config = ExtractConfig(db_dir, out_dir)
    extract(ex_config)

####### Perform Scan #######
# The Scan step will perform a search down the resolution tree of the COPC or
# EPT file you've supplied and will provide a best guess of how many cells per
# tile you should use for this dataset.

def sc():
    scan.scan()

def uploadToS3():
    bucket = "tftest222"
    path_prefix = "/autoscale-test"

    s3c = boto3.client('s3')

    # enumerate local files recursively
    for root, dirs, files in os.walk(out_dir):

        for filename in files:

            # construct the full local path
            local_path = os.path.join(root, filename)

            # construct the full Dropbox path
            relative_path = os.path.relpath(local_path, out_dir)
            s3_path = os.path.join(path_prefix, relative_path)

            # relative_path = os.path.relpath(os.path.join(root, filename))

            print('Searching "%s" in "%s"' % (s3_path, bucket))
            try:
                s3c.head_object(Bucket=bucket, Key=s3_path)
                print("Path found on S3! Skipping " + s3_path + "...")

                # try:
                    # client.delete_object(Bucket=bucket, Key=s3_path)
                # except:
                    # print("Unable to delete %s..." % s3_path)
            except:
                print("Uploading %s..." % s3_path)
                s3c.upload_file(local_path, bucket, s3_path)


def localTasks():
    make_metric()
    db()
    sh()
    ex()
    uploadToS3()

def autoscale():
    cluster = EC2Cluster(
        # Cluster manager specific config kwargs
        security=False,
        docker_image="daskdev/dask:latest",
        env_vars={ "AWS_REGION": "us-west-2", "DEFAULT_AWS_REGION": "us-west-2", "EXTRA_CONDA_PACKAGES": "silvimetric boto3", "USE_MAMBA": "true" },
        instance_type="m4.large"
    )

    client = Client(cluster)

    f = client.submit(localTasks)
    f.result()

    cluster.close()


if __name__ == "__main__":
    autoscale()
