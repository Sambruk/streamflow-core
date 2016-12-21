/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.workspace.cases.general.forms.geo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

public class AreaPainter implements Painter<JXMapViewer>
{
	private Color color = new Color(1f, 0f, 0f, 0.5f);
   private Color outlineColor = Color.BLACK;

	private List<GeoPosition> points;

	public AreaPainter()
	{
	   points = new ArrayList<GeoPosition>();
	}

	void setPoints(Collection<GeoPosition> points) {
	   this.points= new ArrayList<GeoPosition>(points);
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int w, int h)
	{
		g = (Graphics2D) g.create();

		// convert from viewport to world bitmap
		Rectangle rect = map.getViewportBounds();
		g.translate(-rect.x, -rect.y);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Polygon polygon = pointsToPolygon(map, points);

      g.setPaint(color);
      g.fill(polygon);

		g.setPaint(outlineColor);
		g.setStroke(new BasicStroke(2));
		g.draw(polygon);

		g.dispose();
	}

	private Polygon pointsToPolygon(JXMapViewer map, List<GeoPosition> points) {
	   Polygon result = new Polygon();
	   for (GeoPosition p: points) {
	      Point2D p2d = map.getTileFactory().geoToPixel(p, map.getZoom());
	      result.addPoint((int) p2d.getX(), (int) p2d.getY());
	   }
	   return result;
   }
}
