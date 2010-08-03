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

import java.awt.BorderLayout;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;

/**
 * JAVADOC
 */
public class FormFieldsList
      extends JPanel
{
   private JList list;
   public JScrollPane pane = new JScrollPane();

   public FormFieldsList()
   {
      setLayout( new BorderLayout() );

      list = new JList();
      list.setCellRenderer( new SeparatorListCellRenderer(new ListItemListCellRenderer()) );
      list.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (list.getSelectedValue() instanceof SeparatorList.Separator)
               list.clearSelection();
         }
      });
      pane.setViewportView( list );

      add( pane, BorderLayout.CENTER );
   }

   public JList getList()
   {
      return list;
   }

   public JScrollPane getPane()
   {
      return pane;
   }

   public void setEventList( EventList<ListItemValue> eventList )
   {
      SortedList<ListItemValue> sortedIssues = new SortedList<ListItemValue>( eventList, new ListItemComparator() );

      EventListModel listModel = new EventListModel<ListItemValue>( new SeparatorList<ListItemValue>( sortedIssues, new ListItemGroupingComparator(), 1, 10000 ) );

      list.setModel( listModel );
   }
}