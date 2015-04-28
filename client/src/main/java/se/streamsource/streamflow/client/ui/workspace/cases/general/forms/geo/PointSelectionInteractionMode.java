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

import org.jxmapviewer.JXMapViewer;

import se.streamsource.streamflow.client.util.i18n;

public class PointSelectionInteractionMode implements MapInteractionMode {

   private PointSelectionListener pointSelectionListener;

   @Override
   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder) {
      pointSelectionListener = new PointSelectionListener(geoMarkerHolder, mapViewer);
      mapViewer.addMouseListener(pointSelectionListener);
   }

   @Override
   public void leaveMode(JXMapViewer mapViewer) {
      mapViewer.removeMouseListener(pointSelectionListener);
   }

   @Override
   public String getHelpHint() {
      return i18n.text(GeoLocationFieldPanelResources.mode_hint_select_point);
   }
}
