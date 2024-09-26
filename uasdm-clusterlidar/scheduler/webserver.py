from flask import Flask, request, jsonify
from dask.distributed import Client, Future
from dask_ec2 import EC2Cluster
import uuid
import json
import os
import time
import boto3
import zipfile
import shutil
import psutil
import platform
import threading
from functools import partial

app = Flask(__name__)

# Global variables
tasks_file = 'tasks.json'
config_file = 'config.json'
tasks = {}     # In-memory store of taskId to task info mappings
clusters = {}  # In-memory store of cluster_id to cluster and client mappings
config = None  # Configuration data loaded from config.json
tasks_lock = threading.Lock()  # Lock for thread-safe access to tasks

def load_config():
    global config
    if os.path.exists(config_file):
        with open(config_file, 'r') as f:
            config = json.load(f)
        print("Loaded config from config.json")
    else:
        print("Config file not found.")
        config = {"imageSizeMapping": []}

def load_tasks():
    if os.path.exists(tasks_file):
        with open(tasks_file, 'r') as f:
            data = json.load(f)
            for taskId, task_info in data.items():
                dateCreated = task_info['dateCreated']
                cluster_id = task_info['cluster_id']
                cluster_params = task_info['cluster_params']
                scheduler_address = task_info['scheduler_address']
                # Reconstruct client connected to the existing cluster
                try:
                    client = Client(scheduler_address)
                    # Get the future
                    future = client.get_future(taskId)
                    # Attach callback with taskId
                    callback = partial(task_done_callback, taskId=taskId)
                    future.add_done_callback(callback)
                    # Store in tasks and clusters
                    with tasks_lock:
                        tasks[taskId] = {
                            'future': future,
                            'dateCreated': dateCreated,
                            'cluster_id': cluster_id,
                            'cluster_params': cluster_params,
                            'scheduler_address': scheduler_address
                        }
                        clusters[cluster_id] = {
                            'client': client,
                            'cluster': None,  # We don't have the cluster object here
                            'scheduler_address': scheduler_address
                        }
                    print(f"Reconnected to cluster {cluster_id} for task {taskId}")
                except Exception as e:
                    print(f"Failed to reconnect to cluster {cluster_id} for task {taskId}: {e}")
                    with tasks_lock:
                        tasks[taskId] = {
                            'future': None,
                            'dateCreated': dateCreated,
                            'cluster_id': cluster_id,
                            'cluster_params': cluster_params,
                            'scheduler_address': scheduler_address
                        }
        print("Loaded tasks from tasks.json")
    else:
        print("Tasks file not found.")

def save_tasks():
    with tasks_lock:
        data = {}
        for taskId, task_info in tasks.items():
            data[taskId] = {
                'dateCreated': task_info['dateCreated'],
                'cluster_id': task_info['cluster_id'],
                'cluster_params': task_info['cluster_params'],
                'scheduler_address': task_info['scheduler_address']
            }
        with open(tasks_file, 'w') as f:
            json.dump(data, f)
    print("Saved tasks to tasks.json")

def parse_s3_url(s3_url):
    s3_url = s3_url.replace("s3://", "")
    bucket_name, key = s3_url.split('/', 1)
    return bucket_name, key

def process(data_dir):
    # Placeholder for the actual processing logic
    time.sleep(5)  # Simulate processing time
    result_dir = os.path.join(data_dir, 'results')
    os.makedirs(result_dir, exist_ok=True)
    with open(os.path.join(result_dir, 'output.txt'), 'w') as f:
        f.write('Processing complete.')
    return 'Processing complete.'

def worker_task(zip_url):
    # Download zip file from S3
    s3 = boto3.client('s3')
    bucket_name, key = parse_s3_url(zip_url)
    local_zip_path = '/tmp/data_{}.zip'.format(uuid.uuid4())
    s3.download_file(bucket_name, key, local_zip_path)

    # Unzip the file
    data_dir = '/tmp/data_{}'.format(uuid.uuid4())
    with zipfile.ZipFile(local_zip_path, 'r') as zip_ref:
        zip_ref.extractall(data_dir)

    # Process the data
    result = process(data_dir)

    # Upload results to S3
    results_zip_path = '/tmp/results_{}.zip'.format(uuid.uuid4())
    shutil.make_archive(results_zip_path.replace('.zip', ''), 'zip', os.path.join(data_dir, 'results'))
    s3.upload_file(results_zip_path, bucket_name, 'results/{}'.format(os.path.basename(results_zip_path)))

    # Clean up
    os.remove(local_zip_path)
    shutil.rmtree(data_dir)
    os.remove(results_zip_path)

    return result

def select_instance_type(maxImages, maxColSizeMb):
    for mapping in config['imageSizeMapping']:
        if maxImages <= mapping['maxImages'] and maxColSizeMb <= mapping['maxColSizeMb']:
            return mapping
    # If none match, return the largest one
    return config['imageSizeMapping'][-1]

def task_done_callback(future, taskId):
    # Callback to remove task when done
    with tasks_lock:
        task_info = tasks.get(taskId)
        if task_info:
            # Remove task from tasks
            cluster_id = task_info['cluster_id']
            del tasks[taskId]
            # Update tasks.json
            save_tasks()
            print(f"Task {taskId} completed and removed from tasks.")
            # Check if any other tasks are using the cluster
            if not any(t['cluster_id'] == cluster_id for t in tasks.values()):
                # No other tasks are using the cluster; we can close it
                client = clusters[cluster_id]['client']
                client.close()
                print(f"Client for cluster {cluster_id} closed.")
                # Close the cluster if we have it
                cluster = clusters[cluster_id].get('cluster')
                if cluster:
                    cluster.close()
                    print(f"Cluster {cluster_id} closed.")
                del clusters[cluster_id]

def get_task_status(taskId):
    with tasks_lock:
        task_info = tasks.get(taskId)
        if not task_info:
            return {'status': 'NONE'}, 404

        future = task_info['future']
        dateCreated = task_info['dateCreated']
        processingTime = int((time.time() - dateCreated) * 1000)  # in milliseconds

        if future is None:
            status = 'UNKNOWN'
            output = None
        elif future.cancelled():
            status = 'CANCELED'
            output = None
        elif future.done():
            status = 'COMPLETED'
            try:
                output = future.result()
            except Exception as e:
                output = str(e)
        else:
            status = 'RUNNING'
            output = None

        # Get worker logs
        cluster_id = task_info['cluster_id']
        client = clusters[cluster_id]['client']
        worker_logs = client.get_worker_logs()
        output = worker_logs if worker_logs else output

        result = {
            'status': status,
            'dateCreated': dateCreated,
            'processingTime': processingTime if status == 'RUNNING' else -1,
            'output': output
        }
        return result, 200

@app.route('/info', methods=['GET'])
def info():
    data = {
        'software_version': '1.0.0',
        'memory': psutil.virtual_memory().total,
        'cpu_cores': psutil.cpu_count(),
        'platform': platform.platform()
    }
    return jsonify(data)

@app.route('/task/new', methods=['POST'])
def new_task():
    zip_url = request.form.get('zipUrl')
    max_images = request.form.get('maxImages', type=int)
    max_col_size_mb = request.form.get('maxColSizeMb', type=int)

    if not zip_url or max_images is None or max_col_size_mb is None:
        return jsonify({'error': 'zipUrl, maxImages, and maxColSizeMb parameters are required'}), 400

    # Select instance type based on parameters
    mapping = select_instance_type(max_images, max_col_size_mb)
    cluster_params = mapping

    # Generate unique IDs
    taskId = str(uuid.uuid4())
    cluster_id = str(uuid.uuid4())
    dateCreated = time.time()

    # Create EC2Cluster
    cluster = EC2Cluster(
        instance_type=cluster_params['slug'],
        worker_ebs_size=cluster_params['storage']
    )
    client = Client(cluster)
    scheduler_address = cluster.scheduler_address

    # Submit task to Dask
    future = client.submit(worker_task, zip_url)
    # Attach callback with taskId
    callback = partial(task_done_callback, taskId=taskId)
    future.add_done_callback(callback)

    # Store task and cluster info
    with tasks_lock:
        tasks[taskId] = {
            'future': future,
            'dateCreated': dateCreated,
            'cluster_id': cluster_id,
            'cluster_params': cluster_params,
            'scheduler_address': scheduler_address
        }
        clusters[cluster_id] = {
            'client': client,
            'cluster': cluster,
            'scheduler_address': scheduler_address
        }

    save_tasks()

    return jsonify({'taskId': taskId})

@app.route('/status/<taskId>', methods=['GET'])
def task_status(taskId):
    result, status_code = get_task_status(taskId)
    return jsonify(result), status_code

@app.route('/tasks', methods=['GET'])
def list_tasks():
    with tasks_lock:
        task_list = []
        for taskId in tasks.keys():
            result, _ = get_task_status(taskId)
            task_list.append(result)
    return jsonify(task_list)

if __name__ == '__main__':
    load_config()
    load_tasks()
    app.run(host='0.0.0.0', port=5000, threaded=True)
