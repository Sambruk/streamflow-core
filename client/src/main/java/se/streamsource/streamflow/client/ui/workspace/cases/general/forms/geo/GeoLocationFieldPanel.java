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

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.VirtualEarthTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import se.streamsource.streamflow.api.administration.form.GeoLocationFieldValue;
import se.streamsource.streamflow.api.administration.form.LocationDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FieldSubmissionDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftSettingsDTO;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.AbstractFieldPanel;
import se.streamsource.streamflow.client.ui.workspace.cases.general.forms.FormSubmissionWizardPageModel;
import se.streamsource.streamflow.client.util.StateBinder;
import se.streamsource.streamflow.client.util.StreamflowToggleButton;
import se.streamsource.streamflow.client.util.mapquest.MapquestAddress;
import se.streamsource.streamflow.client.util.mapquest.MapquestNominatimService;
import se.streamsource.streamflow.client.util.mapquest.MapquestQueryResult;

public class GeoLocationFieldPanel extends AbstractFieldPanel implements GeoMarkerHolder
{
   private static final Logger logger = LoggerFactory.getLogger(GeoLocationFieldPanel.class);

   private StateBinder.Binding binding;
   private JXMapViewer mapViewer;
   private MapInteractionMode currentInteractionMode;

   /** Current value as handled by StreamFlow, ie a JSONified LocationDTO
    */
   private String currentValue;
   private GeoLocationFieldValue fieldValue;

   @Uses
   ObjectBuilder<MapquestNominatimService> geoLookupServiceBuilder;

   private FormSubmissionWizardPageModel model;
   private FormDraftSettingsDTO formDraftSettings;

   private ButtonGroup modeButtonGroup;
   private JLabel addressInfoLabel;
   private JLabel helpHintLabel;


   public GeoLocationFieldPanel(@Service ApplicationContext appContext, @Uses FieldSubmissionDTO field,
         @Uses GeoLocationFieldValue fieldValue, @Uses FormSubmissionWizardPageModel model)
   {
      super( field );
      this.model = model;
      this.fieldValue = fieldValue;
      this.formDraftSettings = model.getFormDraftModel().settings();

      setActionMap( appContext.getActionMap( this ) );

      mapViewer = createMapViewer();
      setMapType(MapType.ROAD_MAP);

      JPanel controlPanel = createControlPanel();

      FormLayout layout = new FormLayout("1dlu, 20dlu:grow, 4dlu, pref", "260dlu");
      setLayout(layout);
      add(mapViewer, new CellConstraints(2,1, CellConstraints.FILL, CellConstraints.FILL));
      add(controlPanel, new CellConstraints(4,1, CellConstraints.DEFAULT, CellConstraints.FILL));
   }

   private JXMapViewer createMapViewer() {
       JXMapViewer mapViewer = new JXMapViewer();

       mapViewer.setZoom(7);
       mapViewer.setBorder(new LineBorder(Color.GRAY));
       mapViewer.addComponentListener(new InitialMapScrollHandler());
       return mapViewer;
   }

   private JPanel createControlPanel() {
      JComboBox<MapType> mapTypeSelector = createMapTypeSelector();
      JPanel modeButtonPanel = createModeButtonPanel();
      addressInfoLabel = new JLabel();
      helpHintLabel = new JLabel("Help hint here");

      FormLayout layout = new FormLayout("60dlu", "pref, 4dlu, pref, 4dlu, pref, 4dlu, pref:grow");
      JPanel controlPanel = new JPanel(layout);
      controlPanel.add(mapTypeSelector, new CellConstraints(1,1));
      controlPanel.add(modeButtonPanel, new CellConstraints(1,3, CellConstraints.FILL, CellConstraints.DEFAULT));
      controlPanel.add(addressInfoLabel, new CellConstraints(1,5));
      controlPanel.add(helpHintLabel, new CellConstraints(1,7,CellConstraints.DEFAULT, CellConstraints.BOTTOM));

      return controlPanel;
   }

   private JComboBox<MapType> createMapTypeSelector() {
      final JComboBox<MapType> mapTypeSelector = new JComboBox<MapType>(MapType.values());
      mapTypeSelector.addItemListener(new ItemListener() {
         @Override
         public void itemStateChanged(ItemEvent e) {
            setMapType((MapType) mapTypeSelector.getSelectedItem());
         }
      });
      return mapTypeSelector;
   }

   private JPanel createModeButtonPanel() {
      modeButtonGroup = new ButtonGroup();
      FormLayout layout = new FormLayout("pref:grow", "pref, pref, pref");
      PanelBuilder builder = new PanelBuilder(layout);

      if (fieldValue.point().get()) {
         JToggleButton selectPointButton = new StreamflowToggleButton(getActionMap().get("startPointSelection"));
         modeButtonGroup.add(selectPointButton);
         builder.add(selectPointButton);
         builder.nextRow();
      }

      if (fieldValue.polyline().get()) {
         JToggleButton selectLineButton = new StreamflowToggleButton(getActionMap().get("startLineSelection"));
         modeButtonGroup.add(selectLineButton);
         builder.add(selectLineButton);
         builder.nextRow();
      }

      if (fieldValue.polygon().get()) {
         JToggleButton selectPolygonButton = new StreamflowToggleButton(getActionMap().get("startAreaSelection"));
         modeButtonGroup.add(selectPolygonButton);
         builder.add(selectPolygonButton);
         builder.nextRow();
      }

      return builder.getPanel();
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
      return currentValue;
   }

   @Override
   public void setValue(String newValue)
   {
      this.currentValue = newValue;
      addressInfoLabel.setText(formatAddressInfo(getCurrentLocationData()));
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

   public LocationDTO getCurrentLocationData() {
      return module.valueBuilderFactory().newValueFromJSON( LocationDTO.class, "".equals( currentValue ) ? "{}" : currentValue );
   }

   @Override
   public GeoMarker getCurrentGeoMarker() {
      return GeoMarker.parseGeoMarker(getCurrentLocationData().location().get());
   }

   @Override
   public void updateGeoMarker(GeoMarker marker) {
      LocationDTO locationData = locationDataForMarker(marker);
      currentValue = locationData.toJSON();
      addressInfoLabel.setText(formatAddressInfo(locationData));
      binding.updateProperty(getValue());
      switchInteractionMode(new PanZoomInteractionMode());
      initiateGetAddressInfo(marker);
   }

   private void initiateGetAddressInfo(GeoMarker marker) {
      final GeoPosition firstPoint = marker.getPoints().get(0);
      new SwingWorker<MapquestQueryResult, Object>() {

         @Override
         protected MapquestQueryResult doInBackground() throws Exception {
            String urlPattern = formDraftSettings.mapquestReverseLookupUrlPattern().get();
            MapquestNominatimService geoLookupService = geoLookupServiceBuilder.use(urlPattern).newInstance();
            return geoLookupService.reverseLookup(firstPoint.getLatitude(), firstPoint.getLongitude());
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
      LocationDTO updatedLocationData = locationDataWithAddressInfo(getCurrentLocationData(), mapquestQueryResult);
      currentValue = updatedLocationData.toJSON();
      addressInfoLabel.setText(formatAddressInfo(updatedLocationData));
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
      prototype.city().set(firstNonNull(address.getTown(), address.getCity(), address.getCounty(), ""));
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
         GeoPosition defaultPosition = ((PointMarker) GeoMarker.parseGeoMarker(formDraftSettings.location().get())).getPosition();
         mapViewer.setAddressLocation(defaultPosition);
         mapViewer.setZoom(formDraftSettings.zoomLevel().get() != null ? formDraftSettings.zoomLevel().get() : 6);
      }
      else if (marker instanceof PointMarker) {
         mapViewer.setAddressLocation(((PointMarker) marker).getPosition());
         mapViewer.setZoom(formDraftSettings.zoomLevel().get() != null ? formDraftSettings.zoomLevel().get() : 6);
      }
      else {
         Set<GeoPosition> positionsForMarker = new HashSet<GeoPosition>(marker.getPoints());
         mapViewer.zoomToBestFit(positionsForMarker, 0.5);
      }
   }

   @Action
   public void startPointSelection() {
      switchInteractionMode(new PointSelectionInteractionMode());
   }

   @Action
   public void startLineSelection() {
      switchInteractionMode(new LineSelectionInteractionMode());
   }

   @Action
   public void startAreaSelection() {
      switchInteractionMode(new AreaSelectionInteractionMode());
   }

   private void switchInteractionMode(MapInteractionMode newMode) {
      if (currentInteractionMode != null) {
         currentInteractionMode.leaveMode(mapViewer);
      }

      if (newMode instanceof PanZoomInteractionMode) {
         modeButtonGroup.clearSelection();
      }

      newMode.enterMode(mapViewer, this);
      helpHintLabel.setText("<html>" + newMode.getHelpHint() + "</html>");
      currentInteractionMode = newMode;
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