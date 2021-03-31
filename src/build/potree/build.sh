#!/bin/bash

set -ex

POTREE_VERSION=1.8

UASDM=../../..
POTREE_WEBAPP=$UASDM/uasdm-web/src/main/webapp/WEB-INF/gov/osmre/uasdm/potree

[ -d $POTREE_WEBAPP/potree ] && rm -rf $POTREE_WEBAPP/potree
mkdir $POTREE_WEBAPP/potree

[ -d ./target ] && rm -rf ./target
mkdir ./target

wget https://github.com/potree/potree/releases/download/$POTREE_VERSION/Potree_$POTREE_VERSION.zip -O ./target/potree.zip

unzip ./target/potree.zip -d $POTREE_WEBAPP/potree
mv $POTREE_WEBAPP/potree/Potree_$POTREE_VERSION/* $POTREE_WEBAPP/potree
rm -rf $POTREE_WEBAPP/potree/Potree_$POTREE_VERSION
