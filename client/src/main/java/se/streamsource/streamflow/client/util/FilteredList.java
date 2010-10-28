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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.dci.value.LinkValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class FilteredList
      extends JPanel
{
   private JTextField filterField;
   private JList list;
   public JScrollPane pane = new JScrollPane();

   public FilteredList()
   {
      setLayout( new BorderLayout() );

      filterField = new JTextField( 20 );

      list = new JList();
      list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
      FilterLinkListCellRenderer filterCellRenderer = new FilterLinkListCellRenderer();

      list.setCellRenderer( filterCellRenderer );
      pane.setViewportView( list );

      add( filterField, BorderLayout.NORTH );
      add( pane, BorderLayout.CENTER );
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

   public void setEventList( EventList<LinkValue> eventList )
   {
      SortedList<LinkValue> sortedIssues = new SortedList<LinkValue>( eventList, new LinkComparator() );
      final FilterList<LinkValue> textFilteredIssues = new FilterList<LinkValue>( sortedIssues, new TextComponentMatcherEditor( filterField, new LinkFilterator() ) );
      EventListModel listModel = new EventListModel<LinkValue>( textFilteredIssues );

      textFilteredIssues.addListEventListener( new ListEventListener<LinkValue>()
      {
         public void listChanged( ListEvent<LinkValue> linkValueListEvent )
         {
            if (list.getModel().getSize() > 0)
            {
               for (int i = 0; i < list.getModel().getSize(); i++)
               {
                  if (list.getModel().getElementAt( i ) != null)
                  {
                     final int idx = i;
                     
                     SwingUtilities.invokeLater( new Runnable()
                     {
                        public void run()
                        {
                           list.setSelectedIndex( idx );
                        }
                     });

                     break;
                  }
               }

            }
         }
      });

      list.setModel( listModel );

      filterField.setText( "" );


   }
}
