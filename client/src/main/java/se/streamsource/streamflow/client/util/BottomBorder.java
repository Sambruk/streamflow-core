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
package se.streamsource.streamflow.client.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

public class BottomBorder extends AbstractBorder
{
   protected int thickness;

   protected Color lineColor;

   protected int gap;

   public BottomBorder(Color color)
   {
      this(color, 1, 1);
   }

   public BottomBorder(Color color, int thickness)
   {
      this(color, thickness, thickness);
   }

   public BottomBorder(Color color, int thickness, int gap)
   {
      lineColor = color;
      this.thickness = thickness;
      this.gap = gap;
   }

   public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
   {
      Color oldColor = g.getColor();
      int i;

      g.setColor(lineColor);
      for (i = 0; i < thickness; i++)
      {
         g.drawLine(x, y + height - i - 1, x + width, y + height - i - 1);
      }
      g.setColor(oldColor);
   }

   public Insets getBorderInsets(Component c)
   {
      return new Insets(0, 0, gap, 0);
   }

   public Insets getBorderInsets(Component c, Insets insets)
   {
      insets.left = 0;
      insets.top = 0;
      insets.right = 0;
      insets.bottom = gap;
      return insets;
   }

   /**
    * Returns the color of the border.
    */
   public Color getLineColor()
   {
      return lineColor;
   }

   /**
    * Returns the thickness of the border.
    */
   public int getThickness()
   {
      return thickness;
   }

   /**
    * Returns whether or not the border is opaque.
    */
   public boolean isBorderOpaque()
   {
      return false;
   }

   public int getGap()
   {
      return gap;
   }

}
