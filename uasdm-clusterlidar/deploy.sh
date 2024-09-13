#!/bin/bash

cd silvimetric && pip install . && cd ..

silvimetric -d autzen --scheduler distributed --dasktype ec2 --threads 4 --workers 4 --watch shatter --date 2018-12-01 autzen.copc.laz


