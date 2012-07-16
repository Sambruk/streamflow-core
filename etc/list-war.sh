#!/bin/sh
#
#
# Copyright 2009-2012 Jayway Products AB
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

ssh wayuser@192.168.0.146 ls -t /opt/nexus-1.1.1/sonatype-work/nexus/storage/snapshots/se/streamsource/streamflow/streamflow-war/0.1-SNAPSHOT/*.war | head -1
