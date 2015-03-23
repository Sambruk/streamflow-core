package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

public class AreaSelectionInteractionMode
   implements MapInteractionMode, MouseInputListener {

   private List<GeoPosition> points;
   private AreaPainter areaPainter;
   private JXMapViewer mapViewer;
   private GeoMarkerHolder geoMarkerHolder;

   @Override
   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder) {
      mapViewer.addMouseListener(this);
      mapViewer.addMouseMotionListener(this);

      points = new ArrayList<GeoPosition>();
      areaPainter = new AreaPainter();
      areaPainter.setPoints(points);
      mapViewer.setOverlayPainter(areaPainter);

      this.mapViewer = mapViewer;
      this.geoMarkerHolder = geoMarkerHolder;
   }

   @Override
   public void leaveMode(JXMapViewer mapViewer) {
      mapViewer.removeMouseListener(this);
      mapViewer.removeMouseMotionListener(this);
      mapViewer.setOverlayPainter(null);
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
      points.add(geoPosition);
      areaPainter.setPoints(points);
      mapViewer.repaint();

      if (e.getClickCount() == 2) {
         points.add(points.get(0)); // Close polygon
         PolygonMarker areaMarker = new PolygonMarker(Iterables.map(new Function<GeoPosition, PointMarker>() {
            @Override
            public PointMarker map(GeoPosition from) {
               return new PointMarker(from.getLatitude(), from.getLongitude());
            }
         }, points));
         geoMarkerHolder.updateGeoMarker(areaMarker);
      }
   }

   @Override
   public void mousePressed(MouseEvent e) {
   }

   @Override
   public void mouseReleased(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   @Override
   public void mouseExited(MouseEvent e) {
   }

   @Override
   public void mouseDragged(MouseEvent e) {
   }

   @Override
   public void mouseMoved(MouseEvent e) {
      GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
      List<GeoPosition> pointsWithMousePoint = new ArrayList<GeoPosition>(points);
      pointsWithMousePoint.add(geoPosition);
      areaPainter.setPoints(pointsWithMousePoint);
      mapViewer.repaint();
   }
}
