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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.TitledLinkValue;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * JAVADOC
 */
public class GroupedFilteredList
      extends JPanel implements PropertyChangeListener
{
   private JTextField textField;
   private JList list;
   public JScrollPane pane = new JScrollPane();

   public GroupedFilteredList()
   {
      setLayout( new BorderLayout() );

      textField = new JTextField( 20 );

      FilterLinkListCellRenderer filterCellRenderer = new FilterLinkListCellRenderer();
      filterCellRenderer.addPropertyChangeListener( this );
      textField.getDocument().addDocumentListener( filterCellRenderer );

      list = new JList();

      list.setCellRenderer( new SeparatorListCellRenderer( filterCellRenderer ) );
      list.getSelectionModel().addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (list.getSelectedValue() instanceof SeparatorList.Separator)
               list.clearSelection();
         }
      } );
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
      TextComponentMatcherEditor editor = new TextComponentMatcherEditor( textField, new LinkFilterator() );
      editor.addMatcherEditorListener( new MatcherEditor.Listener()
      {
         public void changedMatcher( MatcherEditor.Event event )
         {
            for (int i = 0; i < list.getModel().getSize(); i++)
            {
               if (list.getModel().getElementAt( i ) != null && list.getModel().getElementAt( i ) instanceof LinkValue)
               {
                  final int idx = i;

                  SwingUtilities.invokeLater( new Runnable()
                  {
                     public void run()
                     {
                        list.setSelectedIndex( idx );
                     }
                  } );

                  break;
               }
            }
         }
      });
      final FilterList<TitledLinkValue> textFilteredIssues = new FilterList<TitledLinkValue>( sortedIssues, editor );

      EventListModel listModel = new EventListModel<TitledLinkValue>( new SeparatorList<TitledLinkValue>( textFilteredIssues, new TitledLinkGroupingComparator(), 1, 10000 ) );

      list.setModel( listModel );

      textField.setText( "" );
   }

   public void propertyChange( PropertyChangeEvent evt )
   {
      if (evt.getPropertyName().equals( "text" ))
      {
         list.repaint();
      }
   }
}