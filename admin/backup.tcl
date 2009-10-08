# Create backup
# An export of the current application database will be exported
# to /backup. After running this, copy that file to your backup
# system and then remove it.

source "connect.tcl"
puts [jmx_invoke -m StreamFlow:name=Manager backup]
exit
