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

FROM daskdev/dask@sha256:6a66c4a42ed84bb2fa4866876a7b59f8e355d434e63682d2ccb0295938fbccbd

WORKDIR /opt/uasdm-clusterlidar

COPY environment.yml /opt/uasdm-clusterlidar/environment.yml

RUN mamba create -c conda-forge \
     -n silvimetric && mamba env update -n silvimetric --file environment.yml

SHELL ["mamba", "run", "-n", "silvimetric", "/bin/bash", "-c"]

RUN mamba install -c conda-forge dask-cloudprovider
RUN mamba install -c conda-forge boto3
RUN mamba install -c conda-forge silvimetric

RUN mamba install python=3.10.12
RUN mamba install toolz=0.12.0

##RUN pip install dask-cloudprovider boto3 silvimetric PDAL tiledb numpy shapely dask pyproj GDAL scipy awscli websocket-client python-json-logger dill pandas lmoments3

COPY . /opt/uasdm-clusterlidar

##RUN silvimetric -d autzen initialize --bounds '{"maxx":639003.73,"maxy":853536.21,"maxz":615.26,"minx":635579.2,"miny":848884.83,"minz":406.46}' --crs EPSG:2992

##ENTRYPOINT ["conda", "run", "-n", "silvimetric", "/bin/bash", "-c", "silvimetric", "-d", "autzen", "--scheduler", "distributed", "--dasktype", "ec2", "--threads", "4", "--workers", "4", "--watch", "shatter", "--date", "2018-12-01","autzen.copc.lazvim"]

ENTRYPOINT ["mamba", "run", "-n", "silvimetric", "/bin/bash", "-c"]

CMD ["python autoscale.py"]
