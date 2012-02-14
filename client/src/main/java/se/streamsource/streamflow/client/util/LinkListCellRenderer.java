/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.TitledLinkValue;

import javax.swing.*;
import java.awt.*;

/**
 * List renderer for lists that use LinkValue as items.
 */
public class LinkListCellRenderer extends DefaultListCellRenderer
{
   public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
   {
      if (value instanceof LinkValue)
      {
         LinkValue itemValue = (LinkValue) value;
         String val = itemValue == null ? "" : itemValue.text().get();

         if (value instanceof TitledLinkValue)
            val = "  "+val;

         return super.getListCellRendererComponent( list, val, index, isSelected, cellHasFocus );
      } else return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
   }
}