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
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.streamflow.infrastructure.application.LinkValue;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class FilteredList
      extends JPanel
{
   private JTextField textField;
   private JList list;
   private EventListModel listModel;
   public JScrollPane pane = new JScrollPane();

   public FilteredList()
   {
      setLayout( new BorderLayout() );

      textField = new JTextField( 20 );

      list = new JList();
      list.setCellRenderer( new LinkListCellRenderer() );
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

   public void setEventList( EventList<LinkValue> eventList )
   {
      SortedList<LinkValue> sortedIssues = new SortedList<LinkValue>( eventList, new LinkComparator() );
      final FilterList<LinkValue> textFilteredIssues = new FilterList<LinkValue>( sortedIssues, new TextComponentMatcherEditor( textField, new LinkFilterator() ) );
      listModel = new EventListModel<LinkValue>( textFilteredIssues );

      textFilteredIssues.addListEventListener( new ListEventListener<LinkValue>()
      {
         public void listChanged( ListEvent<LinkValue> linkValueListEvent )
         {
            if (textFilteredIssues.size() == 1)
            {
               list.setSelectedIndex( 0 );
            }
         }
      });

      list.setModel( listModel );

      textField.setText( "" );


   }
}
