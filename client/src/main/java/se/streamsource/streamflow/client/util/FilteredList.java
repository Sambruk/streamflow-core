/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.event.*;
import ca.odell.glazedlists.swing.*;
import se.streamsource.dci.value.link.*;

import javax.swing.*;
import java.awt.*;

/**
 * JAVADOC
 */
public class FilteredList extends JPanel
{
   private JTextField filterField;
   private JList list;
   public JScrollPane pane = new JScrollPane();

   public FilteredList()
   {
      setLayout(new BorderLayout());

      filterField = new JTextField(20);

      list = new JList();
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      FilterLinkListCellRenderer filterCellRenderer = new FilterLinkListCellRenderer();

      list.setCellRenderer(filterCellRenderer);
      pane.setViewportView(list);

      add(filterField, BorderLayout.NORTH);
      add(pane, BorderLayout.CENTER);
   }

   public JTextField getFilterField()
   {
      return filterField;
   }

   public JList getList()
   {
      return list;
   }

   public JScrollPane getPane()
   {
      return pane;
   }

   public void setEventList(EventList<LinkValue> eventList)
   {
      setEventList(eventList, true);
   }

   public void setEventList(EventList<LinkValue> eventList, boolean addFirstIndexSelector)
   {
      final FilterList<LinkValue> textFilteredIssues = new FilterList<LinkValue>(eventList,
              new TextComponentMatcherEditor(filterField, new LinkFilterator()));
      EventListModel listModel = new EventListModel<LinkValue>(textFilteredIssues);

      if (addFirstIndexSelector)
      {
         textFilteredIssues.addListEventListener(new ListEventListener<LinkValue>()
         {
            public void listChanged(ListEvent<LinkValue> linkValueListEvent)
            {
               if (list.getModel().getSize() > 0)
               {
                  for (int i = 0; i < list.getModel().getSize(); i++)
                  {
                     if (list.getModel().getElementAt(i) != null)
                     {
                        final int idx = i;

                        SwingUtilities.invokeLater(new Runnable()
                        {
                           public void run()
                           {
                              list.setSelectedIndex(idx);
                           }
                        });

                        break;
                     }
                  }

               }
            }
         });
      }

      list.setModel(listModel);

      filterField.setText("");

   }
}
