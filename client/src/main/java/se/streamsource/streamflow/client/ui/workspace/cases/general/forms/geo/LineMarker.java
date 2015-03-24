package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.ArrayList;
import java.util.List;

class LineMarker extends GeoMarker {

   private List<PointMarker> points;

   public LineMarker(Iterable<PointMarker> points) {
      this.points = new ArrayList<PointMarker>();
      for (PointMarker p: points) {
         this.points.add(p);
      }
   }

   @Override
   public List<PointMarker> getPoints() {
      return points;
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
      LineMarker other = (LineMarker) obj;
      if (points == null) {
         if (other.points != null)
            return false;
      } else if (!points.equals(other.points))
         return false;
      return true;
   }
}