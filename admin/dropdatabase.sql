# Database removal script for StreamFlow
# This script will drop any existing StreamFlow databases and the "streamflow" user
DROP DATABASE streamflow;
DROP USER 'streamflow'@'localhost';
DROP USER 'streamflow'@'%';
