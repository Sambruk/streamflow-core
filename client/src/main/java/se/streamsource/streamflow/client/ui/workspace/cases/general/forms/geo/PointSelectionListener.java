package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

public class PointSelectionListener implements MouseListener {

   private JXMapViewer mapViewer;
   private GeoMarkerHolder geoMarkerHolder;

   public PointSelectionListener(GeoMarkerHolder geoMarkerHolder,
         JXMapViewer mapViewer) {
      this.geoMarkerHolder = geoMarkerHolder;
      this.mapViewer = mapViewer;
   }

   @Override
   public void mouseReleased(MouseEvent e) {
   }

   @Override
   public void mousePressed(MouseEvent e) {
   }

   @Override
   public void mouseExited(MouseEvent e) {
   }

   @Override
   public void mouseEntered(MouseEvent e) {
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         GeoPosition geoPosition = mapViewer.convertPointToGeoPosition(e.getPoint());
         GeoMarker marker = new PointMarker(geoPosition.getLatitude(), geoPosition.getLongitude());
         geoMarkerHolder.updateGeoMarker(marker);
      }
   }
}
