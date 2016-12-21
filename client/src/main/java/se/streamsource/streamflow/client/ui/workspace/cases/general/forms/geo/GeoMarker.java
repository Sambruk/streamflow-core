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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jxmapviewer.viewer.GeoPosition;

abstract class GeoMarker {

   public abstract List<GeoPosition> getPoints();

   public abstract String stringify();

   /** Parses a geomarker string in one of these formats: Point: "1.23, 4.56",
    * Lines: "(1.11, 2.22), (3.33, 4.44), (5.55, 6.66)",
    * Polygon: "(1.11, 2.22), (3.33, 4.44), (5.55, 6.66), (1.11, 2.22)"
    */
   public static GeoMarker parseGeoMarker(String string) {
      String trimmed = string.trim();

      if (trimmed.isEmpty()) {
         return null;
      }

      if (trimmed.startsWith("(")) {
         List<GeoPosition> points = parsePointList(trimmed);
         if (points.get(0).equals(points.get(points.size()-1))) {
            return new PolygonMarker(points);
         }
         else {
            return new LineMarker(points);
         }
      }
      else {
         return new PointMarker(parsePoint(trimmed));
      }
   }

   private static List<GeoPosition> parsePointList(String s) {
      List<GeoPosition> result = new ArrayList<GeoPosition>();

      while (!s.isEmpty()) {
         int endParenIndex = s.indexOf(')');
         if (endParenIndex == -1) {
            throw new IllegalArgumentException("Unterminated parenthesis in point list");
         }

         result.add(parsePoint(s.substring(1, endParenIndex-1)));

         int nextStartParenIndex = s.indexOf('(', 1);
         if (nextStartParenIndex == -1) {
            break;
         }

         s = s.substring(nextStartParenIndex);
      }

      return result;
   }

   private static GeoPosition parsePoint(String s) {
      String[] lonLat = s.split(",");
      if (lonLat.length != 2) {
         throw new IllegalArgumentException("Invalid position");
      }

      return new GeoPosition(Double.parseDouble(lonLat[0]), Double.parseDouble(lonLat[1]));
   }

   protected static String stringify(GeoPosition position) {
      return String.format(Locale.US, "%.15f,%.15f", position.getLatitude(), position.getLongitude());
   }
}