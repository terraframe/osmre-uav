#!/bin/bash
# Do not run with sudo

# Exit immediately if anything errors out
set -e

git lfs clone https://github.com/micasense/imageprocessing.git
cp -rf imageprocessing/micasense ./micasense