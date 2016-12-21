#!/bin/sh
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



#
# jmxsh
#
# A shell wrapper for jmxsh.
#
# Assumes java is in the PATH.  If not, will need to edit this script.
#
# You'll need to modify this when you find a place to jmxsh.
#
JMXSH_JARFILE=./jmxsh-R4.jar

exec java -jar $JMXSH_JARFILE "$@"
