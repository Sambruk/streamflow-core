/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.client.infrastructure.ui;

import se.streamsource.streamflow.infrastructure.application.PageListItemValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Font;

/**
 * JAVADOC
 */
public class PageItemListCellRenderer
   extends DefaultListCellRenderer
{
   public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
   {
      if (value instanceof ListItemValue)
      {
         String val = ((ListItemValue)value).description().get();

         if (value instanceof PageListItemValue)
         {
            Component component = super.getListCellRendererComponent( list, ((ListItemValue)value).description().get(), index, isSelected, cellHasFocus );
            setFont( getFont().deriveFont( Font.ITALIC ));
            return component;
         } else
         {
            return super.getListCellRendererComponent( list, "   "+val, index, isSelected, cellHasFocus );
         }

      }
      return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
   }
}