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
package se.streamsource.streamflow.client.ui;

import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.util.i18n;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class ContextItemListRenderer
        extends DefaultListRenderer
{
   public ContextItemListRenderer()
   {
      super(new WrappingProvider(
              new IconValue()
              {
                 public Icon getIcon(Object o)
                 {
                    ContextItem item = (ContextItem) o;
                    return i18n.icon(Icons.valueOf(item.getRelation()), 16);
                 }
              },
              new StringValue()
              {
                 public String getString(Object o)
                 {
                    ContextItem item = (ContextItem) o;
                    String str = item.getName();
                    if (item.getCount() > 0)
                       str += " (" + item.getCount() + ")";
                    return str;
                 }
              },
              false
      ));
   }

   @Override
   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
   {
      JComponent component = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      ContextItem contextItem = (ContextItem) value;
      if (contextItem.getRelation().equals("perspective"))
      {
         component.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
      } else
      {
         component.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
      }
      return component;
   }
}
