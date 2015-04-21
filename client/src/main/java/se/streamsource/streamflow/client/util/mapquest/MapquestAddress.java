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
package se.streamsource.streamflow.client.util.mapquest;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapquestAddress {

   private String country;
   private String house_number;
   private String postcode;
   private String pedestrian;
   private String road;
   private String town;
   private String city;
   private String county;

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

   public String getTown() {
      return town;
   }

   public void setTown(String town) {
      this.town = town;
   }

   public String getCity() {
      return city;
   }

   public void setCity(String city) {
      this.city = city;
   }

   public String getCounty() {
      return county;
   }

   public void setCounty(String county) {
      this.county = county;
   }

   @Override
   public String toString() {
      return "MapquestAddress [country=" + country + ", house_number="
            + house_number + ", postcode=" + postcode + ", pedestrian="
            + pedestrian + ", road=" + road + ", city=" + city + ", county="
            + county + "]";
   }
}
