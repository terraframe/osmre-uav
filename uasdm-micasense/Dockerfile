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

FROM ubuntu:bionic

ARG exiftool_version=12.25

SHELL ["/bin/bash", "-c"]

# Install system dependencies
RUN apt update \
  && apt install git git-lfs libzbar0 bzip2 ca-certificates libglib2.0-0 libxext6 libsm6 libxrender1 libgl1-mesa-glx make wget gcc -y

# Install exiftool
RUN mkdir ~/program \
  && mkdir ~/program/exiftool \
  && cd ~/program/exiftool \
  && wget https://exiftool.org/Image-ExifTool-$exiftool_version.tar.gz \
  && tar -xvzf Image-ExifTool-$exiftool_version.tar.gz \
  && cd ~/program/exiftool/Image-ExifTool-$exiftool_version/ \
  && perl Makefile.PL \
  && make test \
  && make install
  
# Install Miniconda3
RUN wget --quiet https://repo.anaconda.com/miniconda/Miniconda3-4.7.12-Linux-x86_64.sh -O ~/miniconda.sh && \
    /bin/bash ~/miniconda.sh -b -p /opt/conda && \
    rm ~/miniconda.sh && \
    /opt/conda/bin/conda clean -tipsy && \
    ln -s /opt/conda/etc/profile.d/conda.sh /etc/profile.d/conda.sh && \
    echo ". /opt/conda/etc/profile.d/conda.sh" >> ~/.bashrc && \
    echo "conda activate base" >> ~/.bashrc && \
    find /opt/conda/ -follow -type f -name '*.a' -delete && \
    find /opt/conda/ -follow -type f -name '*.js.map' -delete && \
    /opt/conda/bin/conda clean -afy

# Fetch the micasense_conda_env
RUN mkdir /root/imageprocessing \
  && cd /root/imageprocessing \
  && wget https://raw.githubusercontent.com/micasense/imageprocessing/master/micasense_conda_env.yml -O micasense_conda_env.yml

WORKDIR /root/imageprocessing

# Install Mikasense python dependencies
RUN /opt/conda/bin/conda env create -f micasense_conda_env.yml

# Copy our source over
COPY batch.py /root/imageprocessing/batch.py
COPY imageprocessing /root/imageprocessing/

# Add conda and exiftool to the shell PATH
ENV PATH="/opt/conda/bin:${PATH}"
RUN mkdir /root/program/exiftool/Image-ExifTool-$exiftool_version/bin && cp /root/program/exiftool/Image-ExifTool-$exiftool_version/exiftool /root/program/exiftool/Image-ExifTool-$exiftool_version/bin
ENV PATH="/root/program/exiftool/Image-ExifTool-$exiftool_version/bin:${PATH}"
RUN chmod +x /root/program/exiftool/Image-ExifTool-$exiftool_version/bin/exiftool
ENV exiftoolpath="/root/program/exiftool/Image-ExifTool-$exiftool_version/bin/exiftool"

# Run the mikasense test code
RUN source /root/.bashrc && source activate micasense && pytest .

# By default, when they run this docker image, run our processing python script.
CMD source activate micasense && python ./batch.py
