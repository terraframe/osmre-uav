#!/bin/bash

set -ex

POTREE_VERSION=1.8

UASDM=..
POTREE_VIEWER=$UASDM/uasdm-web/src/main/webapp/potree-viewer

[ -d $POTREE_VIEWER/potree ] && rm -rf $POTREE_VIEWER/potree
mkdir $POTREE_VIEWER/potree

[ -d $UASDM/potree/target ] && rm -rf $UASDM/potree/target
mkdir $UASDM/potree/target

wget https://github.com/potree/potree/releases/download/$POTREE_VERSION/Potree_$POTREE_VERSION.zip -O $UASDM/potree/target/potree.zip

unzip $UASDM/potree/target/potree.zip -d $POTREE_VIEWER/potree
mv $POTREE_VIEWER/potree/Potree_$POTREE_VERSION/* $POTREE_VIEWER/potree
rm -rf $POTREE_VIEWER/potree/Potree_$POTREE_VERSION
