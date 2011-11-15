/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.server.plugin.address
;

/**
 * Lookup contact information given a template which contains "clues" as to who the person is.
 */
public interface StreetAddressLookup
{
   /**
    * Give a template for street information that is partially filled in, and let the plugin
    * do a lookup for street addresses that match. May return 0, 1 or more possible alternatives.
    *
    * @param streetTemplate partially filled in template with contact information.
    * @return possible matches
    */
   StreetList lookup(StreetValue streetTemplate);

   /**
    * Reloads the solr index from data provided by the underlying plugin.
    */
   void reindex();

}