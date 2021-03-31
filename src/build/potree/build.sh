#!/bin/bash
#
# Copyright 2020 The Department of Interior
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


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
