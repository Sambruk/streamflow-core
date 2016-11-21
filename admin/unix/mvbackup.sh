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

# Change folder locations of SFBACKUP and DEST according to your OS
# Depending on what user is running the script you might need to apply sudo to some of the commands
# Run the script manually the first time to assure everything works as expected
# When satisfied, enable the "rm" command to empty the backup folder

SFBACKUP=/Users/arvidhuss/Library/Application\ Support/StreamflowServer/backup/
DEST=/Users/arvidhuss/demo/

ORIG=$(pwd)
NOW=$(date +"%Y%m%d-%H%M")
FILE="streamflow-mvbackup-$NOW.tar"

cd "$SFBACKUP"

tar -cvf $FILE streamflow*

mv $FILE "$DEST"

# rm streamflow*

cd "$ORIG"