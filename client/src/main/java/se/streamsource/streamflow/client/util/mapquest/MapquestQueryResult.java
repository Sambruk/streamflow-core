/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.client.util.mapquest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapquestQueryResult {

   private String osm_type;
   private MapquestAddress address;

   public String getOsm_type() {
      return osm_type;
   }

   public void setOsm_type(String osm_type) {
      this.osm_type = osm_type;
   }

   public MapquestAddress getAddress() {
      return address;
   }

   public void setAddress(MapquestAddress address) {
      this.address = address;
   }

   @Override
   public String toString() {
      return "MapquestQueryResult [osm_type=" + osm_type + ", address="
            + address + "]";
   }
}
