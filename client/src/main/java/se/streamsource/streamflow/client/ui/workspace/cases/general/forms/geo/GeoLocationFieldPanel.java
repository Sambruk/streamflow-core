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
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;
import javax.swing.event.MouseInputListener;
import javax.swing.text.JTextComponent;

import org.jdesktop.application.ApplicationContext;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.AbstractFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageModel;
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
      setLayout( new BorderLayout() );
      this.fieldValue = fieldValue;

      textField = new JTextField();
      add(textField, BorderLayout.NORTH);
      textField.setColumns( 50 ); // TODO: Fix magic number

      setBorder(new LineBorder(Color.GREEN));

      mapViewer = setUpMapViewer();
      mapViewer.setPreferredSize(new Dimension(500, 400));
      setMapType(MapType.ROAD_MAP);
      add(mapViewer, BorderLayout.CENTER);

      JPanel controlPanel = setupControlPanel();
      add(controlPanel, BorderLayout.EAST);

      setActionMap( appContext.getActionMap( this ) );
      ActionMap am = getActionMap();
   }

   private JXMapViewer setUpMapViewer() {
       JXMapViewer mapViewer = new JXMapViewer();

       // Add interactions
       MouseInputListener panListener = new PanMouseInputListener(mapViewer);
       mapViewer.addMouseListener(panListener);
       mapViewer.addMouseMotionListener(panListener);
       mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
       mapViewer.addKeyListener(new PanKeyListener(mapViewer));

       // Set the focus
       GeoPosition frankfurt = new GeoPosition(50.11, 8.68);

       mapViewer.setZoom(7);
       mapViewer.setAddressLocation(frankfurt);

       return mapViewer;
   }

   private JPanel setupControlPanel() {
      // TODO: Clean up this layout mess

      JPanel controlPanel = new JPanel(new BorderLayout(10, 0));

      JPanel dummyTopPanel = new JPanel();
      controlPanel.add(dummyTopPanel, BorderLayout.NORTH);

      dummyTopPanel.setLayout(new BoxLayout(dummyTopPanel, BoxLayout.Y_AXIS));

      final JComboBox<MapType> mapTypeSelector = new JComboBox<MapType>(MapType.values());
      mapTypeSelector.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            setMapType((MapType) mapTypeSelector.getSelectedItem());
         }
      });
      dummyTopPanel.add(mapTypeSelector);

      ButtonGroup modeButtonGroup = new ButtonGroup();
      JToggleButton selectPointButton = new JToggleButton("Select point");
      JToggleButton selectLineButton = new JToggleButton("Select line");
      JToggleButton selectPolygonButton = new JToggleButton("Select area");
      modeButtonGroup.add(selectPointButton);
      modeButtonGroup.add(selectLineButton);
      modeButtonGroup.add(selectPolygonButton);
      dummyTopPanel.add(selectPointButton);
      dummyTopPanel.add(selectLineButton);
      dummyTopPanel.add(selectPolygonButton);

      dummyTopPanel.add(new JLabel("Address here"));
      dummyTopPanel.add(new JLabel("Help hint here"));

      return controlPanel;
   }

   private void setMapType(MapType mapType) {
      if (mapViewer.getTileFactory() != null) {
         mapViewer.getTileFactory().dispose();
      }

      switch (mapType) {
      case ROAD_MAP:
      {
         TileFactoryInfo info = new OSMTileFactoryInfo();
         DefaultTileFactory tileFactory = new DefaultTileFactory(info);
         mapViewer.setTileFactory(tileFactory);
         tileFactory.setThreadPoolSize(8);
         break;
      }
      case SATELLITE:
      {
         TileFactoryInfo info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE);
         DefaultTileFactory tileFactory = new DefaultTileFactory(info);
         mapViewer.setTileFactory(tileFactory);
         tileFactory.setThreadPoolSize(8);
         break;
      }
      default:
         mapViewer.setTileFactory(null);
      }


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
      GeoMarker geoMarker = GeoMarker.parseGeoMarker(locationDTO.location().get());
      if (geoMarker instanceof PointMarker) {
         PointMarker point = (PointMarker) geoMarker;
         mapViewer.setAddressLocation(new GeoPosition(point.getLatitude(), point.getLongitude()));

         final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
         waypointPainter.setWaypoints(Collections.singleton(new DefaultWaypoint(point.getLatitude(), point.getLongitude())));

         mapViewer.setOverlayPainter(waypointPainter);


         mapViewer.addMouseListener(new PointSelector(this, mapViewer));
      }
   }


   public void setGeoMarker(GeoMarker marker) {
      if (marker instanceof PointMarker) {
         PointMarker point = (PointMarker) marker;
         logger.info("Point: "+point.getLatitude()+","+point.getLongitude());
      }
   }

   private LocationDTO parseLocationDTOValue(String newValue) {
       return module.valueBuilderFactory().newValueFromJSON( LocationDTO.class, "".equals( newValue ) ? "{}" : newValue );
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
            binding.updateProperty( ((JTextComponent) input).getText() );
            return true;
         }
      } );
   }

   enum MapType {
      ROAD_MAP("Road map"),
      SATELLITE("Satellite");

      String name;

      private MapType(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return name;
      }
   }
}