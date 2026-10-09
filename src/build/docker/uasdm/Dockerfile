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

FROM tomcat:11.0-jdk17

ENV CGR_VERSION=LATEST
ENV LOG_LEVEL=error
ENV POSTGRES_PORT=5432
ENV PIP_ROOT_USER_ACTION=ignore

# Set environment variables to avoid prompts and for Miniconda installation
ENV DEBIAN_FRONTEND=noninteractive
ENV CONDA_DIR /opt/conda
ENV PATH=$CONDA_DIR/bin:$PATH

RUN mkdir -p $CATALINA_HOME/wars

# Install exiftool
RUN apt-get update && apt-get -y install exiftool 

# Install GDAL and COG Tiff validator
#RUN apt-get update && apt-get -y install libgdal-dev gdal-bin python3 pip wget
#RUN pip install --global-option=build_ext --global-option="-I/usr/include/gdal" GDAL=="$(gdal-config --version)"
RUN wget https://raw.githubusercontent.com/OSGeo/gdal/master/swig/python/gdal-utils/osgeo_utils/samples/validate_cloud_optimized_geotiff.py -O validate_cloud_optimized_geotiff.py

RUN apt-get update && apt-get -y install python3 pip wget

# INSTALL PotreeConverter (build deps are installed and purged in the same layer)
ARG BUILD_DEPS="git cmake build-essential g++-14 gcc-14 libboost-all-dev libtbb-dev"
ARG POTREE_CONVERTER_VERSION="2.1.3"

RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ${BUILD_DEPS} ca-certificates; \
    git clone --depth 1 --branch ${POTREE_CONVERTER_VERSION} https://github.com/potree/PotreeConverter.git /opt/PotreeConverter; \
    mkdir -p /opt/PotreeConverter/build; \
    cd /opt/PotreeConverter/build; \
    CC=gcc-14 CXX=g++-14 cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_POLICY_VERSION_MINIMUM=3.5 ..; \
    make -j"$(nproc)"; \
    # keep the runtime libraries the binary links to (e.g. libtbb12) from being autoremoved
    ldd /opt/PotreeConverter/build/PotreeConverter \
      | awk '/=>/ { print $3 }' \
      | xargs -r realpath \
      | xargs -r dpkg-query -S 2>/dev/null \
      | cut -d: -f1 | sort -u \
      | xargs -r apt-mark manual; \
    # remove build-only artifacts
    rm -rf /opt/PotreeConverter/.git /opt/PotreeConverter/build/CMakeFiles; \
    apt-get purge -y --auto-remove ${BUILD_DEPS}; \
    rm -rf /var/lib/apt/lists/*

WORKDIR /opt/PotreeConverter/build


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
ENV LD_LIBRARY_PATH=${CONDA_DIR}/lib:${LD_LIBRARY_PATH}

# Ensure libmamba + sqlite in base are consistent and usable
RUN conda install -n base -y -c conda-forge \
      "sqlite>=3.45" \
      conda-libmamba-solver libmamba \
 && conda config --set solver libmamba


# Install GDAL
RUN conda install -c conda-forge gdal proj proj-data


# Install silvimetric
WORKDIR /opt/silvimetric
RUN conda env create \
    -f https://raw.githubusercontent.com/hobuinc/silvimetric/main/environment.yml  \
    -n silvimetric
RUN apt-get update && apt-get install -y jq
COPY silvimetric_idm.sh /opt/silvimetric/silvimetric_idm.sh
RUN chmod +x /opt/silvimetric/silvimetric_idm.sh
COPY dask_distributed.yml /root/.config/dask/distributed.yaml

# Install IDM special tooling
COPY idm_pdal_translate_copc.sh /opt/idm/idm_pdal_translate_copc.sh

#Revert the working directory
WORKDIR /usr/local/tomcat

# Copy CGR WAR
COPY target/uasdm.war $CATALINA_HOME/wars/geoprism.war

# Deploy CGR War
RUN mkdir -p $CATALINA_HOME/wars/geoprism && cd $CATALINA_HOME/wars/geoprism && $JAVA_HOME/bin/jar xf $CATALINA_HOME/wars/geoprism.war
RUN mv $CATALINA_HOME/wars/geoprism $CATALINA_HOME/webapps/ROOT

# Log4j properties
RUN wget -nv -O $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml https://raw.githubusercontent.com/terraframe/geoprism-cloud/dev/ansible/roles/webserver/files/log4j2.xml
RUN sed -i -e 's/<Root level="error">/<Root level="$LOG_LEVEL">/g' $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2.xml

# Pre-load with a default appcfg
COPY target/appcfg $CATALINA_HOME/appcfg

RUN wget -nv -O $CATALINA_HOME/bin/wait-for-it.sh https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh
RUN chmod +x $CATALINA_HOME/bin/wait-for-it.sh

#CMD ["$CATALINA_HOME/bin/wait-for-it.sh", "-t", 0, "database:$POSTGRES_PORT", "--"]
