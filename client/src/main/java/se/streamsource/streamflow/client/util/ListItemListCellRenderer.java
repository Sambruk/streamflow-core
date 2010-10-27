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

package se.streamsource.streamflow.client.util;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;

/**
 * List renderer for lists that use ListItemValue as items.
 */
public class ListItemListCellRenderer extends DefaultListCellRenderer
{
   public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
   {
      if (value instanceof ListItemValue)
      {
         ListItemValue itemValue = (ListItemValue) value;
         String val = itemValue == null ? "" : itemValue.description().get();

         return super.getListCellRendererComponent( list, val, index, isSelected, cellHasFocus );
      } else return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
   }
}
