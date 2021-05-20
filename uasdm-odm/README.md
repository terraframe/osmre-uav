# What is this project?

This project is a wrapper around an ODM fork and also a NodeODM fork that TerraFrame is maintaining of the official OpenDroneMap software. The official path for these repositories is as follows:

1. https://github.com/terraframe/ODM
2. https://github.com/terraframe/NodeODM

These forks were primarily created for the purposes of adding support for multispectral processing, using Micasense. At the time this project was created, ODM did not support multispectral processing. Given that ODM currently supports multispectral processing, this project may considered to be legacy (unless for some reason the Micasense output is preferred). Micasense is run in a sibling Docker container alongside ODM. This means the command for running ODM must be different to allow for spawning sibling docker containers within a docker container.

The Micasense codebase can be found here:

https://github.com/micasense/imageprocessing

We also have wrapper projects similar to this in the uasdm-micasense and uasdm-clusterodm sibiling projects.

# Shell Scripts

There are various shell scripts which facilitate various build tasks for deploy and development. These shell scripts have been tested only on Ubuntu. Some are expected to be run as root, and others not (this is usually documented at the top of the script).

The shell scripts may or may not utilize the following environment variables:
- UASDM = /path/to/uasdm/git/project/../
- UASDM_ECR_KEY = credentials to Terraframe's private ECR server
- UASDM_ECR_SECRET = credentials to Terraframe's private ECR server

The purposes of the shell scripts is as follows:
init.sh - Downloads the required repositories and configures them
build.sh - Builds the necessary Docker images from the repositories fetched with init.sh
run.sh - Runs the Docker image built with the build.sh script
deploy.sh - Deploys the Docker image to the Terraframe ECR repository.
