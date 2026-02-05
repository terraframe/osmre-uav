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

FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive
ENV CONDA_DIR=/opt/conda
ENV PATH=$CONDA_DIR/bin:$PATH
ENV PROJ_DATA=/opt/conda/envs/silvimetric/share/proj

# Base system tooling
RUN apt-get update && apt-get -y install --no-install-recommends \
        ca-certificates \
        curl \
        unzip \
        bash \
        jq \
        libxml2 \
    && rm -rf /var/lib/apt/lists/*

# AWS CLI v2
RUN curl -sS "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip \
    && unzip -q /tmp/awscliv2.zip -d /tmp \
    && /tmp/aws/install \
    && rm -rf /tmp/aws /tmp/awscliv2.zip

### Install Conda from miniforge ###
## Commands taken from official conda-forge Dockerfile at:
## https://github.com/conda-forge/miniforge-images/blob/master/ubuntu/Dockerfile
ARG MINIFORGE_NAME=Miniforge3
ARG MINIFORGE_VERSION=24.7.1-2
ARG TARGETPLATFORM
ENV CONDA_DIR=/opt/conda
ENV LANG=C.UTF-8 LC_ALL=C.UTF-8
ENV PATH=${CONDA_DIR}/bin:${PATH}
RUN apt-get update > /dev/null && \
    apt-get install --no-install-recommends --yes \
        wget bzip2 ca-certificates \
        git \
        tini \
        > /dev/null && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    wget --no-hsts --quiet https://github.com/conda-forge/miniforge/releases/download/${MINIFORGE_VERSION}/${MINIFORGE_NAME}-${MINIFORGE_VERSION}-Linux-$(uname -m).sh -O /tmp/miniforge.sh && \
    /bin/bash /tmp/miniforge.sh -b -p ${CONDA_DIR} && \
    rm /tmp/miniforge.sh && \
    conda clean --tarballs --index-cache --packages --yes && \
    find ${CONDA_DIR} -follow -type f -name '*.a' -delete && \
    find ${CONDA_DIR} -follow -type f -name '*.pyc' -delete && \
    conda clean --force-pkgs-dirs --all --yes  && \
    echo ". ${CONDA_DIR}/etc/profile.d/conda.sh && conda activate base" >> /etc/skel/.bashrc && \
    echo ". ${CONDA_DIR}/etc/profile.d/conda.sh && conda activate base" >> ~/.bashrc

# Make sure conda's shared libs (sqlite, etc.) are preferred over system libs
ENV LD_LIBRARY_PATH=${CONDA_DIR}/lib

# Ensure libmamba + sqlite in base are consistent and usable
RUN conda install -n base -y -c conda-forge \
      "sqlite>=3.45" \
      conda-libmamba-solver libmamba \
 && conda config --set solver libmamba

# Install GDAL
RUN conda install -c conda-forge gdal proj proj-data

# Install silvimetric env
WORKDIR /opt/lidar
RUN conda env create \
    -f https://raw.githubusercontent.com/hobuinc/silvimetric/main/environment.yml  \
    -n silvimetric
COPY silvimetric_idm.sh /opt/lidar/silvimetric_idm.sh
RUN chmod +x /opt/lidar/silvimetric_idm.sh
COPY dask_distributed.yml /root/.config/dask/distributed.yaml

RUN conda run -n silvimetric pip install "pandas>=1.0,<3.0"

# Include additional scripts
COPY idm_pdal_translate_copc.sh /opt/lidar/idm_pdal_translate_copc.sh

# Entrypoint wrapper that does S3 download -> run -> upload
COPY run.sh /opt/lidar/run.sh
RUN chmod +x /opt/lidar/run.sh

WORKDIR /work
ENTRYPOINT ["/opt/lidar/run.sh"]
