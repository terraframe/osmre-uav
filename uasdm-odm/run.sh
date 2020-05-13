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


sudo docker rm -f $(docker ps -a -q --filter="name=uasdm-nodeodm") || true
sudo docker run -d -p 3000:3000 -v /opt/odm-micasense-temp:/opt/micasense -e MICASENSE_HOST_BINDING=/opt/odm-micasense-temp -v /usr/bin/docker:/usr/bin/docker -v /var/run/docker.sock:/var/run/docker.sock --name uasdm-nodeodm uasdm-nodeodm

echo "The server should be running at http://localhost:3000/"
