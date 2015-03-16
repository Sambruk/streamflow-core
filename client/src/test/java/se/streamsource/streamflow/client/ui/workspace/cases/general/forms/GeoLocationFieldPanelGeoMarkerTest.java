package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.text.ParseException;

import org.junit.Test;

import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.GeoLocationFieldPanel.GeoMarker;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.GeoLocationFieldPanel.LineMarker;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.GeoLocationFieldPanel.PointMarker;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.GeoLocationFieldPanel.PolygonMarker;

public class GeoLocationFieldPanelGeoMarkerTest {

   @Test
   public void parseEmpty() {
      GeoMarker marker = GeoLocationFieldPanel.parseGeoMarker("");
      assertThat(marker, nullValue());
   }
   
   @Test
   public void parseNull() {
      GeoMarker marker = GeoLocationFieldPanel.parseGeoMarker("");
      assertThat(marker, nullValue());
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void parseInvalidString() {
      GeoLocationFieldPanel.parseGeoMarker("invalid");
   }

   @Test(expected=IllegalArgumentException.class)
   public void parseInvalidOneNumberOnly() {
      GeoLocationFieldPanel.parseGeoMarker("1.234");
   }

   @Test(expected=IllegalArgumentException.class)
   public void parseInvalidPair() {
      GeoLocationFieldPanel.parseGeoMarker("1.234,foo");
   }

   @Test
   public void parsePointMarker() {
      GeoMarker marker = GeoLocationFieldPanel.parseGeoMarker("1.500,2.750");
      checkPointMarker(marker, 1.5, 2.75);
   }

   @Test
   public void parsePointMarkerWithSpaces() {
      GeoMarker marker = GeoLocationFieldPanel.parseGeoMarker("  1.500  ,  2.750  ");
      checkPointMarker(marker, 1.5, 2.75);
   }
   
   @Test(expected=IllegalArgumentException.class)
   public void parseUnterminatedPointList() {
      GeoLocationFieldPanel.parseGeoMarker("(1.234,1.234");
   }

   @Test
   public void parseLine() {
      GeoMarker marker = GeoLocationFieldPanel.parseGeoMarker("(1.500,2.750), ( 3.5, 4.75 )  ");
      assertThat(marker, instanceOf(LineMarker.class));
      LineMarker line = (LineMarker) marker;
      assertThat(line.getPoints().size(), equalTo(2));      
      checkPointMarker(line.getPoints().get(0), 1.5, 2.75);
      checkPointMarker(line.getPoints().get(1), 3.5, 4.75);
   }
   
   @Test
   public void parsePolygon() {
      GeoMarker marker = GeoLocationFieldPanel.parseGeoMarker("(1.500,2.750), ( 3.5, 4.75 ), (1.500,2.750)");
      assertThat(marker, instanceOf(PolygonMarker.class));
      PolygonMarker polygon = (PolygonMarker) marker;
      assertThat(polygon.getPoints().size(), equalTo(3));      
      checkPointMarker(polygon.getPoints().get(0), 1.5, 2.75);
      checkPointMarker(polygon.getPoints().get(1), 3.5, 4.75);
      checkPointMarker(polygon.getPoints().get(2), 1.5, 2.75);
   }
   
   private void checkPointMarker(GeoMarker marker, double lon, double lat) {
      assertThat(marker, notNullValue());
      assertThat(marker, instanceOf(PointMarker.class));
      PointMarker point = (PointMarker) marker;
      assertThat(point.getLon(), equalTo(lon));
      assertThat(point.getLat(), equalTo(lat));
   }
}
