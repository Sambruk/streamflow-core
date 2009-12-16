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
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class GroupedFilteredTree
      extends JPanel
{
   private JTextField textField;
   private JTree tree;
   private EventListModel treeModel;
   public JScrollPane pane = new JScrollPane();

   public GroupedFilteredTree()
   {
      setLayout( new BorderLayout() );

      textField = new JTextField( 20 );

      tree = new JTree();
      tree.setCellRenderer( new ListItemTreeCellRenderer() );
      pane.setViewportView( tree );

      add( textField, BorderLayout.NORTH );
      add( pane, BorderLayout.CENTER );
   }

   public JTextField getTextField()
   {
      return textField;
   }

   public JTree getTree()
   {
      return tree;
   }

   public JScrollPane getPane()
   {
      return pane;
   }

   public void setEventList( EventList<ListItemValue> eventList )
   {
      SortedList<ListItemValue> sortedIssues = new SortedList<ListItemValue>( eventList, new ListItemComparator() );
      FilterList<ListItemValue> textFilteredIssues = new FilterList<ListItemValue>( sortedIssues, new TextComponentMatcherEditor( textField, new ListItemFilterator() ) );
      
//      treeModel = new EventTreeModel<ListItemValue>( new TreeList<ListItemValue>(textFilteredIssues, ) );

//      tree.setModel( treeModel );

      textField.setText( "" );


   }
}