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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * Rounded border. Draws a rounded border to the left and right of the component.
 */
public class RoundedBorder implements Border
{
   int radius;

   public RoundedBorder()
   {
      this(5);
   }

   public RoundedBorder( int radius )
   {
      this.radius = radius;
   }

   public Insets getBorderInsets( Component c )
   {
      return new Insets( 0, radius , 0 , radius );
   }

   public boolean isBorderOpaque()
   {
      return true;
   }

   public void paintBorder( Component c, Graphics g, int x, int y, int width, int height )
   {
      // Fill background color
      g.setColor( c.getParent().getBackground() );
      g.fillRect( x, y, radius, height );
      g.fillRect( x+width-radius, y, radius, height );

      g.setColor( c.getBackground() );
      // Left side
      g.fillArc( x, y, radius*2, radius*2, 90, 90 );
      g.fillArc( x, y+height-(radius*2)-1, radius*2, radius*2, 180, 90 );
      g.fillRect( x, y+radius, radius+1, height-2*radius );

      // Right side
      g.fillArc( x+width-(radius*2)-1, y, radius*2, radius*2, 0, 90 );
      g.fillArc( x+width-(radius*2)-1, y+height-(radius*2)-1, radius*2, radius*2, 270, 90 );
      g.fillRect( x+width-radius-1, y+radius, radius+1, height-2*radius );
   }
}
