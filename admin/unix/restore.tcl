#
#
# Copyright 2009-2012 Streamsource AB
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Restore from backup
# Place the database backup file (e.g. streamflow_data_*.jzon.gz) into
# the /backup directory before running this

source "connect.tcl"
puts [jmx_invoke -m Qi4j:application=StreamflowServer,name=Manager restore]
exit