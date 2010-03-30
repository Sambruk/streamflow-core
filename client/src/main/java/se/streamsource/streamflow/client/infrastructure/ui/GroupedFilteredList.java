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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
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
      list.setCellRenderer( new SeparatorListCellRenderer(new LinkListCellRenderer()) );
      list.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (list.getSelectedValue() instanceof SeparatorList.Separator)
               list.clearSelection();
         }
      });
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

   public void setEventList( EventList<TitledLinkValue> eventList )
   {
      SortedList<TitledLinkValue> sortedIssues = new SortedList<TitledLinkValue>( eventList, new LinkComparator() );
      final FilterList<TitledLinkValue> textFilteredIssues = new FilterList<TitledLinkValue>( sortedIssues, new TextComponentMatcherEditor( textField, new LinkFilterator() ) );

      listModel = new EventListModel<TitledLinkValue>( new SeparatorList<TitledLinkValue>( textFilteredIssues, new TitledLinkGroupingComparator(), 1, 10000 ) );

      list.setModel( listModel );

      textField.setText( "" );

      textFilteredIssues.addListEventListener( new ListEventListener<LinkValue>()
      {
         public void listChanged( ListEvent<LinkValue> linkValueListEvent )
         {
            if (textFilteredIssues.size() == 1)
            {
               list.setSelectedIndex( 1 );
            }
         }
      });
   }
}