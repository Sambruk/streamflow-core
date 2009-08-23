#!/bin/sh
ssh wayuser@192.168.0.146 ls -t /opt/nexus-1.1.1/sonatype-work/nexus/storage/snapshots/se/streamsource/streamflow/streamflow-war/0.1-SNAPSHOT/*.war | head -1
