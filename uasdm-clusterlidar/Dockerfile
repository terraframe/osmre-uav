FROM daskdev/dask@sha256:6a66c4a42ed84bb2fa4866876a7b59f8e355d434e63682d2ccb0295938fbccbd

ENV PATH="/root/miniconda3/bin:${PATH}"
ARG PATH="/root/miniconda3/bin:${PATH}"

# Install wget to fetch Miniconda
RUN apt-get update && \
    apt-get install -y wget && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Miniconda on x86 or ARM platforms
RUN arch=$(uname -m) && \
    if [ "$arch" = "x86_64" ]; then \
    MINICONDA_URL="https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-x86_64.sh"; \
    elif [ "$arch" = "aarch64" ]; then \
    MINICONDA_URL="https://repo.anaconda.com/miniconda/Miniconda3-latest-Linux-aarch64.sh"; \
    else \
    echo "Unsupported architecture: $arch"; \
    exit 1; \
    fi && \
    wget $MINICONDA_URL -O miniconda.sh && \
    mkdir -p /root/.conda && \
    bash miniconda.sh -b -p /root/miniconda3 && \
    rm -f miniconda.sh

RUN conda --version

WORKDIR /opt/uasdm-clusterlidar

COPY environment.yml /opt/uasdm-clusterlidar/environment.yml

RUN conda env create \
     -f environment.yml  \
     -n silvimetric

SHELL ["conda", "run", "-n", "silvimetric", "/bin/bash", "-c"]

RUN conda install -c conda-forge dask-cloudprovider
RUN conda install -c conda-forge boto3
RUN conda install -c conda-forge silvimetric

##RUN pip install dask-cloudprovider boto3 silvimetric PDAL tiledb numpy shapely dask pyproj GDAL scipy awscli websocket-client python-json-logger dill pandas lmoments3

COPY . /opt/uasdm-clusterlidar

##RUN silvimetric -d autzen initialize --bounds '{"maxx":639003.73,"maxy":853536.21,"maxz":615.26,"minx":635579.2,"miny":848884.83,"minz":406.46}' --crs EPSG:2992

##ENTRYPOINT ["conda", "run", "-n", "silvimetric", "/bin/bash", "-c", "silvimetric", "-d", "autzen", "--scheduler", "distributed", "--dasktype", "ec2", "--threads", "4", "--workers", "4", "--watch", "shatter", "--date", "2018-12-01","autzen.copc.lazvim"]

#ENTRYPOINT ["conda", "run", "-n", "silvimetric", "/bin/bash", "-c"]

#CMD ["python autoscale.py"]

##ENTRYPOINT ["python", "autoscale.py"]

ENTRYPOINT ["tini", "-g", "--", "/usr/bin/prepare.sh"]
