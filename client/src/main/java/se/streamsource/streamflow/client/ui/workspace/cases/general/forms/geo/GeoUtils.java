package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;

public class GeoUtils {

   public static GeoPosition geoPosition(PointMarker p) {
      return new GeoPosition(p.getLatitude(), p.getLongitude());
   }

   public static PointMarker pointMarker(GeoPosition p) {
      return new PointMarker(p.getLatitude(), p.getLongitude());
   }

   public static Set<GeoPosition> positionSet(Iterable<PointMarker> points) {
      Set<GeoPosition> result = new HashSet<GeoPosition>();
      for (PointMarker p: points) {
         result.add(geoPosition(p));
      }
      return result;
   }

   public static List<GeoPosition> positionList(Iterable<PointMarker> points) {
      List<GeoPosition> result = new ArrayList<GeoPosition>();
      for (PointMarker p: points) {
         result.add(geoPosition(p));
      }
      return result;
   }

   public static List<PointMarker> pointMarkerList(Iterable<GeoPosition> positions) {
      List<PointMarker> result = new ArrayList<PointMarker>();
      for (GeoPosition p: positions) {
         result.add(pointMarker(p));
      }
      return result;
   }
}
