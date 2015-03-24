package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

class PointMarker extends GeoMarker {

   private double longitude;
   private double latitude;

   public PointMarker(double latitude, double longitude) {
      this.longitude = longitude;
      this.latitude = latitude;
   }

   public double getLongitude() {
      return longitude;
   }

   public double getLatitude() {
      return latitude;
   }

   @Override
   public List<PointMarker> getPoints() {
      return Collections.singletonList(this);
   }

   @Override
   public String stringify() {
      return String.format(Locale.US, "%.15f,%.15f", latitude, longitude);
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      long temp;
      temp = Double.doubleToLongBits(latitude);
      result = prime * result + (int) (temp ^ (temp >>> 32));
      temp = Double.doubleToLongBits(longitude);
      result = prime * result + (int) (temp ^ (temp >>> 32));
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
      if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
         return false;
      if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
         return false;
      return true;
   }


}