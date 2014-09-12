#!/bin/sh

wget http://repo2.maven.org/maven2/org/mortbay/jetty/jetty-runner/8.1.8.v20121106/jetty-runner-8.1.8.v20121106.jar

ln -s ../../web/target/streamflow-web-1.9.0-SNAPSHOT.war streamflow-web.war
ln -s jetty-runner-8.1.8.v20121106.jar jetty-runner.jar
