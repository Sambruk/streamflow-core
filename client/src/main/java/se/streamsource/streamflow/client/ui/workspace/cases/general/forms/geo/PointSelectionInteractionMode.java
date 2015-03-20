package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import org.jxmapviewer.JXMapViewer;

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
}
