/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.Collections;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

class PointMarker extends GeoMarker {

   private GeoPosition position;

   public PointMarker(GeoPosition position) {
      this.position = position;
   }

   public GeoPosition getPosition() {
      return position;
   }

   @Override
   public List<GeoPosition> getPoints() {
      return Collections.singletonList(position);
   }

   @Override
   public String stringify() {
      return stringify(position);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((position == null) ? 0 : position.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      PointMarker other = (PointMarker) obj;
      if (position == null) {
         if (other.position != null)
            return false;
      } else if (!position.equals(other.position))
         return false;
      return true;
   }
}