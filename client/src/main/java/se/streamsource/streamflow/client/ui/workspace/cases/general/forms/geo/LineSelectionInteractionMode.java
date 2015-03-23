package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;

public class LineSelectionInteractionMode
   implements MapInteractionMode, MouseInputListener {

   private List<GeoPosition> points;
   private LinePainter linePainter;
   private JXMapViewer mapViewer;
   private GeoMarkerHolder geoMarkerHolder;

   @Override
   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder) {
      mapViewer.addMouseListener(this);
      mapViewer.addMouseMotionListener(this);

      points = new ArrayList<GeoPosition>();
      linePainter = new LinePainter();
      linePainter.setPoints(points);
      mapViewer.setOverlayPainter(linePainter);

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
      linePainter.setPoints(points);
      mapViewer.repaint();

      if (e.getClickCount() == 2) {
         LineMarker lineMarker = new LineMarker(Iterables.map(new Function<GeoPosition, PointMarker>() {
            @Override
            public PointMarker map(GeoPosition from) {
               return new PointMarker(from.getLatitude(), from.getLongitude());
            }
         }, points));
         geoMarkerHolder.updateGeoMarker(lineMarker);
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
      linePainter.setPoints(pointsWithMousePoint);
      mapViewer.repaint();
   }
}
