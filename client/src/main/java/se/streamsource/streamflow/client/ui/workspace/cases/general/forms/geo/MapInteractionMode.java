package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import org.jxmapviewer.JXMapViewer;

public interface MapInteractionMode {

   public void enterMode(JXMapViewer mapViewer, GeoMarkerHolder geoMarkerHolder);

   public void leaveMode(JXMapViewer mapViewer);

}
