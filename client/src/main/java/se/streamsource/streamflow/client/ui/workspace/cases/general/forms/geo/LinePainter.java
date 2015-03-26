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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.painter.Painter;

public class LinePainter implements Painter<JXMapViewer>
{
	private Color color = Color.RED;
   private Color outlineColor = Color.BLACK;

	private List<GeoPosition> points;

	public LinePainter()
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

		// Draw outline
		g.setColor(outlineColor);
		g.setStroke(new BasicStroke(4));
		drawLines(g, map);

		// Draw interior on top of outline
		g.setColor(color);
		g.setStroke(new BasicStroke(2));
		drawLines(g, map);

		g.dispose();
	}

	private void drawLines(Graphics2D g, JXMapViewer map)
	{
		int lastX = 0;
		int lastY = 0;

		boolean first = true;

		for (GeoPosition gp : points)
		{
			// convert geo-coordinate to world bitmap pixel
			Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

			if (first)
			{
				first = false;
			}
			else
			{
				g.drawLine(lastX, lastY, (int) pt.getX(), (int) pt.getY());
			}

			lastX = (int) pt.getX();
			lastY = (int) pt.getY();
		}
	}
}
