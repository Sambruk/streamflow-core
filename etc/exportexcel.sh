#!/bin/sh
java -jar ../client/src/main/lib/excelbundle.jar -r ../client/src/main/resources/ -export ../client/target/streamflow.xls -l default,sv,sv_SE_gov -ref sv
