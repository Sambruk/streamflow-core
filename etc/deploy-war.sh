#!/bin/sh

war=`sh list-war.sh`
if [ $? -ne 0 ]; then
    echo "Problems listing available wars. Exiting."
    exit 1
fi
base=`basename $war` && 

echo "Retrieving latest war from Nexus repository..."
if [ ! -f "$base" ]; then
    scp wayuser@192.168.0.146:$war .
else
    echo "$base already exists"
fi

echo "Copying to EC2 instance..."
scp -i id-eu-streamflow-keypair $base root@`cat dns.txt`:
echo "Deploying to Tomcat on EC2 instance..."
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` cp $base /usr/local/tomcat/webapps/streamflow.war
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` tail -f /var/log/tomcat/catalina.out
