package se.streamsource.streamflow.client.util.mapquest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapquestAddress {

   private String country;
   private String house_number;
   private String postcode;
   private String pedestrian;
   private String road;
   private String city;

   public String getCountry() {
      return country;
   }

   public void setCountry(String country) {
      this.country = country;
   }

   public String getHouse_number() {
      return house_number;
   }

   public void setHouse_number(String house_number) {
      this.house_number = house_number;
   }

   public String getPostcode() {
      return postcode;
   }

   public void setPostcode(String postcode) {
      this.postcode = postcode;
   }

   public String getPedestrian() {
      return pedestrian;
   }

   public void setPedestrian(String pedestrian) {
      this.pedestrian = pedestrian;
   }

   public String getRoad() {
      return road;
   }

   public void setRoad(String road) {
      this.road = road;
   }

   public String getCity() {
      return city;
   }

   public void setCity(String city) {
      this.city = city;
   }
}
