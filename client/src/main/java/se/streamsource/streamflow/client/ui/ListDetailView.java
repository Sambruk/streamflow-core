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

package se.streamsource.streamflow.client.ui;

import ca.odell.glazedlists.swing.EventListModel;
import com.jgoodies.forms.factories.Borders;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.client.infrastructure.ui.LinkListCellRenderer;
import se.streamsource.streamflow.client.infrastructure.ui.SelectionActionEnabler;
import se.streamsource.streamflow.client.ui.administration.TabbedResourceView;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationUsersView;
import se.streamsource.streamflow.client.ui.administration.organization.OrganizationsView;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/**
 * JAVADOC
 */
public abstract class ListDetailView
      extends JSplitPane
   implements TransactionListener
{
   protected JList list;

   public ListDetailView()
   {
      setBorder( Borders.createEmptyBorder("2dlu, 2dlu, 2dlu, 2dlu"));

      setRightComponent( new JPanel() );
      setBorder( BorderFactory.createEmptyBorder() );

      setDividerLocation( 250 );
      setOneTouchExpandable( true );
   }

   protected void initMaster( EventListModel<LinkValue> listModel, Action createAction, Action[] selectionActions, final DetailFactory factory)
   {
      list = new JList(listModel);
      list.setCellRenderer( new LinkListCellRenderer() );

      JScrollPane scrollPane = new JScrollPane( list );

      JPanel master = new JPanel(new BorderLayout());
      master.add( scrollPane, BorderLayout.CENTER );

      // Toolbar
      JPanel toolbar = new JPanel();

      if (createAction != null)
         toolbar.add( new JButton( createAction ) );

      if (selectionActions.length != 0)
      {
         JPopupMenu options = new JPopupMenu();
         for (Action selectionAction : selectionActions)
         {
            options.add( selectionAction );
         }
         list.getSelectionModel().addListSelectionListener( new SelectionActionEnabler( selectionActions ) );
         toolbar.add( new JButton( new OptionsAction( options ) ) );
      }

      master.add( toolbar, BorderLayout.SOUTH);

      setLeftComponent( master );

      list.addListSelectionListener( new ListSelectionListener()
      {
         public void valueChanged( ListSelectionEvent e )
         {
            if (!e.getValueIsAdjusting())
            {
               LinkValue detailLink = (LinkValue) list.getSelectedValue();
               if (detailLink != null)
               {
                  int selectedIndex = -1;
                  if (getRightComponent() != null && getRightComponent() instanceof TabbedResourceView)
                  {
                     TabbedResourceView tab = (TabbedResourceView) getRightComponent();
                     selectedIndex = tab.getSelectedIndex();
                  }

                  Component detailView = factory.createDetail( detailLink );

                  if (detailView instanceof TabbedResourceView)
                  {
                     ((TabbedResourceView)detailView).setSelectedIndex( selectedIndex );
                  }

                  setRightComponent( detailView );

                  if (detailView instanceof ListDetailView || detailView instanceof TabbedResourceView)
                  {
                     ListDetailView parentListDetailView = (ListDetailView) SwingUtilities.getAncestorOfClass( ListDetailView.class, ListDetailView.this);
                     if (parentListDetailView != null)
                        parentListDetailView.setDividerLocation( 0 );
                  }

               } else
               {
                  setRightComponent( new JPanel() );
               }
            }
         }
      } );
   }

   public interface DetailFactory
   {
      Component createDetail(LinkValue detailLink);
   }
}