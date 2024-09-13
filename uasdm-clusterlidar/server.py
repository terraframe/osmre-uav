import dask.config
from dask_cloudprovider.aws import EC2Cluster
from dask.distributed import Client

cluster = EC2Cluster(
    # Cluster manager specific config kwargs
    
)

client = Client(cluster)

cluster.close()

