package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.HashSet;
import java.util.Set;

import org.jxmapviewer.viewer.GeoPosition;

public class GeoUtils {

   public static Set<GeoPosition> positionSet(Iterable<PointMarker> points) {
      Set<GeoPosition> result = new HashSet<GeoPosition>();
      for (PointMarker p: points) {
         result.add(geoPosition(p));
      }
      return result;
   }

   private static GeoPosition geoPosition(PointMarker p) {
      return new GeoPosition(p.getLatitude(), p.getLongitude());
   }
}
