# What is this project?

The primary source this project contains is a Dockerfile and also a batch.py. The rest of the files are either notes or tutorials copied/pasted directly from the Micasense project. Batch.py is the primary entrypoint of the Dockerfile and is the primary entrypoint for running Micasense. The easiest way to get started with this project is to build and run the Dockerfile (because it takes care of all the environment setup for you), however I have included the direct python instructions for completeness.


# How to run the Dockerfile

1. If you haven't put data into the out directory yet, do so now. Look at the batch.py instructions for more info.
2. Open a terminal
3. cd to this project
4. Run `sudo ./build.sh`
5. Run `sudo ./test.sh`


# How to run batch.py

Batch.py can be run directly from python, and it processes a directory of "input" and converts it into a directory of "output."

Follow the directions here when building your input directory. This directory is essentially just the unzipped zip that you would upload to UASDM.
https://terraframe.atlassian.net/wiki/spaces/OSMRE/pages/920945005/Multispectrail+Importable+Specification

Next, follow the Micasense documentation to get your python environment set up:
https://micasense.github.io/imageprocessing/MicaSense%20Image%20Processing%20Setup.html

Finally, set these environment variables in your shell:

```
export MICASENSE_IN=$(pwd)/in
export MICASENSE_OUT=$(pwd)/out
```

and then create a symbolic link in this directory to the Micasense source

```
ln -s $(pwd)/micasense ./micasense
```

(After a `conda activate micasense`) Run with:
```
python batch.py
```
