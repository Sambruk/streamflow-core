#!/bin/sh

war=`sh list-war.sh`
if [ $? -ne 0 ]; then
    echo "Problems listing available wars. Exiting."
    exit 1
fi
base=`basename $war` && 

echo "Retrieving latest war from Nexus repository: $base"
if [ ! -f "$base" ]; then
    scp wayuser@192.168.0.146:$war .
else
    echo "$base already exists"
fi

echo "Copying $base to EC2 instance..."
scp -i id-eu-streamflow-keypair $base root@`cat dns.txt`:
echo "Deploying $base to Tomcat on EC2 instance..."
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` /etc/init.d/tomcat stop
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` rm -rf /usr/local/tomcat/webapps/streamflow
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` rm -rf /usr/local/tomcat/work/Catalina/localhost/streamflow
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` cp $base /usr/local/tomcat/webapps/streamflow.war
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` /etc/init.d/tomcat start
ssh -i id-eu-streamflow-keypair root@`cat dns.txt` tail -f /var/log/tomcat/catalina.out
