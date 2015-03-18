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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.ApplicationContext;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;

public class GeoLocationFieldPanel extends AbstractFieldPanel
{
   private static final Logger logger = LoggerFactory.getLogger(GeoLocationFieldPanel.class);

   private JTextField textField;
   private JXMapViewer mapViewer;
   private GeoLocationFieldValue fieldValue;

   
   @Service
   DialogService dialogs;

   private FormSubmissionWizardPageModel model;

   public GeoLocationFieldPanel(@Service ApplicationContext appContext, @Uses FieldSubmissionDTO field,
         @Uses GeoLocationFieldValue fieldValue, @Uses FormSubmissionWizardPageModel model)
   {
      super( field );
      this.model = model;
      setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
      this.fieldValue = fieldValue;

      textField = new JTextField();
      add(textField);
      textField.setColumns( 50 ); // TODO: Fix magic number

      setBorder(new LineBorder(Color.GREEN));

      mapViewer = setUpMapViewer();
      mapViewer.setPreferredSize(new Dimension(500, 400));
      add(mapViewer);
      
      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();
   }

   private JXMapViewer setUpMapViewer() {
       JXMapViewer mapViewer = new JXMapViewer();

       // Create a TileFactoryInfo for OpenStreetMap
       TileFactoryInfo info = new OSMTileFactoryInfo();
       DefaultTileFactory tileFactory = new DefaultTileFactory(info);
       mapViewer.setTileFactory(tileFactory);

       // Use 8 threads in parallel to load the tiles
       tileFactory.setThreadPoolSize(8);

       // Set the focus
       GeoPosition frankfurt = new GeoPosition(50.11, 8.68);

       mapViewer.setZoom(7);
       mapViewer.setAddressLocation(frankfurt);

       return mapViewer;
   }

   @Override
   public String getValue()
   {
      return textField.getText();
   }

   @Override
   public void setValue(String newValue)
   {
      textField.setText( newValue );

      LocationDTO locationDTO = parseLocationDTOValue(newValue);
      GeoMarker geoMarker = parseGeoMarker(locationDTO.location().get());
      if (geoMarker instanceof PointMarker) {
         PointMarker point = (PointMarker) geoMarker;
         mapViewer.setAddressLocation(new GeoPosition(point.getLon(), point.getLat()));         
      }
   }


   private LocationDTO parseLocationDTOValue(String newValue) {
       return module.valueBuilderFactory().newValueFromJSON( LocationDTO.class, "".equals( newValue ) ? "{}" : newValue );
   }
   
   /** Parses a geomarker string in one of these formats: Point: "1.23, 4.56", 
    * Lines: "(1.11, 2.22), (3.33, 4.44), (5.55, 6.66)", 
    * Polygon: "(1.11, 2.22), (3.33, 4.44), (5.55, 6.66), (1.11, 2.22)"   
    */
   static GeoMarker parseGeoMarker(String string) {
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

   @Override
   public boolean validateValue(Object newValue)
   {
      return true; // TODO: Validate geo value
   }

   @Override
   public void setBinding(final StateBinder.Binding binding)
   {
      final GeoLocationFieldPanel panel = this;
      textField.setInputVerifier( new InputVerifier()
      {
         @Override
         public boolean verify(JComponent input)
         {
             // TODO: Verify geo value properly
             
//            if (!Strings.empty( fieldValue.regularExpression().get() )
//                  && !Strings.empty( ((JTextComponent) input).getText() ))
//            {
//               try
//               {
//                  new RegexPatternFormatter( fieldValue.regularExpression().get() )
//                        .stringToValue( ((JTextComponent) input).getText() );
//               } catch (ParseException e)
//               {
//                  dialogs.showMessageDialog( panel, i18n.text( CaseResources.regular_expression_does_not_validate ), "" );
//                  return false;
//               }
//            }
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      } );
   }

//   @Override
//   protected String componentName()
//   {
//      StringBuilder componentName = new StringBuilder( "<html>" );
//      componentName.append( title() );
//      if (!Strings.empty( fieldValue.hint().get() ))
//      {
//         componentName.append( " <font color='#778899'>(" ).append( fieldValue.hint().get() ).append( ")</font>" );
//      }
//
//      if (mandatory())
//      {
//         componentName.append( " <font color='red'>*</font>" );
//      }
//      componentName.append( "</html>" );
//      return componentName.toString();
//   }

   static abstract class GeoMarker {

   }

   static class PointMarker extends GeoMarker {
      
      private double lon;
      private double lat;

      public PointMarker(double lon, double lat) {
         this.lon = lon;
         this.lat = lat;
      }

      public double getLon() {
         return lon;
      }

      public double getLat() {
         return lat;
      }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         long temp;
         temp = Double.doubleToLongBits(lat);
         result = prime * result + (int) (temp ^ (temp >>> 32));
         temp = Double.doubleToLongBits(lon);
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
         if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
            return false;
         if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
            return false;
         return true;
      } 
      
      
   }

   static class LineMarker extends GeoMarker {

      private List<PointMarker> points;

      public LineMarker(List<PointMarker> points) {
         this.points = points;
      }

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

   static class PolygonMarker extends GeoMarker {
   
      private List<PointMarker> points;

      public PolygonMarker(List<PointMarker> points) {
         this.points = points;
      }   

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
         PolygonMarker other = (PolygonMarker) obj;
         if (points == null) {
            if (other.points != null)
               return false;
         } else if (!points.equals(other.points))
            return false;
         return true;
      }              
   }
}