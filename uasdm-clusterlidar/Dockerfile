FROM ubuntu:18.04

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

COPY . /opt/uasdm-clusterlidar

WORKDIR /opt/uasdm-clusterlidar/silvimetric

RUN conda env create \
    -f https://raw.githubusercontent.com/hobuinc/silvimetric/main/environment.yml  \
    -n silvimetric

SHELL ["conda", "run", "-n", "silvimetric", "/bin/bash", "-c"]

WORKDIR /opt/uasdm-clusterlidar/silvimetric

RUN pip install .

RUN conda install -c conda-forge dask-cloudprovider

RUN silvimetric -d autzen initialize --bounds '{"maxx":639003.73,"maxy":853536.21,"maxz":615.26,"minx":635579.2,"miny":848884.83,"minz":406.46}' --crs EPSG:2992

ENTRYPOINT ["conda", "run", "-n", "silvimetric", "/bin/bash", "-c", "silvimetric", "-d", "autzen", "--scheduler", "distributed", "--dasktype", "ec2", "--threads", "4", "--workers", "4", "--watch", "shatter", "--date", "2018-12-01","autzen.copc.lazvim"]
