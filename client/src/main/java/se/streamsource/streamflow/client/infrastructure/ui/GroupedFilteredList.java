/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.infrastructure.ui;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class GroupedFilteredList
      extends JPanel
{
   private JTextField textField;
   private JList list;
   private EventListModel listModel;
   public JScrollPane pane = new JScrollPane();

   public GroupedFilteredList()
   {
      setLayout( new BorderLayout() );

      textField = new JTextField( 20 );

      list = new JList();
      list.setCellRenderer( new SeparatorListCellRenderer(new ListItemListCellRenderer()) );
      pane.setViewportView( list );

      add( textField, BorderLayout.NORTH );
      add( pane, BorderLayout.CENTER );
   }

   public JTextField getTextField()
   {
      return textField;
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
      FilterList<ListItemValue> textFilteredIssues = new FilterList<ListItemValue>( sortedIssues, new TextComponentMatcherEditor( textField, new ListItemFilterator() ) );

      listModel = new EventListModel<ListItemValue>( new SeparatorList<ListItemValue>( textFilteredIssues, new ListItemGroupingComparator(), 1, 10000 ) );

      list.setModel( listModel );

      textField.setText( "" );
   }
}