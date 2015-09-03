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

import java.util.Collections;
import java.util.List;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

import se.streamsource.streamflow.client.util.i18n;

public class PanZoomInteractionMode implements MapInteractionMode {

   private MouseInputListener panMouseInputListener;
   private ZoomMouseWheelListenerCursor zoomMouseWheelListener;
   private PanKeyListener panKeyListener;

   @Override
   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder) {
      panMouseInputListener = new PanMouseInputListener(mapViewer);
      mapViewer.addMouseListener(panMouseInputListener);
      mapViewer.addMouseMotionListener(panMouseInputListener);
      zoomMouseWheelListener = new ZoomMouseWheelListenerCursor(mapViewer);
      mapViewer.addMouseWheelListener(zoomMouseWheelListener);
      panKeyListener = new PanKeyListener(mapViewer);
      mapViewer.addKeyListener(panKeyListener);

      mapViewer.setOverlayPainter(painterForMarker(geoMarkerHolder.getCurrentGeoMarker()));
   }

   private Painter<JXMapViewer> painterForMarker(GeoMarker marker) {
      if (marker instanceof PointMarker) {
         PointMarker point = (PointMarker) marker;
         final WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<Waypoint>();
         waypointPainter.setWaypoints(Collections.singleton(new DefaultWaypoint(point.getPosition())));
         return waypointPainter;
      }
      else if (marker instanceof LineMarker) {
         List<GeoPosition> points = marker.getPoints();
         LinePainter linePainter = new LinePainter();
         linePainter.setPoints(points);
         return linePainter;
      }
      else if (marker instanceof PolygonMarker) {
         List<GeoPosition> points = marker.getPoints();
         AreaPainter areaPainter = new AreaPainter();
         areaPainter.setPoints(points);
         return areaPainter;
      }
      else {
         return null;
      }
   }

   @Override
   public void leaveMode(JXMapViewer mapViewer) {
      mapViewer.removeMouseListener(panMouseInputListener);
      mapViewer.removeMouseMotionListener(panMouseInputListener);
      mapViewer.removeMouseWheelListener(zoomMouseWheelListener);
      mapViewer.removeKeyListener(panKeyListener);

      mapViewer.setOverlayPainter(null);
   }

   @Override
   public String getHelpHint() {
      return i18n.text(GeoLocationFieldPanelResources.mode_hint_pan_zoom);
   }
}
