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
import ca.odell.glazedlists.swing.EventListModel;
import org.jdesktop.swingx.JXList;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * JAVADOC
 */
public class GroupedList
      extends JPanel
{
   private JXList list;
   public JScrollPane pane = new JScrollPane();

   public GroupedList()
   {
      setLayout( new BorderLayout() );

      list = new JXList();
      list.setCellRenderer( new PageItemListCellRenderer() );

      pane.setViewportView( list );

      add( pane, BorderLayout.CENTER );
   }

   public JXList getList()
   {
      return list;
   }

   public JScrollPane getPane()
   {
      return pane;
   }

   public void setEventList( EventList<ListItemValue> eventList )
   {
      list.setModel( new EventListModel<ListItemValue>( eventList ) );
   }
}