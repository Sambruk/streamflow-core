# Restore from backup
# Place the database backup file (e.g. streamflow_data_*.jzon.gz) into
# the /backup directory before running this

source "connect.tcl"
puts [jmx_invoke -m StreamFlow:name=Manager restore]
exit
