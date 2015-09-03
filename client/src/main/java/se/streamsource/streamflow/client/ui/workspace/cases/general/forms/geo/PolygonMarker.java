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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.viewer.GeoPosition;

class PolygonMarker extends GeoMarker {

   private List<GeoPosition> points;

   public PolygonMarker(Iterable<GeoPosition> points) {
      this.points = new ArrayList<GeoPosition>();
      for (GeoPosition p: points) {
         this.points.add(p);
      }
   }

   @Override
   public List<GeoPosition> getPoints() {
      return points;
   }

   @Override
   public String stringify() {
      StringBuilder sb = new StringBuilder();
      boolean firstElement = true;
      for (GeoPosition p: points) {
         if (firstElement) {
            firstElement = false;
         }
         else {
            sb.append(',');
         }
         sb.append('(');
         sb.append(stringify(p));
         sb.append(')');
      }
      return sb.toString();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((points == null) ? 0 : points.hashCode());
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
      PolygonMarker other = (PolygonMarker) obj;
      if (points == null) {
         if (other.points != null)
            return false;
      } else if (!points.equals(other.points))
         return false;
      return true;
   }
}