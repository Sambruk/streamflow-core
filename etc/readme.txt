Deployment of the latest snapshot of the streamflow-war artifact is done
by following this procedure:

Preparations
------------
0. Get VPN access to the Jayway internal network.
1. Create a local folder ~/.ec2/projects/streamflow
2. Get the root private key for the Amazon EC2 deployment server.
3. Place the contents of the private key in the file:
   ~/.ec2/projects/streamflow/id-eu-streamflow-keypair
4. Make sure the permissions of the key file are 600 (owner read-only).
5. Copy or link the files in this directory to ~/.ec2/projects/streamflow
6. Generate an SSH key with:
   ssh-keygen -t dsa
7. Place the content of the newly generated ~/.ssh/id_dsa.pub in
   192.168.0.146:~wayuser/.ssh/authorized_keys

Verify preparations
-------------------
1. Connect to VPN and perform this command:
   ssh wayuser@192.168.0.146 date
   You should get a date with no password being asked for.
2. Perform a similar command against EC2:
   ssh -i id-eu-streamflow-keypair root@streamflow.doesntexist.com date
   Again, you should get a date with no password being asked for.

Deployment
----------
1. cd ~/.ec2/projects/streamflow
2. Connect to VPN.
3. sh deploy-war.sh

The latest snapshot of the war will be copied to a local file, then copied to
EC2, and deployed to Tomcat. The deployment will end with a 'tail -f' being
issued on the Tomcat log file. Test the war by going to the URL:

http://streamflow.doesntexist.com/streamflow

Download the client and test it. If everything looks fine, you can interrupt
the 'tail -f' command with Ctrl-C.
