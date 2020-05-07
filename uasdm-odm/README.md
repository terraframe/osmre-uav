# What is this project?

This project is a forked version of ODM v0.9.1 which adds support for Micasense. Micasense is run in a sibling Docker container alongside ODM. This means the command for running ODM must be different to allow for spawning sibling docker containers within a docker container.

The command to run this project is as follows:

`docker run -d -p 3000:3000 -v /var/www/data:/var/www/data -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock --name uasdm-odm uasdm-odm`

and requires pulling the uasdm-odm container from our private AWS ECR. You may also build it locally with the instructions below.


# How can I build this project?

Upon a fresh checkout of this project, simply open a terminal, cd to this directory, and run init.sh. This script will fetch ODM and NodeODM automatically for you.

To build, simply run build.sh. This script will generate new docker images with the updated source code.

I have noticed that the ODM Docker image does not build correctly on my Mac, however I have had good success on Ubuntu bionic.

