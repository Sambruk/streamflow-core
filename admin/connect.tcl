# Connect to a remote StreamFlow instance using JMX-RMI
# Change the host, port and login info to match your environment
package require java
jmx_connect -h localhost -p 1099 -U administrator -P administrator
