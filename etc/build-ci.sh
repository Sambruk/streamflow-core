#!/bin/sh

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
