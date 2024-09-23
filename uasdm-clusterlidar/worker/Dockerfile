FROM daskdev/dask@sha256:6a66c4a42ed84bb2fa4866876a7b59f8e355d434e63682d2ccb0295938fbccbd

RUN mamba install -c conda-forge boto3
RUN mamba install -c conda-forge silvimetric

ENTRYPOINT ["tini", "-g", "--", "/usr/bin/prepare.sh"]
