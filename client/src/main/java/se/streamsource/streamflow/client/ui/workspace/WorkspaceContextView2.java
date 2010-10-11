/*
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

package se.streamsource.streamflow.client.ui.workspace;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SeparatorContextItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SeparatorListCellRenderer;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.ContextItemGroupComparator;
import se.streamsource.streamflow.client.ui.ContextItemListRenderer;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Comparator;
import java.util.List;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;

/**
 * JAVADOC
 */
public class WorkspaceContextView2
      extends JPanel
   implements TransactionListener
{
   private JList contextList;
   private JScrollPane workspaceContextScroll;
   private WorkspaceContextModel2 contextModel;

   public WorkspaceContextView2( final @Service ApplicationContext context,
                                 final @Structure ObjectBuilderFactory obf,
                                 @Uses final CommandQueryClient client )
   {
      setLayout( new BorderLayout() );

      setPreferredSize( new Dimension(250, 500) );

      this.contextModel = obf.newObjectBuilder( WorkspaceContextModel2.class ).use( client ).newInstance();

      contextList = new JList();
      Comparator<ContextItem> comparator = new ContextItemGroupComparator();

      JTextField filterField = new JTextField();
      SortedList<ContextItem> sortedIssues = new SortedList<ContextItem>( contextModel.getItems(), new Comparator<ContextItem>()
      {
         public int compare( ContextItem o1, ContextItem o2 )
         {
            return o1.getGroup().compareTo( o2.getGroup() );
         }
      });
      final FilterList<ContextItem> textFilteredIssues = new FilterList<ContextItem>( sortedIssues, new TextComponentMatcherEditor<ContextItem>( filterField, new TextFilterator<ContextItem>()
      {
         public void getFilterStrings( List<String> strings, ContextItem contextItem )
         {
            strings.add(contextItem.getGroup());
         }
      }) );
      EventList<ContextItem> separatorList = new SeparatorList<ContextItem>( textFilteredIssues, comparator, 1, 10000 );
      contextList.setModel( new EventListModel<ContextItem>( separatorList ) );
      contextList.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      contextList.setCellRenderer( new SeparatorContextItemListCellRenderer( new ContextItemListRenderer()));

      workspaceContextScroll = new JScrollPane( contextList );

      add( filterField, BorderLayout.NORTH );
      add( workspaceContextScroll, BorderLayout.CENTER );

      new RefreshWhenVisible( this, contextModel);
   }

   public JList getWorkspaceContextList()
   {
      return contextList;
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, withNames("joinedProject", "leftProject", "joinedGroup", "leftGroup",
            "createdProject", "removedProject", "changedDescription" )))
      {
         contextModel.refresh();
      }
   }
}
