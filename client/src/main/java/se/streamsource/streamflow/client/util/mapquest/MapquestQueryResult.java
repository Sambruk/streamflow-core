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
}
