#
#
# Copyright
# 2009-2015 Jayway Products AB
# 2016-2017 Föreningen Sambruk
#
# Licensed under AGPL, Version 3.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.gnu.org/licenses/agpl.txt
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Connect to a remote Streamflow instance using JMX-RMI
# Change the host, port and login info to match your environment

package require java
jmx_connect -h localhost -p 1099 -U administrator -P administrator
