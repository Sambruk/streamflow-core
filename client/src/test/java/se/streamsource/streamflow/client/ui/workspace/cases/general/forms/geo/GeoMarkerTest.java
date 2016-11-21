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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.jxmapviewer.viewer.GeoPosition;

public class GeoMarkerTest {

   @Test
   public void parseEmpty() {
      GeoMarker marker = GeoMarker.parseGeoMarker("");
      assertThat(marker, nullValue());
   }

   @Test
   public void parseNull() {
      GeoMarker marker = GeoMarker.parseGeoMarker("");
      assertThat(marker, nullValue());
   }

   @Test(expected=IllegalArgumentException.class)
   public void parseInvalidString() {
      GeoMarker.parseGeoMarker("invalid");
   }

   @Test(expected=IllegalArgumentException.class)
   public void parseInvalidOneNumberOnly() {
      GeoMarker.parseGeoMarker("1.234");
   }

   @Test(expected=IllegalArgumentException.class)
   public void parseInvalidPair() {
      GeoMarker.parseGeoMarker("1.234,foo");
   }

   @Test
   public void parsePointMarker() {
      GeoMarker marker = GeoMarker.parseGeoMarker("1.500,2.750");
      checkPointMarker(marker, 1.5, 2.75);
   }

   @Test
   public void parsePointMarkerWithSpaces() {
      GeoMarker marker = GeoMarker.parseGeoMarker("  1.500  ,  2.750  ");
      checkPointMarker(marker, 1.5, 2.75);
   }

   @Test(expected=IllegalArgumentException.class)
   public void parseUnterminatedPointList() {
      GeoMarker.parseGeoMarker("(1.234,1.234");
   }

   @Test
   public void parseLine() {
      GeoMarker marker = GeoMarker.parseGeoMarker("(1.500,2.750), ( 3.5, 4.75 )  ");
      assertThat(marker, instanceOf(LineMarker.class));
      LineMarker line = (LineMarker) marker;
      assertThat(line.getPoints().size(), equalTo(2));
      checkGeoPosition(line.getPoints().get(0), 1.5, 2.75);
      checkGeoPosition(line.getPoints().get(1), 3.5, 4.75);
   }

   @Test
   public void parsePolygon() {
      GeoMarker marker = GeoMarker.parseGeoMarker("(1.500,2.750), ( 3.5, 4.75 ), (1.500,2.750)");
      assertThat(marker, instanceOf(PolygonMarker.class));
      PolygonMarker polygon = (PolygonMarker) marker;
      assertThat(polygon.getPoints().size(), equalTo(3));
      checkGeoPosition(polygon.getPoints().get(0), 1.5, 2.75);
      checkGeoPosition(polygon.getPoints().get(1), 3.5, 4.75);
      checkGeoPosition(polygon.getPoints().get(2), 1.5, 2.75);
   }

   @Test
   public void stringifyPoint() {
      PointMarker marker = new PointMarker(new GeoPosition(1.5, 2.75));
      String stringified = marker.stringify();
      assertThat(stringified, equalTo("1.500000000000000,2.750000000000000"));
   }

   @Test
   public void stringifyLine() {
      LineMarker marker = new LineMarker(Arrays.asList(
            new GeoPosition(1.5, 2.75), new GeoPosition(3.5, 4.75)));
      String stringified = marker.stringify();
      assertThat(stringified, equalTo("(1.500000000000000,2.750000000000000),(3.500000000000000,4.750000000000000)"));
   }

   @Test
   public void stringifyPolygon() {
      PolygonMarker marker = new PolygonMarker(Arrays.asList(
            new GeoPosition(1.5, 2.75),
            new GeoPosition(3.5, 4.75),
            new GeoPosition(5.5, 6.75),
            new GeoPosition(1.5, 2.75)
            ));
      String stringified = marker.stringify();
      assertThat(stringified, equalTo("(1.500000000000000,2.750000000000000),(3.500000000000000,4.750000000000000),(5.500000000000000,6.750000000000000),(1.500000000000000,2.750000000000000)"));
   }

   private void checkPointMarker(GeoMarker marker, double lat, double lon) {
      assertThat(marker, notNullValue());
      assertThat(marker, instanceOf(PointMarker.class));
      PointMarker point = (PointMarker) marker;
      assertThat(point.getPosition().getLongitude(), equalTo(lon));
      assertThat(point.getPosition().getLatitude(), equalTo(lat));
   }

   private void checkGeoPosition(GeoPosition position, double lat, double lon) {
      assertThat(position.getLongitude(), equalTo(lon));
      assertThat(position.getLatitude(), equalTo(lat));
   }
}
