@echo off
rem
rem
rem Copyright 2009-2011 Streamsource AB
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem



rem
rem jmxsh
rem
rem A shell wrapper for jmxsh.
rem
rem Assumes java is in the PATH.  If not, will need to edit this script.
rem
rem You'll need to modify this when you find a place to jmxsh.
rem

set JMXSH_JARFILE=.\..\jmxsh-R4.jar

call java -jar %JMXSH_JARFILE% %1
