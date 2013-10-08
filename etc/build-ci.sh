#!/bin/sh
#
#
# Copyright 2009-2012 Jayway Products AB
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


cd webclient

node_version=0.10.5

if [ ! -e nodejs-plugin-$node_version.zip ]
then
    wget https://s3.amazonaws.com/clickstacks/admin/nodejs-plugin-$node_version.zip
    unzip nodejs-plugin-$node_version.zip
    tar xf node.tar.gz
    mv node-v* node_lib


rm -rf target
mkdir target

export PATH=$PATH:$WORKSPACE/webclient/node_lib/bin

npm install
npm install grunt
npm install grunt-cli
npm install phantomjs

export PATH=$WORKSPACE/webclient/node_modules/grunt-cli/bin/:$WORKSPACE/webclient/node_modules/bower/bin:$WORKSPACE/webclient/node_modules/phantomjs/bin:$PATH

bower update && bower install

else
  export PATH=$WORKSPACE/webclient/node_modules/grunt-cli/bin/:$WORKSPACE/webclient/node_modules/bower/bin:$WORKSPACE/webclient/node_modules/phantomjs/bin:$PATH:$WORKSPACE/webclient/node_lib/bin

fi
cd ..

grunt -version
