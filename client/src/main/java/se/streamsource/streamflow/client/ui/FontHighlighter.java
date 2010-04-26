/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import java.awt.Component;
import java.awt.Font;

/**
 * JAVADOC
 */
public class FontHighlighter
      extends AbstractHighlighter
{

   private Font highlightedFont;
   private Font normalFont;

   public FontHighlighter( HighlightPredicate predicate, Font highlightedFont, Font normalFont )
   {
      super( predicate );
      this.normalFont = normalFont;
      setHighlightedFont( highlightedFont );
   }

   @Override
   public Component highlight( Component component, ComponentAdapter componentAdapter )
   {
      if (getHighlightPredicate().isHighlighted( component, componentAdapter ))
         component.setFont( highlightedFont );
      else
         component.setFont( normalFont );
      return super.highlight( component, componentAdapter );
   }

   protected Component doHighlight( Component component, ComponentAdapter componentAdapter )
   {
      return component;
   }

   public final void setHighlightedFont( Font highlightedFont )
   {
      if (highlightedFont.equals( this.highlightedFont )) return;
      this.highlightedFont = highlightedFont;
      fireStateChanged();
   }
}
