# What is this project?

This project is a forked version of ODM v0.9.1 which adds support for Micasense. Micasense is run in a sibling Docker container alongside ODM. This means the command for running ODM must be different to allow for spawning sibling docker containers within a docker container.

The command to run this project is as follows:

`docker run -d -p 3000:3000 -v /var/www/data:/var/www/data -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock --name uasdm-odm uasdm-odm`

and requires pulling the uasdm-odm container from our private AWS ECR. You may also build it locally with the instructions below.


# How can I build this project?

This project is not a full checkout of ODM, nor does it contain NodeODM. It only contains the source which was actually modified.

Upon a fresh checkout of this project, simply open a terminal, cd to this directory, and run init.sh. This script will fetch ODM and NodeODM automatically for you.

To build, simply run build.sh. This script will replace all modified ODM source and then build the projects. 

I have noticed that the ODM Docker image does not build correctly on my Mac, however I have had good success on Ubuntu bionic.


# What exactly was changed?

The following files were changed:

1. EDIT opendm/config.py

This file was changed to add the new 'multispectral parameter'

```
parser.add_argument('--multispectral',
        action='store_true',
        default=False,
        help='If set to true, ODM will assume the imagery is multispectral and Micasense will be invoked '
        'Default: '
        '%(default)s')
```

2. NEW stages/odm_micasense.py

This new file was added. It contains the core logic for running the Micasense docker container.

3. EDIT odm_app.py

Two major changes were made in this file. The first is adding the new micasense stage to the list of stages and telling ODM to run it first. Second, the initialization procedure of ODM was slightly modified. Some initialization code was pulled out of dataset.py and moved into this file here, because the initialization code needed to be run before the micasense stage.

4. EDIT dataset.py

The previously mentioned initialization code was pulled out of dataset.py and moved into the bottom of odm_app.py
