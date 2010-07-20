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

package se.streamsource.streamflow.client.infrastructure.ui;

import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.Component;

/**
 * List renderer for lists that use LinkValue as items.
 */
public class FilterLinkListCellRenderer extends DefaultListCellRenderer
      implements DocumentListener
{
   private String filterText = "";

   public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
   {
      if (value instanceof LinkValue)
      {
         LinkValue itemValue = (LinkValue) value;
         String val = itemValue == null ? "" : itemValue.text().get();

         if (value instanceof TitledLinkValue)
         {
            if (((TitledLinkValue) value).classes().get() != null)
            {
               val = "<html>&nbsp; " + highlightQuery( val ) + " [" + highlightQuery( ((TitledLinkValue) value).classes().get().trim() ) + "]</html>";
            } else
               val = "  " + highlightQuery( val );
         }

         return super.getListCellRendererComponent( list, val, index, isSelected, cellHasFocus );
      } else return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
   }

   private String highlightQuery( String s )
   {
      if (!"".equals( filterText ) && s.indexOf( filterText ) != -1)
      {
         return s.replace( filterText, "<b>" + filterText + "</b>" );
      }
      return s;
   }

   public void insertUpdate( DocumentEvent e )
   {
      try
      {
         String oldValue = filterText;
         filterText = e.getDocument().getText( 0, e.getDocument().getLength() );
         this.firePropertyChange( "text", oldValue, filterText );
      } catch (BadLocationException e1)
      {
         e1.printStackTrace();
      }
   }

   public void removeUpdate( DocumentEvent e )
   {
      try
      {
         String oldValue = filterText;
         filterText = e.getDocument().getText( 0, e.getDocument().getLength() );
         this.firePropertyChange( "text", oldValue, filterText );
      } catch (BadLocationException e1)
      {
         e1.printStackTrace();
      }
   }

   public void changedUpdate( DocumentEvent e )
   {
   }
}