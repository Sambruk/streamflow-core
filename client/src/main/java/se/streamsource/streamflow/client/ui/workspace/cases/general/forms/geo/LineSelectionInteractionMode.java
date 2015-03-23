package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputListener;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;

public class LineSelectionInteractionMode
   implements MapInteractionMode, MouseInputListener {

   @Override
   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder) {
      mapViewer.addMouseListener(this);
      mapViewer.addMouseMotionListener(this);
// TODO:
//      mapViewer.setOverlayPainter();
   }

   @Override
   public void leaveMode(JXMapViewer mapViewer) {
      mapViewer.removeMouseListener(this);
      mapViewer.removeMouseMotionListener(this);
      mapViewer.setOverlayPainter(null);
   }

   @Override
   public void mouseClicked(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }

   @Override
   public void mousePressed(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }

   @Override
   public void mouseReleased(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }

   @Override
   public void mouseEntered(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }

   @Override
   public void mouseExited(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }

   @Override
   public void mouseDragged(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }

   @Override
   public void mouseMoved(MouseEvent e) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Not implemented");

   }
}
