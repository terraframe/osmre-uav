FROM ubuntu:bionic

SHELL ["/bin/bash", "-c"]

# Install system dependencies
RUN apt update \
  && apt install git git-lfs libzbar0 bzip2 ca-certificates libglib2.0-0 libxext6 libsm6 libxrender1 libgl1-mesa-glx make wget gcc -y

# Install exiftool
RUN mkdir ~/program \
  && mkdir ~/program/exiftool \
  && cd ~/program/exiftool \
  && wget https://www.sno.phy.queensu.ca/~phil/exiftool/Image-ExifTool-10.98.tar.gz \
  && tar -xvzf Image-ExifTool-10.98.tar.gz \
  && cd ~/program/exiftool/Image-ExifTool-10.98/ \
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
RUN mkdir /root/program/exiftool/Image-ExifTool-10.98/bin && cp /root/program/exiftool/Image-ExifTool-10.98/exiftool /root/program/exiftool/Image-ExifTool-10.98/bin
ENV PATH="/root/program/exiftool/Image-ExifTool-10.98/bin:${PATH}"
RUN chmod +x /root/program/exiftool/Image-ExifTool-10.98/bin/exiftool
ENV exiftoolpath="/root/program/exiftool/Image-ExifTool-10.98/bin/exiftool"

# Run the mikasense test code
RUN source /root/.bashrc && source activate micasense && pytest .

# By default, when they run this docker image, run our processing python script.
CMD source activate micasense && python ./batch.py
