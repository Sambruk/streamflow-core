--
--
-- Copyright 2009-2012 Jayway Products AB
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

# Database creation script for Streamflow
# Create the Streamflow database along with 
# a "streamflow" user that has access to it
CREATE DATABASE streamflow
  DEFAULT CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;
CREATE USER 'streamflow'@'localhost' IDENTIFIED BY 'streamflow';
GRANT ALL PRIVILEGES ON streamflow.* TO 'streamflow'@'localhost' WITH GRANT OPTION;
CREATE USER 'streamflow'@'%' IDENTIFIED BY 'streamflow';
GRANT ALL PRIVILEGES ON streamflow.* TO 'streamflow'@'%' WITH GRANT OPTION;
