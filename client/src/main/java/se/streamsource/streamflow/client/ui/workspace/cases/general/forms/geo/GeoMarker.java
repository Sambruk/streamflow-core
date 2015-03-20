package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.ArrayList;
import java.util.List;

abstract class GeoMarker {

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
         List<PointMarker> points = parsePointList(trimmed);
         if (points.get(0).equals(points.get(points.size()-1))) {
            return new PolygonMarker(points);
         }
         else {
            return new LineMarker(points);
         }
      }
      else {
         return parsePoint(trimmed);
      }
   }

   private static List<PointMarker> parsePointList(String s) {
      List<PointMarker> result = new ArrayList<PointMarker>();

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

   private static PointMarker parsePoint(String s) {
      String[] lonLat = s.split(",");
      if (lonLat.length != 2) {
         throw new IllegalArgumentException("Invalid position");
      }

      return new PointMarker(Double.parseDouble(lonLat[0]), Double.parseDouble(lonLat[1]));
   }
}