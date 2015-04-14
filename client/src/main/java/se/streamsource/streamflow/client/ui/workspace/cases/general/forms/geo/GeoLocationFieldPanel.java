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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.ApplicationContext;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.AbstractFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageModel;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.dialog.DialogService;
import se.streamsource.streamflow.client.util.mapquest.MapquestAddress;
import se.streamsource.streamflow.client.util.mapquest.MapquestNominatimService;
import se.streamsource.streamflow.client.util.mapquest.MapquestQueryResult;

public class GeoLocationFieldPanel extends AbstractFieldPanel implements GeoMarkerHolder
{
   private static final Logger logger = LoggerFactory.getLogger(GeoLocationFieldPanel.class);

   private JTextField textField;
   private StateBinder.Binding binding;
   private JXMapViewer mapViewer;
   private MapInteractionMode currentInteractionMode;
   private LocationDTO currentLocationData;
   private GeoLocationFieldValue fieldValue;

   @Service
   DialogService dialogs;

   private FormSubmissionWizardPageModel model;

   private ButtonGroup modeButtonGroup;

   private JLabel addressInfoLabel;

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

       mapViewer.setZoom(7);
       mapViewer.addComponentListener(new InitialMapScrollHandler());
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

      modeButtonGroup = new ButtonGroup();
      JToggleButton selectPointButton = new JToggleButton("Select point");
      JToggleButton selectLineButton = new JToggleButton("Select line");
      JToggleButton selectPolygonButton = new JToggleButton("Select area");
      modeButtonGroup.add(selectPointButton);
      modeButtonGroup.add(selectLineButton);
      modeButtonGroup.add(selectPolygonButton);
      dummyTopPanel.add(selectPointButton);
      dummyTopPanel.add(selectLineButton);
      dummyTopPanel.add(selectPolygonButton);

      addressInfoLabel = new JLabel();
      dummyTopPanel.add(addressInfoLabel);
      dummyTopPanel.add(new JLabel("Help hint here"));

      selectPointButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            switchInteractionMode(new PointSelectionInteractionMode());
         }
      });
      selectLineButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            switchInteractionMode(new LineSelectionInteractionMode());
         }
      });
      selectPolygonButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            switchInteractionMode(new AreaSelectionInteractionMode());
         }
      });

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
         break;
      }
      case SATELLITE:
      {
         TileFactoryInfo info = new VirtualEarthTileFactoryInfo(VirtualEarthTileFactoryInfo.SATELLITE);
         DefaultTileFactory tileFactory = new DefaultTileFactory(info);
         mapViewer.setTileFactory(tileFactory);
         break;
      }
      default:
         mapViewer.setTileFactory(null);
      }
   }

   @Override
   public String getValue()
   {
      return currentLocationData.toJSON();
   }

   @Override
   public void setValue(String newValue)
   {
      textField.setText( newValue );
      currentLocationData = parseLocationDTOValue(newValue);
      addressInfoLabel.setText(formatAddressInfo(currentLocationData));
      switchInteractionMode(new PanZoomInteractionMode());
      scrollMarkerIntoView(getCurrentGeoMarker());
   }

   private static String formatAddressInfo(LocationDTO locationDTO) {
      List<String> elements = new ArrayList<String>();
      if (!StringUtils.isBlank(locationDTO.street().get())) {
         elements.add(locationDTO.street().get());
      }
      if (!StringUtils.isBlank(locationDTO.zipcode().get())) {
         elements.add(locationDTO.zipcode().get());
      }
      if (!StringUtils.isBlank(locationDTO.city().get())) {
         elements.add(locationDTO.city().get());
      }
      if (!StringUtils.isBlank(locationDTO.country().get())) {
         elements.add(locationDTO.country().get());
      }

      return "<html>" + StringUtils.join(elements, ", ") + "</html>";
   }

   @Override
   public GeoMarker getCurrentGeoMarker() {
      return GeoMarker.parseGeoMarker(currentLocationData.location().get());
   }

   @Override
   public void updateGeoMarker(GeoMarker marker) {
      currentLocationData = locationDataForMarker(marker);
      addressInfoLabel.setText(formatAddressInfo(currentLocationData));
      binding.updateProperty(getValue());
      switchInteractionMode(new PanZoomInteractionMode());
      initiateGetAddressInfo(marker);
   }

   private void initiateGetAddressInfo(GeoMarker marker) {
      final PointMarker firstPoint = marker.getPoints().get(0);
      new SwingWorker<MapquestQueryResult, Object>() {

         @Override
         protected MapquestQueryResult doInBackground() throws Exception {
            return new MapquestNominatimService().reverseLookup(firstPoint.getLatitude(), firstPoint.getLongitude());
         }

         @Override
         protected void done() {
            try {
               updateAddressInfo(get());
            } catch (Exception e) {
               logger.warn("Failed to get address info", e);
            }
         }
      }.execute();
   }

   private void updateAddressInfo(MapquestQueryResult mapquestQueryResult) {
      currentLocationData = locationDataWithAddressInfo(currentLocationData, mapquestQueryResult);
      addressInfoLabel.setText(formatAddressInfo(currentLocationData));
      binding.updateProperty(getValue());
   }

   private LocationDTO locationDataForMarker(GeoMarker marker) {
      ValueBuilder<LocationDTO> builder = module.valueBuilderFactory().newValueBuilder(LocationDTO.class);
      LocationDTO prototype = builder.prototype();
      prototype.location().set(marker.stringify());
      return builder.newInstance();
   }

   private LocationDTO locationDataWithAddressInfo(LocationDTO locationData, MapquestQueryResult mapquestQueryResult) {
      ValueBuilder<LocationDTO> builder = module.valueBuilderFactory().newValueBuilder(LocationDTO.class).withPrototype(locationData);
      LocationDTO prototype = builder.prototype();

      MapquestAddress address = mapquestQueryResult.getAddress();

      String street = firstNonNull(address.getRoad(), address.getPedestrian(), "");
      if (address.getHouse_number() != null) {
         street = street + " " + address.getHouse_number();
      }

      prototype.street().set(street);
      prototype.zipcode().set(firstNonNull(address.getPostcode(), ""));
      prototype.city().set(firstNonNull(address.getCity(), address.getCounty(), ""));
      prototype.country().set(firstNonNull(address.getCountry(), ""));

      return builder.newInstance();
   }

   private <T> T firstNonNull(T... args) {
      for (T t: args) {
         if (t != null) {
            return t;
         }
      }

      return null;
   }

   @Override
   public void removeNotify() {
      super.removeNotify();
      mapViewer.getTileFactory().dispose();
   }

   private void scrollMarkerIntoView(GeoMarker marker) {
      if (marker == null) {
         // TODO: Scroll to default location
      }
      else {
         List<PointMarker> pointsInMarker = marker.getPoints();
         Set<GeoPosition> positionsForMarker = GeoUtils.positionSet(pointsInMarker);
         mapViewer.zoomToBestFit(positionsForMarker, 0.5);
      }
   }

   private void switchInteractionMode(MapInteractionMode newMode) {
      if (currentInteractionMode != null) {
         currentInteractionMode.leaveMode(mapViewer);
      }

      if (newMode instanceof PanZoomInteractionMode) {
         modeButtonGroup.clearSelection();
      }

      newMode.enterMode(mapViewer, this);
      currentInteractionMode = newMode;
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
      this.binding = binding;
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

   class InitialMapScrollHandler extends ComponentAdapter {
      @Override
      public void componentShown(ComponentEvent e) {
         scrollMarkerIntoView(getCurrentGeoMarker());
      }
      @Override
      public void componentResized(ComponentEvent e) {
         scrollMarkerIntoView(getCurrentGeoMarker());
      }
   }
}