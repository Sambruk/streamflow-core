#!/bin/sh
java -jar ../client/src/main/lib/excelbundle.jar -p -r ../client/src/main/resources/ -import ../client/target/streamflow.xls -l sv_SE_gov -ref sv
