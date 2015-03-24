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
         waypointPainter.setWaypoints(Collections.singleton(new DefaultWaypoint(point.getLatitude(), point.getLongitude())));
         return waypointPainter;
      }
      else if (marker instanceof LineMarker) {
         LineMarker line = (LineMarker) marker;
         List<GeoPosition> points = GeoUtils.positionList(line.getPoints());
         LinePainter linePainter = new LinePainter();
         linePainter.setPoints(points);
         return linePainter;
      }
      else if (marker instanceof PolygonMarker) {
         PolygonMarker polygon = (PolygonMarker) marker;
         List<GeoPosition> points = GeoUtils.positionList(polygon.getPoints());
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
}
