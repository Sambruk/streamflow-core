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

package se.streamsource.streamflow.client.ui.overview;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventListModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.Icons;
import se.streamsource.streamflow.client.infrastructure.ui.RefreshWhenVisible;
import se.streamsource.streamflow.client.infrastructure.ui.SeparatorContextItemListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SeparatorListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.ui.ContextItemGroupComparator;
import se.streamsource.streamflow.client.ui.ContextItemListRenderer;
import se.streamsource.streamflow.client.ui.caze.CaseTableView;
import se.streamsource.streamflow.client.ui.caze.InboxCaseTableFormatter;
import se.streamsource.streamflow.client.ui.caze.OverviewAssignmentsCaseTableFormatter;

import javax.swing.AbstractAction;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;

/**
 * JAVADOC
 */
public class OverviewView
      extends JPanel
{
   private JList overviewList;
   private JSplitPane pane;
   private OverviewModel model;

   public OverviewView( final @Service ApplicationContext context,
                        final @Uses CommandQueryClient client,
                        final @Structure ObjectBuilderFactory obf )
   {
      super( new BorderLayout() );

      overviewList = new JList();

      model = obf.newObjectBuilder( OverviewModel.class ).use( client ).newInstance();

      JTextField filterField = new JTextField();
      SortedList<ContextItem> sortedIssues = new SortedList<ContextItem>( model.getItems(), new Comparator<ContextItem>()
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

      Comparator<ContextItem> comparator = new ContextItemGroupComparator();
      EventList<ContextItem> separatorList = new SeparatorList<ContextItem>( textFilteredIssues, comparator, 1, 10000 );
      overviewList.setModel( new EventListModel<ContextItem>( separatorList ) );
      overviewList.getSelectionModel().setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

      overviewList.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 ), "select" );
      overviewList.getActionMap().put( "select", new AbstractAction()
      {
         public void actionPerformed( ActionEvent e )
         {
            pane.getRightComponent().requestFocus();
         }
      } );

      overviewList.setCellRenderer( new SeparatorContextItemListCellRenderer( new ContextItemListRenderer()));

      JScrollPane workspaceScroll = new JScrollPane( overviewList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

      pane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
      pane.setDividerLocation( 200 );
      pane.setResizeWeight( 0 );

      JPanel overviewOutline = new JPanel( new BorderLayout() );
      overviewOutline.add( workspaceScroll, BorderLayout.CENTER );
      overviewOutline.setMinimumSize( new Dimension( 150, 300 ) );

      pane.setLeftComponent( overviewOutline );
      pane.setRightComponent( new JPanel() );

      add( pane, BorderLayout.CENTER );

      overviewList.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               JList list = (JList) e.getSource();

               if (list.getSelectedValue() == null)
                  return;

               if (list.getSelectedValue() instanceof ContextItem)
               {
                  ContextItem contextItem = (ContextItem) list.getSelectedValue();
                  TableFormat tableFormat;
                  if (contextItem.getRelation().equals(Icons.assign.name()))
                  {
                     tableFormat = new OverviewAssignmentsCaseTableFormatter();
                  } else
                  {
                     tableFormat = new InboxCaseTableFormatter();
                  }
                  CaseTableView caseTable = obf.newObjectBuilder( CaseTableView.class ).use( contextItem.getClient(), tableFormat ).newInstance();

//                  caseTable.getCaseTable().getSelectionModel().addListSelectionListener( new CaseSelectionListener() );

                  pane.setRightComponent( caseTable );
               } else
               {
                  // TODO Overview of all projects
/*
                  final OverviewSummaryModel overviewSummaryModel = model.summary();

                  view = obf.newObjectBuilder( OverviewSummaryView.class ).use( overviewSummaryModel ).newInstance();
                  context.getTaskService().execute( new Task( context.getApplication() )
                  {
                     protected Object doInBackground() throws Exception
                     {
                        overviewSummaryModel.refresh();
                        return null;
                     }
                  } );
*/
               }
            }
         }
      } );

      new RefreshWhenVisible(this, model);
   }
}