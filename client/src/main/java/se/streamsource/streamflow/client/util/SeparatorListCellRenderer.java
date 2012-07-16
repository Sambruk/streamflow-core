/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.SeparatorList;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class SeparatorListCellRenderer
   extends DefaultListCellRenderer
{
   private ListCellRenderer next;

   public SeparatorListCellRenderer( ListCellRenderer next )
   {
      this.next = next;
   }

   public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
   {
      if (value instanceof SeparatorList.Separator)
      {
         SeparatorList.Separator separator = (SeparatorList.Separator) value;
         String text;
         if ( separator.first() instanceof TitledLinkValue)
         {
            text = ((TitledLinkValue) separator.first()).title().get();
         } else
         {
            text = ((LinkValue) separator.first()).text().get();
         }
         Component component = super.getListCellRendererComponent( list, text, index, isSelected, cellHasFocus );
         setFont( getFont().deriveFont( Font.BOLD ));
         return component;
      } else
         return next.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
   }
}
