# Database creation script for StreamFlow
# Create the StreamFlow database along with 
# a "streamflow" user that has access to it
CREATE DATABASE streamflow;
CREATE USER 'streamflow'@'localhost' IDENTIFIED BY 'streamflow';
GRANT ALL PRIVILEGES ON streamflow.* TO 'streamflow'@'localhost' WITH GRANT OPTION;
CREATE USER 'streamflow'@'%' IDENTIFIED BY 'streamflow';
GRANT ALL PRIVILEGES ON streamflow.* TO 'streamflow'@'%' WITH GRANT OPTION;
