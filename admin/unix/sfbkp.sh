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


# Backup Streamflow EntityStore,EventDiff and/or statistics database
SFDIR=/usr/share/tomcat6
BACKUPDEST="$2"

old_dir=`pwd`

cd `dirname "$0"`

streamflow()
{
	sh jmxsh backup.tcl
}

statistic()
{
	sh automysqlbackup.sh
}

case "$1" in
        streamflow)
                streamflow
                ;;
        statistic)
                statistic
                ;;
        full)
                streamflow
                statistic
                cd "$SFDIR"
                NAME=StreamFlowServerBackup_`hostname`_`date +%Y-%m-%d_%Hh%Mm`.zip
                tar -cvf $NAME ./.StreamFlowServer/data/backup ./.StreamFlowServer/statisticbackup ./.java

                if [ -z "$BACKUPDEST" ]; then
                	echo "Missing backup destination host. Backup stored locally!"
                else
			echo "Sending $NAME to $BACKUPDEST"
                	scp -i ./admin/id-eu-streamflow-keypair $SFDIR/$NAME root@$BACKUPDEST:/var/sfbackups/
			rm $NAME
			echo "Backup DONE."
                fi
                ;;
        *)
                echo "Usage: $0 streamflow | statistic | full <backup destination>"
                exit 3
esac

cd $old_dir
